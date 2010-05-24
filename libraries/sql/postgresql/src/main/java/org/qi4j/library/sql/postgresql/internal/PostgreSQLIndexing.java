/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.qi4j.library.sql.postgresql.internal;

import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_NAME;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.sql.api.SQLIndexing;
import org.qi4j.library.sql.common.QNameInfo;
import org.qi4j.library.sql.common.QNameInfo.QNameType;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.value.ValueDescriptor;

/**
 *
 * @author Stanislav Muhametsin
 */
public class PostgreSQLIndexing implements SQLIndexing
{
   @Structure
   private Application _app;

   @Structure
   private Qi4jSPI _qi4SPI;

   @This
   private PostgreSQLDBState _state;

   @This private PostgreSQLTypeHelper _sqlTypeHelper;

   @Override
   public void indexEntities(Iterable<EntityState> changedStates, Connection connection) throws SQLException
   {
      Boolean wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
      PreparedStatement insertToEntityTablePS = null;
      PreparedStatement updateEntityTablePS = null;
      PreparedStatement removeEntityPS = null;
      PreparedStatement queryEntityPKPS = null;
      PreparedStatement insertToPropertyQNamesPS = null;
      PreparedStatement clearQNamesPS = null;
      Map<QualifiedName, PreparedStatement> qNameInsertPSs = new HashMap<QualifiedName, PreparedStatement>();

      try
      {
         insertToEntityTablePS = connection.prepareStatement(String.format(SQLs.SIX_VALUE_INSERT, this._state.schemaName().get(), ENTITY_TABLE_NAME));
         updateEntityTablePS = connection.prepareStatement(String.format(SQLs.UPDATE_ENTITY_TABLE, this._state.schemaName().get()));
         removeEntityPS = connection.prepareStatement(String.format(SQLs.DELETE_FROM_ENTITY_TABLE, this._state.schemaName().get()));
         queryEntityPKPS = connection.prepareStatement(String.format(SQLs.QUERY_ENTITY_PK_BY_IDENTITY, this._state.schemaName().get()));
         insertToPropertyQNamesPS = connection.prepareStatement( String.format( SQLs.TWO_VALUE_INSERT, this._state.schemaName( ).get( ), SQLs.ALL_QNAMES_TABLE_NAME ) );
         clearQNamesPS = connection.prepareStatement( String.format( SQLs.CLEAR_ENTITY_DATA, this._state.schemaName( ).get( ), SQLs.ALL_QNAMES_TABLE_NAME ) );
         Map<Long, EntityState> statesByPK = new HashMap<Long, EntityState>();
         Map<Long, Integer> qNamePKs = new HashMap<Long, Integer>( );

         for (EntityState eState : changedStates)
         {
            EntityStatus status = eState.status();
            Long pk = null;
            if (status.equals(EntityStatus.NEW))
            {
               pk = this.insertEntityInfoAndProperties(connection, qNameInsertPSs, insertToPropertyQNamesPS, insertToEntityTablePS, eState, qNamePKs);
            }
            else if (status.equals(EntityStatus.UPDATED))
            {
               pk = this.updateEntityInfoAndProperties(connection, qNameInsertPSs, insertToPropertyQNamesPS, clearQNamesPS, queryEntityPKPS, updateEntityTablePS, insertToEntityTablePS, eState, qNamePKs);
            }
            else if (status.equals(EntityStatus.REMOVED))
            {
               this.removeEntity(eState, removeEntityPS);
            }
            else
            {
               throw new UnsupportedOperationException("Did not understand what to do with state [id = " + eState.identity().identity() + ", status = " + status + "].");
            }

            if (pk != null)
            {
               statesByPK.put(pk, eState);
            }
         }

         removeEntityPS.executeBatch();
         insertToEntityTablePS.executeBatch();
         updateEntityTablePS.executeBatch();

         for (Map.Entry<Long, EntityState> entry : statesByPK.entrySet())
         {
            EntityState eState = entry.getValue();
            Long pk = entry.getKey();
            EntityStatus status = eState.status();
            if (status.equals(EntityStatus.NEW) || status.equals(EntityStatus.UPDATED))
            {
               this.insertAssoAndManyAssoQNames(qNameInsertPSs, queryEntityPKPS, insertToPropertyQNamesPS, eState, qNamePKs.get( pk ), pk);
            }
         }

         insertToPropertyQNamesPS.executeBatch( );
         clearQNamesPS.executeBatch( );

         for (PreparedStatement ps : qNameInsertPSs.values())
         {
            ps.executeBatch();
         }

         connection.commit();

      }
      catch (SQLException sqle)
      {
         connection.rollback();
         throw sqle;
      }
      finally
      {
         connection.setAutoCommit(wasAutoCommit);
         this.closePSIfNotNull(insertToEntityTablePS);
         this.closePSIfNotNull(updateEntityTablePS);
         this.closePSIfNotNull(removeEntityPS);
         this.closePSIfNotNull(queryEntityPKPS);
         for (PreparedStatement ps : qNameInsertPSs.values())
         {
            this.closePSIfNotNull(ps);
         }

      }
   }

