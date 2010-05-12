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

package org.qi4j.index.sql.startup;

import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_COLLECTION_INDEX_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_COLLECTION_INDEX_COLUMN_NAME_PREFIX;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_APPLICATION_VERSION_COLUMN_DATATYPE;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_IDENTITY_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_MODIFIED_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_MODIFIED_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_NAME;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_VERSION_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TABLE_VERSION_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TYPES_TABLE_NAME;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_NAME_PREFIX;
import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.USED_CLASSES_TABLE_CLASS_NAME_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME;
import static org.qi4j.index.sql.startup.SQLs.USED_CLASSES_TABLE_NAME;
import static org.qi4j.index.sql.startup.SQLs.USED_CLASSES_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.startup.SQLs.USED_CLASSES_TABLE_PK_COLUMN_NAME;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Application;
import org.qi4j.index.sql.RDBConfiguration;
import org.qi4j.index.sql.common.EntityTypeInfo;
import org.qi4j.index.sql.common.QNameInfo;
import org.qi4j.index.sql.common.QNameInfoImpl;
import org.qi4j.index.sql.common.SQLIndexingState;
import org.qi4j.index.sql.common.EntityTypeInfo.EntityTypeInfoImpl;
import org.qi4j.index.sql.common.QNameInfo.QNameType;
import org.qi4j.index.sql.query.SQLQueryParser;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.value.ValueDescriptor;

/**
 * This interface is responsible for generating and, in future, synchronizing database schema with the Qi4j application structure.
 * 
 * The general principle is that each qualified name is its own table. There are few fixed tables
 * (table including mandatory (identity, entity version, application version, entity type) information about entities,
 * lookup table for value composite types and their names, lookup table for entity types and their names, etc). All the tables are in one schema.
 * Null values are allowed, and stored, in order to speed up queries, as seen in {@link SQLQueryParser} (otherwise there would be a need to use
 * correlating subqueries, which is potentially very slow).
 * 
 * @author Stanislav Muhametsin
 */
@Mixins({
   SQLStartup.SQLStartupMixin.class
})
public interface SQLStartup extends Activatable
{
   
   public class SQLStartupMixin implements SQLStartup
   {
      
      @Structure
      private Application _app;
      
      @This
      private SQLIndexingState _state;
      
      @This
      private Configuration<RDBConfiguration> _configuration;
      
      private Map<Class<?>, String> _creationTypeStrings;
      
      private static final String DEFAULT_SCHEMA_NAME = "qi4j";
      
      private void initTypes()
      {
         Map<Class<?>, Integer> primitiveTypes = new HashMap<Class<?>, Integer>();
         primitiveTypes.put(Boolean.class, Types.BOOLEAN);
         primitiveTypes.put(Byte.class, Types.SMALLINT);
         primitiveTypes.put(Short.class, Types.INTEGER);
         primitiveTypes.put(Integer.class, Types.INTEGER);
         primitiveTypes.put(Long.class, Types.BIGINT);
         primitiveTypes.put(Float.class, Types.REAL);
         primitiveTypes.put(Double.class, Types.DOUBLE);
         primitiveTypes.put(Date.class, Types.TIMESTAMP);
         primitiveTypes.put(Character.class, Types.INTEGER);
         primitiveTypes.put(String.class, Types.VARCHAR);
         primitiveTypes.put(Enum.class, Types.VARCHAR);
         
         this._state.javaTypes2SQLTypes().set(primitiveTypes);
         
         // TODO make use of Qi4j's MinLength/MaxLength -styled constraints
         this._creationTypeStrings = new HashMap<Class<?>, String>();
         this._creationTypeStrings.put(Boolean.class, "BOOLEAN");
         this._creationTypeStrings.put(Byte.class, "SMALLINT");
         this._creationTypeStrings.put(Short.class, "INTEGER");
         this._creationTypeStrings.put(Integer.class, "INTEGER");
         this._creationTypeStrings.put(Long.class, "BIGINT");
         this._creationTypeStrings.put(Float.class, "REAL");
         this._creationTypeStrings.put(Double.class, "DOUBLE PRECISION");
         this._creationTypeStrings.put(Date.class, "TIMESTAMP");
         this._creationTypeStrings.put(Character.class, "INTEGER");
         this._creationTypeStrings.put(String.class, "VARCHAR(10240)");
         this._creationTypeStrings.put(Enum.class, "VARCHAR(1024)");
      }
      
