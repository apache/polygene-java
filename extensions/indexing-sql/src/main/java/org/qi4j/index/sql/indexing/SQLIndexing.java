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

package org.qi4j.index.sql.indexing;

import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_NAME;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.index.sql.common.QNameInfo;
import org.qi4j.index.sql.common.SQLIndexingState;
import org.qi4j.index.sql.common.SQLTypeHelper;
import org.qi4j.index.sql.common.QNameInfo.QNameType;
import org.qi4j.index.sql.startup.SQLs;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.value.ValueDescriptor;

/**
 * This interface is responsible of providing the indexing-functionality for Qi4j using relational database.
 * Currently it simply extends the {@link StateChangeListener} interface, but in case custom methods will be required with RDB indexing, this interface will be the place for them.
 *
 * @author Stanislav Muhametsin
 */
@Mixins({
   SQLIndexing.SQLIndexingMixin.class
})
public interface SQLIndexing extends StateChangeListener
{
   
   public abstract class SQLIndexingMixin implements StateChangeListener
   {
      
      @Structure
      private Application _app;
      
      @Structure
      private Qi4jSPI _qi4SPI;
      
      @This
      private SQLIndexingState _state;
      
      @This private SQLTypeHelper _sqlTypeHelper;
      
      @Override
      public void notifyChanges(Iterable<EntityState> changedStates)
      {
         try
         {
            this.handleCommittingEntityStates(this._state.connection().get(), changedStates);
         }
         catch (SQLException sqle)
         {
            throw new UnitOfWorkException(sqle);
         }
      }
      