   private void closePSIfNotNull(PreparedStatement ps) throws SQLException
   {
      if (ps != null)
      {
         ps.close();
      }
   }


   private void syncQNamesInsertPSs(Connection connection, Map<QualifiedName, PreparedStatement> qNameInsertPSs, Set<QualifiedName> qNames) throws SQLException
   {
      Set<QualifiedName> copy = new HashSet<QualifiedName>(qNames);
      copy.removeAll(qNameInsertPSs.keySet());
      for (QualifiedName qName : copy)
      {
         QNameInfo info = this._state.qNameInfos().get().get(qName);
         if (info == null)
         {
            throw new InternalError("Could not find database information about qualified name [" + qName + "]");
         }

         QNameType type = info.getQNameType();
         if (type.equals(QNameType.PROPERTY))
         {
            qNameInsertPSs.put(qName, this.createInsertPropertyPS(connection, info));
         }
         else if (type.equals(QNameType.ASSOCIATION))
         {
            qNameInsertPSs.put(qName, this.createInsertAssociationPS(connection, info));
         }
         else if (type.equals(QNameType.MANY_ASSOCIATION))
         {
            qNameInsertPSs.put(qName, this.createInsertManyAssociationPS(connection, info));
         }
         else
         {
            throw new IllegalArgumentException("Did not know what to do with QName of type " + type + ".");
         }
      }
   }

   private PreparedStatement createInsertPropertyPS(Connection connection, QNameInfo qNameInfo) throws SQLException
   {

      String tableName = qNameInfo.getTableName();
      StringBuilder builder = new StringBuilder();
      builder.append( //
            "INSERT INTO " + this._state.schemaName().get() + "." + tableName + " VALUES(" + "\n" + //
            "?, ?, ?, ");
      if (qNameInfo.getCollectionDepth() > 0)
      {
         builder.append("text2ltree(?), ");
      }
      builder.append("?" + "\n" + ")");

      return connection.prepareStatement(builder.toString());
   }

   private PreparedStatement createInsertAssociationPS(Connection connection, QNameInfo qNameInfo) throws SQLException
   {
      return connection.prepareStatement(String.format(SQLs.THREE_VALUE_INSERT, this._state.schemaName().get(), qNameInfo.getTableName()));
   }

   private PreparedStatement createInsertManyAssociationPS(Connection connection, QNameInfo qNameInfo) throws SQLException
   {
      return connection.prepareStatement(String.format(SQLs.FOUR_VALUE_INSERT, this._state.schemaName().get(), qNameInfo.getTableName()));
   }

   private void clearAllEntitysQNames(PreparedStatement clearPropertiesPS, Long pk) throws SQLException
   {
       clearPropertiesPS.setLong( 1, pk );
       clearPropertiesPS.addBatch( );
   }

   private Integer insertPropertyQNames(Connection connection, Map<QualifiedName, PreparedStatement> qNameInsertPSs, PreparedStatement insertAllQNamesPS, EntityState state, Long entityPK) throws SQLException
   {
      Set<QualifiedName> qNames = this._state.entityUsedQNames().get().get(state.entityDescriptor().type().getName());
      this.syncQNamesInsertPSs(connection, qNameInsertPSs, qNames);
      Integer propertyPK = 0;
      for (PropertyDescriptor pDesc : state.entityDescriptor().state().properties())
      {
         propertyPK = this.insertProperty( //
               qNameInsertPSs, //
               insertAllQNamesPS, //
               propertyPK, //
               entityPK, //
               pDesc.qualifiedName(), //
               state.getProperty(pDesc.qualifiedName()), //
               null //
               );
      }

      return propertyPK;
   }