      @Override
      public void activate() throws Exception
      {
         this.initTypes();
         
         Connection connection = DriverManager.getConnection(this._configuration.configuration().connectionString().get());
         connection.setAutoCommit(false);
         
         this._state.connection().set(connection);
         
         this.initConnection();
      }
      
      @Override
      public void passivate() throws Exception
      {
         
      }
      
      private void initConnection() throws SQLException
      {
         Connection connection = this._state.connection().get();
         String schemaName = this._configuration.configuration().schemaName().get();
         if (schemaName == null)
         {
            schemaName = DEFAULT_SCHEMA_NAME;
         }
         
         this._state.schemaName().set(schemaName);
         this._state.tablePKs().set(new HashMap<String, Long>());
         this._state.usedClassesPKs().set(new HashMap<String, Integer>());
         this._state.entityTypeInfos().set(new HashMap<String, EntityTypeInfo>());
         this._state.entityUsedQNames().set(new HashMap<String, Set<QualifiedName>>());
         this._state.qNameInfos().set(new HashMap<QualifiedName, QNameInfo>());
         
         Set<String> usedClassNames = new HashSet<String>();
         List<EntityDescriptor> entityDescriptors = new ArrayList<EntityDescriptor>();
         this.constructApplicationInfo((ApplicationSPI) this._app, entityDescriptors, this._state.qNameInfos().get(), this._state.entityUsedQNames().get(), usedClassNames);
         
         Boolean wasAutoCommit = connection.getAutoCommit();
         connection.setAutoCommit(true);
         try
         {
            this.createFixedTablesIfNeeded(connection);
            this.syncFixedTables(connection, entityDescriptors, usedClassNames);
            this.createQNameTables(connection);
         }
         finally
         {
            connection.setAutoCommit(wasAutoCommit);
         }
         
      }
      
      private void constructApplicationInfo(ApplicationSPI appSPI, final List<EntityDescriptor> entityDescriptors, Map<QualifiedName, QNameInfo> qNameInfos, Map<String, Set<QualifiedName>> entityUsedQNames, Set<String> usedClassNames) throws SQLException
      {
         final List<ValueDescriptor> valueDescriptors = new ArrayList<ValueDescriptor>();
         appSPI.visitDescriptor(new DescriptorVisitor()
         {
            @Override
            public void visit(EntityDescriptor entityDescriptor)
            {
               if (!ConfigurationComposite.class.isAssignableFrom(entityDescriptor.type()))
               {
                  entityDescriptors.add(entityDescriptor);
               }
            }
            
            @Override
            public void visit(ValueDescriptor valueDescriptor)
            {
               valueDescriptors.add(valueDescriptor);
            }
            
         });
         
         Set<String> usedVCClassNames = new HashSet<String>();
         for (EntityDescriptor descriptor : entityDescriptors)
         {
            Set<QualifiedName> newQNames = new HashSet<QualifiedName>();
            this.extractPropertyQNames(descriptor, qNameInfos, newQNames, valueDescriptors, usedVCClassNames);
            this.extractAssociationQNames(descriptor, qNameInfos, newQNames);
            this.extractManyAssociationQNames(descriptor, qNameInfos, newQNames);
            entityUsedQNames.put(descriptor.type().getName(), newQNames);
         }
         
         usedClassNames.addAll(usedVCClassNames);
      }
      
      private void processPropertyTypeForQNames(PropertyDescriptor pType, Map<QualifiedName, QNameInfo> qNameInfos, Set<QualifiedName> newQNames, List<ValueDescriptor> vDescriptors, Set<String> usedVCClassNames)
      {
         QualifiedName qName = pType.qualifiedName();
         if (!newQNames.contains(qName) && !qName.typeName().name().equals(Identity.class.getName()))
         {
            newQNames.add(qName);
//            System.out.println("QName: " + qName + ", hc: " + qName.hashCode());
            Type vType = pType.type();
            List<Class<?>> collectionClasses = new ArrayList<Class<?>>();
            while (vType instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) vType).getRawType()))
            {
               collectionClasses.add((Class<?>) ((ParameterizedType) vType).getRawType());
               vType = ((ParameterizedType) vType).getActualTypeArguments()[0];
            }
            