      private void handleCommittingEntityStates(Connection connection, Iterable<EntityState> entityStates) throws SQLException
      {
         Boolean wasAutoCommit = connection.getAutoCommit();
         connection.setAutoCommit(false);
         PreparedStatement insertToEntityTablePS = null;
         PreparedStatement updateEntityTablePS = null;
         PreparedStatement removeEntityPS = null;
         PreparedStatement queryEntityPKPS = null;
         Map<QualifiedName, PreparedStatement> qNameClearPSs = new HashMap<QualifiedName, PreparedStatement>();
         Map<QualifiedName, PreparedStatement> qNameInsertPSs = new HashMap<QualifiedName, PreparedStatement>();
         try
         {
            
            insertToEntityTablePS = connection.prepareStatement(String.format(SQLs.SIX_VALUE_INSERT, this._state.schemaName().get(), ENTITY_TABLE_NAME));
            updateEntityTablePS = connection.prepareStatement(String.format(SQLs.UPDATE_ENTITY_TABLE, this._state.schemaName().get()));
            removeEntityPS = connection.prepareStatement(String.format(SQLs.DELETE_FROM_ENTITY_TABLE, this._state.schemaName().get()));
            queryEntityPKPS = connection.prepareStatement(String.format(SQLs.QUERY_ENTITY_PK_BY_IDENTITY, this._state.schemaName().get()));
            
            Map<Long, EntityState> statesByPK = new HashMap<Long, EntityState>();
            
            for (EntityState eState : entityStates)
            {
               EntityStatus status = eState.status();
               Long pk = null;
               if (status.equals(EntityStatus.NEW))
               {
                  pk = this.insertEntityInfoAndProperties(connection, qNameInsertPSs, insertToEntityTablePS, eState);
               }
               else if (status.equals(EntityStatus.UPDATED))
               {
                  pk = this.updateEntityInfoAndProperties(connection, qNameClearPSs, qNameInsertPSs, queryEntityPKPS, updateEntityTablePS, eState);
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
                  this.insertAssoAndManyAssoQNames(connection, qNameInsertPSs, queryEntityPKPS, eState, pk);
               }
            }
            
            for (PreparedStatement ps : qNameClearPSs.values())
            {
                  ps.executeBatch();
            }
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
            
            for (PreparedStatement ps : qNameClearPSs.values())
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
      
      private void syncQNamesClearPSs(Connection connection, Map<QualifiedName, PreparedStatement> qNameClearPSs, Set<QualifiedName> qNames) throws SQLException
      {
         Set<QualifiedName> copy = new HashSet<QualifiedName>(qNames);
         copy.removeAll(qNameClearPSs.keySet());
         for (QualifiedName qName : copy)
         {
            QNameInfo info = this._state.qNameInfos().get().get(qName);
            qNameClearPSs.put(qName, this.createClearPS(connection, info));
         }
      }
      
      private PreparedStatement createClearPS(Connection connection, QNameInfo qNameInfo) throws SQLException
      {
         String tableName = qNameInfo.getTableName();
         return connection.prepareStatement(String.format(SQLs.CLEAR_ENTITY_DATA, this._state.schemaName().get(), tableName));
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
    
         // First normal table
         // TODO get rid of magic numbers. Probably will be suitable when
         // meta-information syncing between application
         // and DB will be implemented.
         String tableName = qNameInfo.getTableName();
         StringBuilder builder = new StringBuilder();
         builder.append( //
               "INSERT INTO " + this._state.schemaName().get() + "." + tableName + " VALUES(" + "\n" + //
               "?, ?, ?, ");
         if (qNameInfo.getCollectionDepth() > 0)
         {
            for (Integer x = 0; x < qNameInfo.getCollectionDepth(); ++x)
            {
               builder.append("?, ");
            }
         }
         builder.append("?" + "\n" + ")");

         return connection.prepareStatement(builder.toString());
      }
      
      private PreparedStatement createInsertAssociationPS(Connection connection, QNameInfo qNameInfo) throws SQLException
      {
         return connection.prepareStatement(String.format(SQLs.TWO_VALUE_INSERT, this._state.schemaName().get(), qNameInfo.getTableName()));
      }
      
      private PreparedStatement createInsertManyAssociationPS(Connection connection, QNameInfo qNameInfo) throws SQLException
      {         
         return connection.prepareStatement(String.format(SQLs.THREE_VALUE_INSERT, this._state.schemaName().get(), qNameInfo.getTableName()));
      }
      
      private void clearAllEntitysQNames(Connection connection, Map<QualifiedName, PreparedStatement> qNameClearPSs, EntityState state, Long pk) throws SQLException
      {
         Set<QualifiedName> qNames = this._state.entityUsedQNames().get().get(state.entityDescriptor().type().getName());
         this.syncQNamesClearPSs(connection, qNameClearPSs, qNames);
         for (QualifiedName qName : qNames)
         {
            PreparedStatement ps = qNameClearPSs.get(qName);
            ps.setLong(1, pk);
            ps.addBatch();
         }
      }
      
      private void insertPropertyQNames(Connection connection, Map<QualifiedName, PreparedStatement> qNameInsertPSs, EntityState state, Long entityPK) throws SQLException
      {
         Set<QualifiedName> qNames = this._state.entityUsedQNames().get().get(state.entityDescriptor().type().getName());
         this.syncQNamesInsertPSs(connection, qNameInsertPSs, qNames);
         for (PropertyDescriptor pDesc : state.entityDescriptor().state().properties())
         {
            this.insertProperty( //
                  qNameInsertPSs, //
                  entityPK, //
                  pDesc.qualifiedName(), //
                  state.getProperty(pDesc.qualifiedName()), //
                  null //
                  );
         }
      }
      
      private void insertAssoAndManyAssoQNames(Connection connection, Map<QualifiedName, PreparedStatement> qNameInsertPSs, PreparedStatement queryPKPS, EntityState state, Long entityPK) throws SQLException
      {
         for (AssociationDescriptor aDesc : state.entityDescriptor().state().associations())
         {
            QualifiedName qName = aDesc.qualifiedName();
            PreparedStatement ps = qNameInsertPSs.get(qName);
            EntityReference ref = state.getAssociation(qName);
            ps.setLong(1, entityPK);
            if (ref == null)
            {
               ps.setNull(2, Types.BIGINT);
            } else
            {
               queryPKPS.setString(1, ref.identity());
               ResultSet rs = queryPKPS.executeQuery();
               if (rs.next())
               {
                  ps.setLong(2, rs.getLong(1));
               }
               else
               {
                  throw new EntityNotFoundException(ref);
               }
            }
            ps.addBatch();
         }
         
         for (ManyAssociationDescriptor mDesc : state.entityDescriptor().state().manyAssociations())
         {
            QualifiedName qName = mDesc.qualifiedName();
            PreparedStatement ps = qNameInsertPSs.get(qName);
            Integer index = 0;
            for (EntityReference ref : state.getManyAssociation(qName))
            {
               ps.setLong(1, entityPK);
               ps.setInt(2, index);
               if (ref == null)
               {
                  ps.setNull(3, Types.BIGINT);
               }
               else
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
               }
               ps.addBatch();
               ++index;
            }
         }
      }
      
      private void insertProperty( //
            Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
            Long entityPK, //
            QualifiedName qName, //
            Object property, //
            Long parentQNameID //
            ) throws SQLException
      {
         if (!qName.type().equals(Identity.class.getName()))
         {
            QNameInfo info = this._state.qNameInfos().get().get(qName);
            if (info.getCollectionDepth() > 0)
            {
               this.storeCollectionProperty(qNameInsertPSs, entityPK, qName, (Collection<?>)property, parentQNameID);
            } else if (info.isFinalTypePrimitive())
            {
               this.storePrimitiveProperty(qNameInsertPSs, entityPK, qName, property, parentQNameID);
            } else
            {
               this.storeValueCompositeProperty(qNameInsertPSs, entityPK, qName, property, parentQNameID);
            }
         }
      }
      
      private void storeCollectionProperty( //
            Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
            Long entityPK, //
            QualifiedName qName, //
            Collection<?> property, //
            Long parentQNameID //
            ) throws SQLException
      {
         QNameInfo info = this._state.qNameInfos().get().get(qName);
         String tableName = info.getTableName();
         PreparedStatement ps = qNameInsertPSs.get(qName);
         if (property != null)
         {
            List<Integer> indices = new ArrayList<Integer>(info.getCollectionDepth());
            for (Integer x = 0; x < info.getCollectionDepth(); ++x)
            {
               indices.add(null);
            }
            this.storeCollectionItems(property, indices, 0, ps, info.getTableName(), entityPK, parentQNameID, info.getFinalType(), info.isFinalTypePrimitive());
         } else
         {
            Long propertyPK = this.newPK(tableName);
            ps.setLong(1, propertyPK);
            ps.setLong(2, entityPK);
            ps.setObject(3, parentQNameID, Types.BIGINT);
            for (Integer x = 4; x < 4 + info.getCollectionDepth(); ++x)
            {
               ps.setNull(x, Types.INTEGER);
            }
            if (info.isFinalTypePrimitive())
            {
               this.storePrimitiveUsingPS(ps, 4 + info.getCollectionDepth(), null, info.getFinalType());
            } else
            {
               this.storeVCClassIDUsingPS(ps, 4 + info.getCollectionDepth(), null);
            }
            ps.addBatch();
         }

      }
      
      
      private void storeCollectionItems(Collection<?> collection, List<Integer> indices, Integer currentCollectionDepth, PreparedStatement ps, String tableName, Long entityPK, Long parentPK, Type finalType, Boolean isFinalTypePrimitive) throws SQLException
      {
         Integer index = 0;
         for (Object o : collection)
         {
            indices.set(currentCollectionDepth, index);
            if (o instanceof Collection<?>)
            {
               this.storeCollectionItems((Collection<?>)o, indices, currentCollectionDepth + 1, ps, tableName, entityPK, parentPK, finalType, isFinalTypePrimitive);
            } else
            {
               this.storeCollectionItem(ps, tableName, entityPK, parentPK, indices, isFinalTypePrimitive, finalType);

               ps.addBatch();
            }
            ++index;
         }
         if (index == 0)
         {
            // TODO fix: a little hack - -1 as indices, and null as value means empty collection
            indices.set(currentCollectionDepth, -1);
            this.storeCollectionItem(ps, tableName, entityPK, parentPK, indices, isFinalTypePrimitive, finalType);
            ps.addBatch();
         }
         indices.set(currentCollectionDepth, null);
      }
      
      private void storeCollectionItem( //
            PreparedStatement ps, //
            String tableName, //
            Long entityPK, //
            Long parentPK, //
            List<Integer> indices, //
            Boolean isFinalTypePrimitive, //
            Type finalType //
            ) throws SQLException
      {
         ps.setLong(1, this.newPK(tableName));
         ps.setLong(2, entityPK);
         ps.setObject(3, parentPK, Types.BIGINT);
         for (Integer x = 0; x < indices.size(); ++x)
         {
            ps.setObject(x + 4, indices.get(x), Types.INTEGER);
         }
         if (isFinalTypePrimitive)
         {
            this.storePrimitiveUsingPS(ps, indices.size() + 4, null, finalType);
         } else
         {
            this.storeVCClassIDUsingPS(ps, indices.size() + 4, null);
         }
      }
      
      private void storePrimitiveProperty( //
            Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
            Long entityPK, //
            QualifiedName qName, //
            Object property, //
            Long parentQNameID //
            ) throws SQLException
      {
         QNameInfo info = this._state.qNameInfos().get().get(qName);
         String tableName = info.getTableName();
         Long propertyPK = this.newPK(tableName);
         PreparedStatement ps = qNameInsertPSs.get(qName);
         ps.setLong(1, propertyPK);
         ps.setLong(2, entityPK);
         ps.setObject(3, parentQNameID, Types.BIGINT);
         Type type = info.getFinalType();
         this.storePrimitiveUsingPS(ps, 4, property, type);
         ps.addBatch();

      }
      
      private void storeValueCompositeProperty( //
            Map<QualifiedName, PreparedStatement> qNameInsertPSs, //
            Long entityPK, //
            QualifiedName qName, //
            Object property, //
            Long parentQNameID //
            ) throws SQLException
      {
         QNameInfo info = this._state.qNameInfos().get().get(qName);
         String tableName = info.getTableName();
         PreparedStatement ps = qNameInsertPSs.get(qName);
         Long propertyPK = this.newPK(tableName);
         ps.setLong(1, propertyPK);
         ps.setLong(2, entityPK);
         ps.setObject(3, parentQNameID, Types.BIGINT);
         this.storeVCClassIDUsingPS(ps, 4, property);
         ps.addBatch();
         if (property != null)
         {
            ValueDescriptor vDesc = this._qi4SPI.getValueDescriptor((ValueComposite) property);
            StateHolder state = ((ValueComposite) property).state();
            for (PropertyDescriptor pDesc : vDesc.state().properties())
            {
               this.insertProperty( //
                     qNameInsertPSs, //
                     entityPK, //
                     pDesc.qualifiedName(), //
                     state.getProperty(pDesc.qualifiedName()).get(), // 
                     propertyPK //
                     );
            }
         }

      }
      
      private void storePrimitiveUsingPS(PreparedStatement ps, Integer nextFreeIndex, Object primitive, Type primitiveType) throws SQLException
      {
         if (primitiveType instanceof ParameterizedType)
         {
            primitiveType = ((ParameterizedType)primitiveType).getRawType();
         }

         this._sqlTypeHelper.addPrimitiveToPS(ps, nextFreeIndex, primitive, primitiveType);
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
      
      private Long updateEntityInfoAndProperties(Connection connection, Map<QualifiedName, PreparedStatement> qNameClearPSs, Map<QualifiedName, PreparedStatement> qNameInsertPSs, PreparedStatement queryPKPS, PreparedStatement ps, EntityState state) throws SQLException
      {
         queryPKPS.setString(1, state.identity().identity());
         ResultSet rs = queryPKPS.executeQuery();
         Long entityPK = null;
         if (rs.next())
         {
            entityPK = rs.getLong(1);
            this.clearAllEntitysQNames(connection, qNameClearPSs, state, entityPK);
            
            // Update state
            ps.setString(1, state.identity().identity());
            ps.setTimestamp(2, new Timestamp(state.lastModified()));
            ps.setString(3, state.version());
            ps.setString(4, this._app.version());
            ps.setLong(5, entityPK);
            ps.addBatch();
            
            this.insertPropertyQNames(connection, qNameInsertPSs, state, entityPK);
         }
         return entityPK;
      }
      
      private Long insertEntityInfoAndProperties(Connection connection, Map<QualifiedName, PreparedStatement> qNameInsertPSs, PreparedStatement ps, EntityState state) throws SQLException
      {
         Long entityPK = this.newPK(ENTITY_TABLE_NAME);
         
         ps.setLong(1, entityPK);
         ps.setInt(2, this._state.entityTypeInfos().get().get(state.entityDescriptor().type().getName()).getEntityTypePK());
         ps.setString(3, state.identity().identity());
         ps.setTimestamp(4, new Timestamp(state.lastModified()));
         ps.setString(5, state.version());
         ps.setString(6, this._app.version());
         ps.addBatch();
         
         this.insertPropertyQNames(connection, qNameInsertPSs, state, entityPK);
         
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
}