   private void insertAssoAndManyAssoQNames(Map<QualifiedName, PreparedStatement> qNameInsertPSs, PreparedStatement queryPKPS, PreparedStatement insertToAllQNamesPS, EntityState state, Integer qNamePK, Long entityPK) throws SQLException
   {
      for (AssociationDescriptor aDesc : state.entityDescriptor().state().associations())
      {
         QualifiedName qName = aDesc.qualifiedName();
         PreparedStatement ps = qNameInsertPSs.get(qName);
         EntityReference ref = state.getAssociation(qName);
         if (ref != null)
         {
            queryPKPS.setString(1, ref.identity());
            ResultSet rs = queryPKPS.executeQuery();
            if (rs.next())
            {
               ps.setLong(3, rs.getLong(1));
            }
            else
            {
               throw new EntityNotFoundException(ref);
            }

            insertToAllQNamesPS.setInt( 1, qNamePK );
            insertToAllQNamesPS.setLong( 2, entityPK );
            insertToAllQNamesPS.addBatch( );

            ps.setInt( 1, qNamePK );
            ps.setLong(2, entityPK);
            ps.addBatch();

            ++qNamePK;
         }
      }

      for (ManyAssociationDescriptor mDesc : state.entityDescriptor().state().manyAssociations())
      {
         QualifiedName qName = mDesc.qualifiedName();
         PreparedStatement ps = qNameInsertPSs.get(qName);
         Integer index = 0;
         for (EntityReference ref : state.getManyAssociation(qName))
         {
            if (ref != null)
            {
               queryPKPS.setString(1, ref.identity());
               ResultSet rs = queryPKPS.executeQuery();
               if (rs.next())
               {
                  ps.setLong(4, rs.getLong(1));
               }
               else
               {
                  throw new EntityNotFoundException(ref);
               }

               insertToAllQNamesPS.setInt( 1, qNamePK );
               insertToAllQNamesPS.setLong( 2, entityPK );
               insertToAllQNamesPS.addBatch( );

               ps.setInt( 1, qNamePK );
               ps.setLong(2, entityPK);
               ps.setInt(3, index);
               ps.addBatch();
               ++qNamePK;

            }
            ++index;
         }
      }
   }

   private Integer insertProperty( //
         Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
         PreparedStatement insertAllQNamesPS, //
         Integer propertyPK, //
         Long entityPK, //
         QualifiedName qName, //
         Object property, //
         Integer parentQNameID //
         ) throws SQLException
    {
       Integer result = propertyPK;
        if ( property != null )
        {
            if ( !qName.type( ).equals( Identity.class.getName( ) ) )
            {
                QNameInfo info = this._state.qNameInfos( ).get( ).get( qName );
                if ( info.getCollectionDepth( ) > 0 )
                {
                    result = this.storeCollectionProperty( qNameInsertPSs, insertAllQNamesPS, propertyPK, entityPK, qName, ( Collection<?> ) property, parentQNameID );
                }
                else if ( info.isFinalTypePrimitive( ) )
                {
                    result = this.storePrimitiveProperty( qNameInsertPSs, insertAllQNamesPS, propertyPK, entityPK, qName, property, parentQNameID );
                }
                else
                {
                    result = this.storeValueCompositeProperty( qNameInsertPSs, insertAllQNamesPS, propertyPK, entityPK, qName, property, parentQNameID );
                }
            }
        }
        return result;
    }

   private Integer storeCollectionProperty( //
         Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
         PreparedStatement insertAllQNamesPS, //
         Integer propertyPK, //
         Long entityPK, //
         QualifiedName qName, //
         Collection<?> property, //
         Integer parentQNameID //
         ) throws SQLException
   {
      QNameInfo info = this._state.qNameInfos().get().get(qName);
      PreparedStatement ps = qNameInsertPSs.get(qName);
      propertyPK = this.storeCollectionInfo( insertAllQNamesPS, propertyPK, entityPK, parentQNameID, ps, info );

      propertyPK = this.storeCollectionItems(qNameInsertPSs, property, insertAllQNamesPS, SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME, ps, info.getTableName(), propertyPK, entityPK, parentQNameID, info.getFinalType(), info.isFinalTypePrimitive());

      return propertyPK;
   }

   private Integer storeCollectionInfo(PreparedStatement insertAllQNamesPS, Integer propertyPK, Long entityPK, Integer parentQNameID, PreparedStatement ps, QNameInfo info) throws SQLException
   {
       insertAllQNamesPS.setInt( 1, propertyPK );
       insertAllQNamesPS.setLong( 2, entityPK );
       insertAllQNamesPS.addBatch( );

       ps.setInt(1, propertyPK);
       ps.setLong( 2, entityPK );
       ps.setObject(3, parentQNameID, Types.BIGINT);
       ps.setString( 4, SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME );
       if (info.isFinalTypePrimitive())
       {
          this.storePrimitiveUsingPS(ps, 5, null, info.getFinalType());
       } else
       {
          this.storeVCClassIDUsingPS(ps, 5, null);
       }
       ps.addBatch( );

       return propertyPK + 1;
   }