            if (!qNameInfos.containsKey(qName))
            {
               QNameInfoImpl info = (QNameInfoImpl) QNameInfoImpl.fromProperty(qName, collectionClasses, QNAME_TABLE_NAME_PREFIX + qNameInfos.size(), vType);
               qNameInfos.put(qName, info);
            }
            
            while(vType instanceof ParameterizedType)
            {
               vType = ((ParameterizedType)vType).getRawType();
            }
            if (vType instanceof Class<?> && ((Class<?>)vType).isInterface()) //
            {
               for (ValueDescriptor vDesc : vDescriptors)
               {
                  String vcTypeName = vDesc.type().getName();
                  // TODO this doesn't understand, say, Map<String, String>, or indeed, any other Serializable
                  if (((Class<?>) vType).isAssignableFrom(vDesc.type()))
                  {
                     usedVCClassNames.add(vcTypeName);
                     for (PropertyDescriptor subPDesc : vDesc.state().properties())
                     {
                        this.processPropertyTypeForQNames( //
                              subPDesc, //
                              qNameInfos, //
                              newQNames, //
                              vDescriptors, //
                              usedVCClassNames //
                              );
                     }
                  }
               }
            }
            
         }
      }
      
      private void extractPropertyQNames(EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> qNameInfos, Set<QualifiedName> newQNames, List<ValueDescriptor> vDescriptors, Set<String> usedVCClassNames)
      {
         for (PropertyDescriptor pDesc : entityDesc.state().properties())
         {
            this.processPropertyTypeForQNames( //
                  pDesc, //
                  qNameInfos, //
                  newQNames, //
                  vDescriptors, //
                  usedVCClassNames //
                  );
         }
         
      }
      
      private void extractAssociationQNames(EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> extractedQNames, Set<QualifiedName> newQNames)
      {
         for (AssociationDescriptor assoDesc : entityDesc.state().associations())
         {
            QualifiedName qName = assoDesc.qualifiedName();
            if (!extractedQNames.containsKey(qName))
            {
               extractedQNames.put(qName,//
                     QNameInfoImpl.fromAssociation( //
                           qName, //
                           QNAME_TABLE_NAME_PREFIX + extractedQNames.size(), //
                           assoDesc.type()//
                           ) //
                     );
               newQNames.add(qName);
            }
         }
      }
      
      private void extractManyAssociationQNames(EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> extractedQNames, Set<QualifiedName> newQNames)
      {
         for (ManyAssociationDescriptor mAssoDesc : entityDesc.state().manyAssociations())
         {
            QualifiedName qName = mAssoDesc.qualifiedName();
            if (!extractedQNames.containsKey(qName))
            {
               extractedQNames.put( //
                     qName, //
                     QNameInfoImpl.fromManyAssociation(qName, QNAME_TABLE_NAME_PREFIX + extractedQNames.size(), mAssoDesc.type()));
               newQNames.add(qName);
            }
         }
      }
      
      private void createFixedTablesIfNeeded(Connection connection) throws SQLException
      {
         Statement stmt = connection.createStatement();
         try
         {
            // TODO remove this and implement cache merging/updating
            ResultSet rs = connection.getMetaData().getSchemas();
            try
            {
               while (rs.next())
               {
                  if (rs.getString(1).equals(this._state.schemaName().get()))
                  {
                     stmt.execute("DROP SCHEMA " + this._state.schemaName().get() + " CASCADE;");
                     break;
                  }
               }
            }
            finally
            {
               rs.close();
            }
            
            stmt.execute("CREATE SCHEMA " + this._state.schemaName().get() + ";");
            
            stmt.execute( //
                  "CREATE TABLE " + this._state.schemaName().get() + "." + USED_CLASSES_TABLE_NAME + "(" + "\n" + //
                        USED_CLASSES_TABLE_PK_COLUMN_NAME + " " + USED_CLASSES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME + " " + USED_CLASSES_TABLE_CLASS_NAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        "PRIMARY KEY(" + USED_CLASSES_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                        "UNIQUE(" + USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME + ")" + "\n" + //
                        ");" //
                  );
            
            this._state.tablePKs().get().put(USED_CLASSES_TABLE_NAME, 0L);
            
            stmt.execute( //
                  "CREATE TABLE " + this._state.schemaName().get() + "." + ENTITY_TYPES_TABLE_NAME + "(" + "\n" + //
                        ENTITY_TYPES_TABLE_PK_COLUMN_NAME + " " + ENTITY_TYPES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME + " " + ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        "PRIMARY KEY(" + ENTITY_TYPES_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                        "UNIQUE(" + ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME + ")" + "\n" + //
                        ");");
            this._state.tablePKs().get().put(ENTITY_TYPES_TABLE_NAME, 0L);
            
            stmt.execute( //
                  "CREATE TABLE " + this._state.schemaName().get() + "." + ENTITY_TABLE_NAME + "(" + "\n" + //
                        ENTITY_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        ENTITY_TYPES_TABLE_PK_COLUMN_NAME + " " + ENTITY_TYPES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL, " + "\n" + //
                        ENTITY_TABLE_IDENTITY_COLUMN_NAME + " " + ENTITY_TABLE_IDENTITY_COLUMN_DATA_TYPE + " UNIQUE NOT NULL, " + "\n" + //
                        ENTITY_TABLE_MODIFIED_COLUMN_NAME + " " + ENTITY_TABLE_MODIFIED_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        ENTITY_TABLE_VERSION_COLUMN_NAME + " " + ENTITY_TABLE_VERSION_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME + " " + ENTITY_TABLE_APPLICATION_VERSION_COLUMN_DATATYPE + "," + "\n" + //
                        "PRIMARY KEY(" + ENTITY_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                        "FOREIGN KEY(" + ENTITY_TYPES_TABLE_PK_COLUMN_NAME + ") REFERENCES " + this._state.schemaName().get() + "." + ENTITY_TYPES_TABLE_NAME + "(" + ENTITY_TYPES_TABLE_PK_COLUMN_NAME + ") ON DELETE RESTRICT ON UPDATE CASCADE" + "\n" + //  
                        ");" //
                  );
            this._state.tablePKs().get().put(ENTITY_TABLE_NAME, 0L);
            
            // TODO INDICES!!!!
            
         }
         finally
         {
            stmt.close();
         }
      }
      
      private void syncFixedTables(Connection connection, List<EntityDescriptor> entityDescriptors, Set<String> usedClassNames) throws SQLException
      {
         // TODO here query and fill maps with existing information
         
         PreparedStatement ps = connection.prepareStatement(String.format(SQLs.TWO_VALUE_INSERT, this._state.schemaName().get(), ENTITY_TYPES_TABLE_NAME));
         try
         {
            for (EntityDescriptor descriptor : entityDescriptors)
            {
               String entityTypeName = descriptor.type().getName();
               if (!this._state.entityTypeInfos().get().containsKey(entityTypeName))
               {
                  long pk = this._state.tablePKs().get().get(ENTITY_TYPES_TABLE_NAME);
                  ps.setInt(1, (int) pk);
                  ps.setString(2, entityTypeName);
                  ps.executeUpdate();
                  this._state.entityTypeInfos().get().put(entityTypeName, new EntityTypeInfoImpl(descriptor, (int) pk));
                  this._state.tablePKs().get().put(ENTITY_TYPES_TABLE_NAME, pk + 1);
               }
            }
         }
         finally
         {
            ps.close();
         }
         
         ps = connection.prepareStatement(String.format(SQLs.TWO_VALUE_INSERT, this._state.schemaName().get(), USED_CLASSES_TABLE_NAME));
         try
         {
            for (String usedClass : usedClassNames)
            {
               if (!this._state.usedClassesPKs().get().containsKey(usedClass))
               {
                  long pk = this._state.tablePKs().get().get(USED_CLASSES_TABLE_NAME);
                  ps.setInt(1, (int) pk);
                  ps.setString(2, usedClass);
                  ps.executeUpdate();
                  this._state.usedClassesPKs().get().put(usedClass, (int) pk);
                  this._state.tablePKs().get().put(USED_CLASSES_TABLE_NAME, pk + 1);
               }
            }
         }
         finally
         {
            ps.close();
         }
      }
      
      private void createQNameTables(Connection connection) throws SQLException
      {
         Statement stmt = connection.createStatement();
         try
         {
            for (QNameInfo qNameInfo : this._state.qNameInfos().get().values())
            {
               StringBuilder builder = new StringBuilder();
               QNameType type = qNameInfo.getQNameType();
               
               builder.append( //
                     "CREATE TABLE " + this._state.schemaName().get() + "." + qNameInfo.getTableName() + "(" + "\n" //
                     );

               if (type.equals(QNameType.PROPERTY))
               {
                  Boolean usedClassFK = false;
                  
                  builder.append(QNAME_TABLE_PK_COLUMN_NAME + " " + QNAME_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        ENTITY_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + " " + QNAME_TABLE_PK_COLUMN_DATA_TYPE + "," + "\n" //
                  );
                  
                  if (qNameInfo.getCollectionDepth() > 0)
                  {
                     for (Integer x = 0; x < qNameInfo.getCollectionDepth(); ++x)
                     {
                        builder.append(QNAME_TABLE_COLLECTION_INDEX_COLUMN_NAME_PREFIX + x + " " + QNAME_TABLE_COLLECTION_INDEX_COLUMN_DATA_TYPE + "," + "\n");
                     }
                  }
                  usedClassFK = this.appendColumnDefinitionsForProperty(builder, qNameInfo);
                  this.appendFKsForProperty(builder, QNAME_TABLE_PK_COLUMN_NAME, usedClassFK);
               }
               else if (type.equals(QNameType.ASSOCIATION))
               {
                  builder.append( //
                        ENTITY_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                              QNAME_TABLE_VALUE_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + "," + "\n" + //
                              "PRIMARY KEY(" + ENTITY_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                              "FOREIGN KEY(" + QNAME_TABLE_VALUE_COLUMN_NAME + ") REFERENCES " + this._state.schemaName().get() + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED," + "\n" //
                        );
               }
               else if (type.equals(QNameType.MANY_ASSOCIATION))
               {
                  builder.append( //
                        ENTITY_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                              QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME + " " + QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                              QNAME_TABLE_VALUE_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + "," + "\n" + //
                              "PRIMARY KEY(" + ENTITY_TABLE_PK_COLUMN_NAME + ", " + QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME + ")," + "\n" + //
                              "FOREIGN KEY(" + QNAME_TABLE_VALUE_COLUMN_NAME + ") REFERENCES " + this._state.schemaName().get() + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED," + "\n" //
                        );
               }
               else
               {
                  throw new IllegalArgumentException("Did not how to create table for qName type: " + type + ".");
               }
               builder.append( //
                     "FOREIGN KEY(" + ENTITY_TABLE_PK_COLUMN_NAME + ") REFERENCES " + this._state.schemaName().get() + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED" + "\n" + //
                           ");" //
                     );
               stmt.execute(builder.toString());
               this._state.tablePKs().get().put(qNameInfo.getTableName(), 0L);

            }
         }
         finally
         {
            stmt.close();
         }
      }
      
      private Boolean appendColumnDefinitionsForProperty(StringBuilder builder, QNameInfo qNameInfo)
      {
         Type finalType = qNameInfo.getFinalType();
         if (finalType instanceof ParameterizedType)
         {
            finalType = ((ParameterizedType)finalType).getRawType();
         }
         Class<?> finalClass = (Class<?>)finalType;
         Boolean usedClassFK = false;
         String sqlType = null;
         if (qNameInfo.isFinalTypePrimitive())
         {
            // Primitive type
            for (Map.Entry<Class<?>, String> entry : this._creationTypeStrings.entrySet())
            {
               if (entry.getKey().isAssignableFrom(finalClass))
               {
                  sqlType = entry.getValue();
                  break;
               }
            }
            if (sqlType == null)
            {
               throw new InternalError("Could not find sql type for java type [" + finalType + "]");
            }
         }
         else
         {
            // Value composite - just need used class
            sqlType = USED_CLASSES_TABLE_PK_COLUMN_DATA_TYPE;
            usedClassFK = true;
         }
         
         builder.append(//
               QNAME_TABLE_VALUE_COLUMN_NAME + " " + sqlType + "," + "\n" //
               );
         
         return usedClassFK;
      }
      
      private void appendFKsForProperty(StringBuilder builder, String primaryKeyColumnName, Boolean usedClassFK)
      {
         builder.append("PRIMARY KEY(" + primaryKeyColumnName + ")," + "\n");
         if (usedClassFK)
         {
            // Foreign key constraint for used classes
            builder.append( //
               "FOREIGN KEY(" + QNAME_TABLE_VALUE_COLUMN_NAME + ") REFERENCES " + this._state.schemaName().get() + "." + USED_CLASSES_TABLE_NAME + "(" + USED_CLASSES_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE RESTRICT NOT DEFERRABLE," + "\n" //
               );
         }
      }
      
   }
}