   private Integer storeCollectionItems(Map<QualifiedName, PreparedStatement> qNameInsertPSs, Collection<?> collection, PreparedStatement insertAllQNamesPS, String path, PreparedStatement ps, String tableName, Integer propertyPK, Long entityPK, Integer parentPK, Type finalType, Boolean isFinalTypePrimitive) throws SQLException
   {
      Integer index = 0;
      for (Object o : collection)
      {
          String itemPath = path + SQLs.QNAME_TABLE_COLLECTION_PATH_SEPARATOR + index;
         if (o instanceof Collection<?>)
         {
            propertyPK = this.storeCollectionItems(qNameInsertPSs, (Collection<?>)o, insertAllQNamesPS, itemPath, ps, tableName, propertyPK, entityPK, parentPK, finalType, isFinalTypePrimitive);
         } else
         {
            propertyPK = this.storeCollectionItem(qNameInsertPSs, ps, insertAllQNamesPS, propertyPK, entityPK, parentPK, itemPath, o, isFinalTypePrimitive, finalType);

            ps.addBatch();
         }
         ++index;
      }
      return propertyPK;
   }

   private Integer storeCollectionItem( //
         Map<QualifiedName, PreparedStatement> qNameInsertPSs,
         PreparedStatement ps, //
         PreparedStatement insertAllQNamesPS, //
         Integer propertyPK, //
         Long entityPK, //
         Integer parentPK, //
         String path, //
         Object item, //
         Boolean isFinalTypePrimitive, //
         Type finalType //
         ) throws SQLException
   {
       insertAllQNamesPS.setInt( 1, propertyPK );
       insertAllQNamesPS.setLong( 2, entityPK );
       insertAllQNamesPS.addBatch( );

      ps.setInt(1, propertyPK);
      ps.setLong( 2, entityPK );
      ps.setObject(3, parentPK, Types.INTEGER);
      ps.setString(4, path);
      if (isFinalTypePrimitive)
      {
         this.storePrimitiveUsingPS(ps, 5, item, finalType);
         ++propertyPK;
      } else
      {
         this.storeVCClassIDUsingPS(ps, 5, item);
         propertyPK = this.storePropertiesOfVC( qNameInsertPSs, insertAllQNamesPS, propertyPK, entityPK, item );
      }

      return propertyPK;
   }

   private Integer storePrimitiveProperty( //
         Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
         PreparedStatement insertAllQNamesPS, //
         Integer propertyPK, //
         Long entityPK, //
         QualifiedName qName, //
         Object property, //
         Integer parentQNameID //
         ) throws SQLException
   {
      QNameInfo info = this._state.qNameInfos().get().get(qName);
      insertAllQNamesPS.setInt( 1, propertyPK );
      insertAllQNamesPS.setLong( 2, entityPK );
      insertAllQNamesPS.addBatch( );

      PreparedStatement ps = qNameInsertPSs.get(qName);
      ps.setInt(1, propertyPK);
      ps.setLong( 2, entityPK );
      ps.setObject(3, parentQNameID, Types.INTEGER);
      Type type = info.getFinalType();
      this.storePrimitiveUsingPS(ps, 4, property, type);
      ps.addBatch();

      return propertyPK + 1;
   }

   private Integer storeValueCompositeProperty( //
         Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
         PreparedStatement insertAllQNamesPS, //
         Integer propertyPK, //
         Long entityPK, //
         QualifiedName qName, //
         Object property, //
         Integer parentQNameID //
         ) throws SQLException
   {

      PreparedStatement ps = qNameInsertPSs.get(qName);
      insertAllQNamesPS.setInt( 1, propertyPK );
      insertAllQNamesPS.setLong( 2, entityPK );
      insertAllQNamesPS.addBatch( );

      ps.setInt(1, propertyPK);
      ps.setLong( 2, entityPK );
      ps.setObject(3, parentQNameID, Types.INTEGER);
      this.storeVCClassIDUsingPS(ps, 4, property);
      ps.addBatch();

      return this.storePropertiesOfVC( qNameInsertPSs, insertAllQNamesPS, propertyPK, entityPK, property );
   }

   private Integer storePropertiesOfVC(
       Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
       PreparedStatement insertAllQNamesPS, //
       Integer propertyPK, //
       Long entityPK, //
       Object property //
       ) throws SQLException
   {
       ValueDescriptor vDesc = this._qi4SPI.getValueDescriptor((ValueComposite) property);
       StateHolder state = ((ValueComposite) property).state();
       Integer originalPropertyPK = propertyPK;
       ++propertyPK;
       for (PropertyDescriptor pDesc : vDesc.state().properties())
       {

          propertyPK = this.insertProperty( //
               qNameInsertPSs, //
               insertAllQNamesPS, //
               propertyPK, //
               entityPK, //
               pDesc.qualifiedName(), //
               state.getProperty(pDesc.qualifiedName()).get(), //
               originalPropertyPK //
               );
       }

       return propertyPK;
   }

   private void storePrimitiveUsingPS(PreparedStatement ps, Integer nextFreeIndex, Object primitive, Type primitiveType) throws SQLException
   {
      if (primitiveType instanceof ParameterizedType)
      {
         primitiveType = ((ParameterizedType)primitiveType).getRawType();
      }

      if (primitiveType instanceof Class<?> && Enum.class.isAssignableFrom((Class<?>)primitiveType))
      {
//         if (primitive == null)
//         {
//            ps.setNull(nextFreeIndex, Types.INTEGER);
//         } else
//         {
            ps.setInt(nextFreeIndex, this._state.enumPKs().get().get(QualifiedName.fromClass((Class<?>)primitiveType, primitive.toString()).toString()));
//         }
      } else
      {
         this._sqlTypeHelper.addPrimitiveToPS(ps, nextFreeIndex, primitive, primitiveType);
      }
   }

   private void storeVCClassIDUsingPS(PreparedStatement ps, Integer nextFreeIndex, Object vc) throws SQLException
   {
      if (vc == null)
      {
         ps.setNull(nextFreeIndex, Types.INTEGER);
      } else
      {
         ValueDescriptor vDesc = this._qi4SPI.getValueDescriptor((ValueComposite)vc);
         String vType = vDesc.type().getName();
         Integer classID = this._state.usedClassesPKs().get().get(vType);
         ps.setInt(nextFreeIndex, classID);
      }
   }

   private Long updateEntityInfoAndProperties(Connection connection, Map<QualifiedName, PreparedStatement> qNameInsertPSs, PreparedStatement insertAllQNamesPS, PreparedStatement clearPropertiesPS, PreparedStatement queryPKPS, PreparedStatement ps, PreparedStatement insertEntityInfoPS, EntityState state, Map<Long, Integer> qNamePKs) throws SQLException
   {
      queryPKPS.setString(1, state.identity().identity());
      ResultSet rs = queryPKPS.executeQuery();
      Long entityPK = null;
      if (rs.next())
      {
         entityPK = rs.getLong(1);
         this.clearAllEntitysQNames(clearPropertiesPS, entityPK);

         // Update state
         ps.setString(1, state.identity().identity());
         ps.setTimestamp(2, new Timestamp(state.lastModified()));
         ps.setString(3, state.version());
         ps.setString(4, this._app.version());
         ps.setLong(5, entityPK);
         ps.addBatch();

         Integer nextUsableQNamePK = this.insertPropertyQNames(connection, qNameInsertPSs, insertAllQNamesPS, state, entityPK);
         qNamePKs.put( entityPK, nextUsableQNamePK );

      } else
      {
         // Most likely re-indexing
         this.insertEntityInfoAndProperties(connection, qNameInsertPSs, insertAllQNamesPS, insertEntityInfoPS, state, qNamePKs);
      }
      return entityPK;
   }

   private Long insertEntityInfoAndProperties(Connection connection, Map<QualifiedName, PreparedStatement> qNameInsertPSs, PreparedStatement insertAllQNamesPS, PreparedStatement ps, EntityState state, Map<Long, Integer> qNamePKs) throws SQLException
   {
      Long entityPK = this.newPK(ENTITY_TABLE_NAME);

      ps.setLong(1, entityPK);
      ps.setInt(2, this._state.entityTypeInfos().get().get(state.entityDescriptor().type().getName()).getEntityTypePK());
      ps.setString(3, state.identity().identity());
      ps.setTimestamp(4, new Timestamp(state.lastModified()));
      ps.setString(5, state.version());
      ps.setString(6, this._app.version());
      ps.addBatch();

      Integer nextQnamePK = this.insertPropertyQNames(connection, qNameInsertPSs, insertAllQNamesPS, state, entityPK);
      qNamePKs.put( entityPK, nextQnamePK );

      return entityPK;
   }

   private void removeEntity(EntityState state, PreparedStatement ps) throws SQLException
   {
      ps.setString(1, state.identity().identity());
      ps.addBatch();
   }

   private Long newPK(String tableName)
   {
      Long result = this._state.tablePKs().get().get(tableName);
      this._state.tablePKs().get().put(tableName, result + 1);
      return result;
   }

}
