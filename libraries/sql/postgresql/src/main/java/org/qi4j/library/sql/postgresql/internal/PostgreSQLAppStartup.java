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

import static org.qi4j.library.sql.postgresql.internal.SQLs.APP_VERSION_PK_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.APP_VERSION_PK_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.APP_VERSION_TABLE_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_APPLICATION_VERSION_COLUMN_DATATYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_IDENTITY_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_MODIFIED_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_MODIFIED_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_PK_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_VERSION_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TABLE_VERSION_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TYPES_TABLE_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_COLLECTION_INDEX_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_COLLECTION_INDEX_COLUMN_NAME_PREFIX;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_NAME_PREFIX;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_PK_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_CLASSES_TABLE_CLASS_NAME_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_CLASSES_TABLE_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_CLASSES_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_CLASSES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_QNAMES_TABLE_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_QNAMES_TABLE_QNAME_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_QNAMES_TABLE_QNAME_COLUMN_NAME;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_QNAMES_TABLE_TABLE_NAME_COLUMN_DATA_TYPE;
import static org.qi4j.library.sql.postgresql.internal.SQLs.USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME;

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
import java.util.logging.Logger;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Application;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.library.sql.api.SQLAppStartup;
import org.qi4j.library.sql.common.EntityTypeInfo;
import org.qi4j.library.sql.common.QNameInfo;
import org.qi4j.library.sql.common.ReindexingStrategy;
import org.qi4j.library.sql.common.QNameInfo.QNameType;
import org.qi4j.library.sql.postgresql.PostgreSQLConfiguration;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.value.ValueDescriptor;

/**
 *
 * @author Stanislav Muhametsin
 */
public class PostgreSQLAppStartup implements SQLAppStartup
{
   @Structure
   private Application _app;
   
   @This
   private PostgreSQLDBState _state;
   
   @This
   private Configuration<PostgreSQLConfiguration> _configuration;
   
   @Service private ReindexingStrategy _reindexingStrategy;
   
   @Service private Reindexer _reindexer;
   
   private Map<Class<?>, String> _creationTypeStrings;
   
   public static final String DEFAULT_SCHEMA_NAME = "qi4j";
   
   private static final Logger _log = Logger.getLogger(PostgreSQLAppStartup.class.getName());
   
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
   public Connection createConnection() throws SQLException
   {
      this.initTypes();
      
      Connection connection = DriverManager.getConnection(this._configuration.configuration().connectionString().get());
      connection.setAutoCommit(false);
      
      this._state.connection().set(connection);
      
      return connection;
   }
   
   @Override
   public void initConnection(Connection connection) throws SQLException
   {
      String schemaName = this._configuration.configuration().schemaName().get();
      if (schemaName == null)
      {
         schemaName = DEFAULT_SCHEMA_NAME;
      } else
      {
         // TODO check that schema name does not contain any nastyness such as sql injections etc
      }

      this._state.schemaName().set(schemaName);
      this._state.tablePKs().set(new HashMap<String, Long>());
      this._state.usedClassesPKs().set(new HashMap<String, Integer>());
      this._state.entityTypeInfos().set(new HashMap<String, EntityTypeInfo>());
      this._state.entityUsedQNames().set(new HashMap<String, Set<QualifiedName>>());
      this._state.qNameInfos().set(new HashMap<QualifiedName, QNameInfo>());
      
      Boolean wasAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(true);
      try
      {
        this.syncDB();
      }
      finally
      {
         connection.setAutoCommit(wasAutoCommit);
      }
      
   }
   
   private void constructApplicationInfo(final Map<String, EntityDescriptor> entityDescriptors, Set<String> usedClassNames, Boolean setQNameTableNameToNull) throws SQLException
   {
      final List<ValueDescriptor> valueDescriptors = new ArrayList<ValueDescriptor>();
      ApplicationSPI appSPI = (ApplicationSPI)this._app;
      appSPI.visitDescriptor(new DescriptorVisitor()
      {
         @Override
         public void visit(EntityDescriptor entityDescriptor)
         {
//            if (!ConfigurationComposite.class.isAssignableFrom(entityDescriptor.type()))
//            {
               entityDescriptors.put(entityDescriptor.type().getName(), entityDescriptor);
//            }
         }
         
         @Override
         public void visit(ValueDescriptor valueDescriptor)
         {
            valueDescriptors.add(valueDescriptor);
         }
         
      });
      
      Set<String> usedVCClassNames = new HashSet<String>();
      for (EntityDescriptor descriptor : entityDescriptors.values())
      {
         Set<QualifiedName> newQNames = new HashSet<QualifiedName>();
         this.extractPropertyQNames(descriptor, this._state.qNameInfos().get(), newQNames, valueDescriptors, usedVCClassNames, setQNameTableNameToNull);
         this.extractAssociationQNames(descriptor, this._state.qNameInfos().get(), newQNames, setQNameTableNameToNull);
         this.extractManyAssociationQNames(descriptor, this._state.qNameInfos().get(), newQNames, setQNameTableNameToNull);
         this._state.entityUsedQNames().get().put(descriptor.type().getName(), newQNames);
      }
      
      usedClassNames.addAll(usedVCClassNames);
   }
   
   private void processPropertyTypeForQNames(PropertyDescriptor pType, Map<QualifiedName, QNameInfo> qNameInfos, Set<QualifiedName> newQNames, List<ValueDescriptor> vDescriptors, Set<String> usedVCClassNames, Boolean setQNameTableNameToNull)
   {
      QualifiedName qName = pType.qualifiedName();
      if (!newQNames.contains(qName) && !qName.typeName().name().equals(Identity.class.getName()))
      {
         newQNames.add(qName);
//         System.out.println("QName: " + qName + ", hc: " + qName.hashCode());
         Type vType = pType.type();
         List<Class<?>> collectionClasses = new ArrayList<Class<?>>();
         while (vType instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) vType).getRawType()))
         {
            collectionClasses.add((Class<?>) ((ParameterizedType) vType).getRawType());
            vType = ((ParameterizedType) vType).getActualTypeArguments()[0];
         }
         
         if (!qNameInfos.containsKey(qName))
         {
            QNameInfo info = QNameInfo.fromProperty(qName, collectionClasses, setQNameTableNameToNull ? null : (QNAME_TABLE_NAME_PREFIX + qNameInfos.size()), vType);
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
                           usedVCClassNames, //
                           setQNameTableNameToNull //
                           );
                  }
               }
            }
         }
         
      }
   }
   
   private void extractPropertyQNames(EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> qNameInfos, Set<QualifiedName> newQNames, List<ValueDescriptor> vDescriptors, Set<String> usedVCClassNames, Boolean setQNameTableNameToNull)
   {
      for (PropertyDescriptor pDesc : entityDesc.state().properties())
      {
         this.processPropertyTypeForQNames( //
               pDesc, //
               qNameInfos, //
               newQNames, //
               vDescriptors, //
               usedVCClassNames, //
               setQNameTableNameToNull //
               );
      }
      
   }
   
   private void extractAssociationQNames(EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> extractedQNames, Set<QualifiedName> newQNames, Boolean setQNameTableNameToNull)
   {
      for (AssociationDescriptor assoDesc : entityDesc.state().associations())
      {
         QualifiedName qName = assoDesc.qualifiedName();
         if (!extractedQNames.containsKey(qName))
         {
            extractedQNames.put(qName,//
                  QNameInfo.fromAssociation( //
                        qName, //
                        setQNameTableNameToNull ? null : (QNAME_TABLE_NAME_PREFIX + extractedQNames.size()), //
                        assoDesc.type()//
                        ) //
                  );
            newQNames.add(qName);
         }
      }
   }
   
   private void extractManyAssociationQNames(EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> extractedQNames, Set<QualifiedName> newQNames, Boolean setQNameTableNameToNull)
   {
      for (ManyAssociationDescriptor mAssoDesc : entityDesc.state().manyAssociations())
      {
         QualifiedName qName = mAssoDesc.qualifiedName();
         if (!extractedQNames.containsKey(qName))
         {
            extractedQNames.put( //
                  qName, //
                  QNameInfo.fromManyAssociation( //
                        qName, //
                        setQNameTableNameToNull ? null : (QNAME_TABLE_NAME_PREFIX + extractedQNames.size()), //
                        mAssoDesc.type() //
                        ) //
                  );
            newQNames.add(qName);
         }
      }
   }
   
   private Boolean syncDB() throws SQLException
   {
      Connection connection = this._state.connection().get();
      String schemaName = this._state.schemaName().get();
      ResultSet rs = connection.getMetaData().getSchemas();
      
      Boolean schemaFound = false;
      try
      {
         while (rs.next() && !schemaFound)
         {
            schemaFound = rs.getString(1).equals(schemaName);
         }
      }
      finally
      {
         rs.close();
      }
      
      Boolean reindexingRequired = this.isReindexingNeeded(schemaFound);
      Map<String, EntityDescriptor> entityDescriptors = new HashMap<String, EntityDescriptor>();
      Set<String> usedClassNames = new HashSet<String>();
      if (schemaFound && !reindexingRequired)
      {
         this.constructApplicationInfo(entityDescriptors, usedClassNames, true);
         this.readAppMetadataFromDB(entityDescriptors);
      } else 
      {
         this.constructApplicationInfo(entityDescriptors, usedClassNames, false);
         this.createSchema();
         this.writeAppMetadataToDB(entityDescriptors, usedClassNames);
         
         if (reindexingRequired)
         {
            this.performReindex();
         }
      }
      
      return reindexingRequired;
   }
   
   private Boolean isReindexingNeeded(Boolean schemaExists) throws SQLException
   {
      Boolean result = false;
      if (schemaExists)
      {
         Connection connection = this._state.connection().get();
         String schemaName = this._state.schemaName().get();
         Statement stmt = connection.createStatement();
         try
         {
            ResultSet rs = stmt.executeQuery("SELECT " + APP_VERSION_TABLE_NAME + "." + APP_VERSION_PK_COLUMN_NAME + " FROM " + schemaName + "." + APP_VERSION_TABLE_NAME);
            
            if (rs.next())
            {
               String dbAppVersion = rs.getString(1);
               if (this._reindexingStrategy != null)
               {
                  result = this._reindexingStrategy.reindexingNeeded(dbAppVersion, this._app.version());
               }
            } else
            {
               // If nothing present in that table - something is broken, and
               // so we should re-index.
               result = true;
            }
            
            if (schemaExists && result)
            {
               stmt.execute("DROP SCHEMA " + schemaName + " CASCADE");
            }
         }
         finally
         {
            stmt.close();
         }
      }
      
      return result;
   }
   
   private void createSchema() throws SQLException
   {
      Connection connection = this._state.connection().get();
      String schemaName = this._state.schemaName().get();
      
      Statement stmt = connection.createStatement();
      try
      {
         stmt.execute("CREATE SCHEMA " + schemaName + ";");
         
         stmt.execute( //
               "CREATE TABLE " + schemaName + "." + USED_CLASSES_TABLE_NAME + "(" + "\n" + //
                     USED_CLASSES_TABLE_PK_COLUMN_NAME + " " + USED_CLASSES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                     USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME + " " + USED_CLASSES_TABLE_CLASS_NAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                     "PRIMARY KEY(" + USED_CLASSES_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                     "UNIQUE(" + USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME + ")" + "\n" + //
                     ");" //
               );
         
         this._state.tablePKs().get().put(USED_CLASSES_TABLE_NAME, 0L);
         
         stmt.execute( //
               "CREATE TABLE " + schemaName + "." + ENTITY_TYPES_TABLE_NAME + "(" + "\n" + //
                     ENTITY_TYPES_TABLE_PK_COLUMN_NAME + " " + ENTITY_TYPES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                     ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME + " " + ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                     "PRIMARY KEY(" + ENTITY_TYPES_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                     "UNIQUE(" + ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME + ")" + "\n" + //
                     ");");
         this._state.tablePKs().get().put(ENTITY_TYPES_TABLE_NAME, 0L);
         
         stmt.execute( //
               "CREATE TABLE " + schemaName + "." + ENTITY_TABLE_NAME + "(" + "\n" + //
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
         
         stmt.execute( //
               "CREATE TABLE " + schemaName + "." + USED_QNAMES_TABLE_NAME + "(" + "\n" + //
               USED_QNAMES_TABLE_QNAME_COLUMN_NAME + " " + USED_QNAMES_TABLE_QNAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
               USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME + " " + USED_QNAMES_TABLE_TABLE_NAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
               "PRIMARY KEY(" + USED_QNAMES_TABLE_QNAME_COLUMN_NAME + ", " + USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME + ")" + "\n" + //
               ");" //
               );

         stmt.execute(
               "CREATE TABLE " + schemaName + "." + APP_VERSION_TABLE_NAME + "(" + "\n" + //
               APP_VERSION_PK_COLUMN_NAME + " " + APP_VERSION_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
               "PRIMARY KEY(" + APP_VERSION_PK_COLUMN_NAME + ")" + "\n" + //
               ");" //
               );
         
         PreparedStatement ps = connection.prepareStatement("INSERT INTO " + schemaName + "." + APP_VERSION_TABLE_NAME + " VALUES ( ? );");
         ps.setString(1, this._app.version());
         ps.execute();
         
         // TODO INDICES!!!!
      }
      finally
      {
         stmt.close();
      }
   }
   
   private void performReindex() throws SQLException
   {
      _log.info("Performing reindexing...");
      this._reindexer.reindex();
      _log.info("Reindexing complete.");
   }
   
   private void readAppMetadataFromDB(Map<String, EntityDescriptor> entityDescriptors) throws SQLException
   {

      String schemaName = this._state.schemaName().get();
      Connection connection = this._state.connection().get();
      Statement stmt = connection.createStatement();
      try
      {
         ResultSet rs = stmt.executeQuery(String.format(SQLs.ONE_VALUE_SELECT, "MAX(" + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ")", schemaName, SQLs.ENTITY_TABLE_NAME));
         long pk = 0L;
         if (rs.next())
         {
            pk = rs.getLong(1) + 1;
         }
         this._state.tablePKs().get().put(ENTITY_TABLE_NAME, pk);
         
         rs = stmt.executeQuery(String.format(SQLs.TWO_VALUE_SELECT, SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME, SQLs.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME, schemaName, SQLs.ENTITY_TYPES_TABLE_NAME));
         while (rs.next())
         {
            pk = rs.getInt(1);
            String entityTypeName = rs.getString(2);
            this._state.entityTypeInfos().get().put(entityTypeName, new EntityTypeInfo(entityDescriptors.get(entityTypeName), (int)pk));
            
            if (!this._state.tablePKs().get().containsKey(ENTITY_TYPES_TABLE_NAME) || this._state.tablePKs().get().get(ENTITY_TYPES_TABLE_NAME) <= pk)
            {
               this._state.tablePKs().get().put(ENTITY_TYPES_TABLE_NAME, (long)pk + 1);
            }
         }

         rs = stmt.executeQuery(String.format(SQLs.TWO_VALUE_SELECT, SQLs.USED_CLASSES_TABLE_PK_COLUMN_NAME, SQLs.USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME, schemaName, SQLs.USED_CLASSES_TABLE_NAME));
         while (rs.next())
         {
            pk = rs.getInt(1);
            String className = rs.getString(2);
            if (!this._state.tablePKs().get().containsKey(USED_CLASSES_TABLE_NAME) || this._state.tablePKs().get().get(USED_CLASSES_TABLE_NAME) <= pk)
            {
               this._state.tablePKs().get().put(USED_CLASSES_TABLE_NAME, pk + 1);
            }
            this._state.usedClassesPKs().get().put(className, (int)pk);
         }
         
         rs = stmt.executeQuery(String.format(SQLs.TWO_VALUE_SELECT, SQLs.USED_QNAMES_TABLE_QNAME_COLUMN_NAME, SQLs.USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME, schemaName, SQLs.USED_QNAMES_TABLE_NAME));
         Statement stmt2 = connection.createStatement();
         try
         {
            while (rs.next())
            {
               String qNameName = rs.getString(1);
               String qNameTableName = rs.getString(2);
               this._state.qNameInfos().get().get(QualifiedName.fromQN(qNameName)).setTableName(qNameTableName);
               pk = 0;
               ResultSet rs2 = stmt2.executeQuery(String.format(SQLs.ONE_VALUE_SELECT, "MAX(" + SQLs.QNAME_TABLE_PK_COLUMN_NAME + ")", schemaName, qNameTableName));
               if (rs2.next())
               {
                  pk = rs2.getLong(1);
               }
               this._state.tablePKs().get().put(qNameTableName, pk + 1);
            }
         }
         finally
         {
            stmt2.close();
         }
      } finally
      {
         stmt.close();
      }
      
   }
   
   private void writeAppMetadataToDB(Map<String, EntityDescriptor> entityDescriptors, Set<String> usedClassNames) throws SQLException
   {
      Connection connection = this._state.connection().get();
      String schemaName = this._state.schemaName().get();

      PreparedStatement ps = connection.prepareStatement(String.format(SQLs.TWO_VALUE_INSERT, this._state.schemaName().get(), ENTITY_TYPES_TABLE_NAME));
      try
      {
         for (EntityDescriptor descriptor : entityDescriptors.values())
         {
            String entityTypeName = descriptor.type().getName();
            if (!this._state.entityTypeInfos().get().containsKey(entityTypeName))
            {
               long pk = this._state.tablePKs().get().get(ENTITY_TYPES_TABLE_NAME);
               ps.setInt(1, (int) pk);
               ps.setString(2, entityTypeName);
               ps.executeUpdate();
               this._state.entityTypeInfos().get().put(entityTypeName, new EntityTypeInfo(descriptor, (int) pk));
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
      
      Statement stmt = connection.createStatement();
      ps = connection.prepareStatement(String.format(SQLs.TWO_VALUE_INSERT, schemaName, USED_QNAMES_TABLE_NAME));
      try
      {
         for (QNameInfo qNameInfo : this._state.qNameInfos().get().values())
         {
            StringBuilder builder = new StringBuilder();
            QNameType type = qNameInfo.getQNameType();
            
            builder.append( //
                  "CREATE TABLE " + schemaName + "." + qNameInfo.getTableName() + "(" + "\n" //
                  );
            String entityRefColumnName = SQLs.ENTITY_TABLE_PK_COLUMN_NAME;
            
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
                     QNAME_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                           QNAME_TABLE_VALUE_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + "," + "\n" + //
                           "PRIMARY KEY(" + QNAME_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                           "FOREIGN KEY(" + QNAME_TABLE_VALUE_COLUMN_NAME + ") REFERENCES " + schemaName + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED," + "\n" //
                     );
               entityRefColumnName = QNAME_TABLE_PK_COLUMN_NAME;
            }
            else if (type.equals(QNameType.MANY_ASSOCIATION))
            {
               builder.append( //
                     QNAME_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                           QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME + " " + QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                           QNAME_TABLE_VALUE_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + "," + "\n" + //
                           "PRIMARY KEY(" + QNAME_TABLE_PK_COLUMN_NAME + ", " + QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME + ")," + "\n" + //
                           "FOREIGN KEY(" + QNAME_TABLE_VALUE_COLUMN_NAME + ") REFERENCES " + schemaName + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED," + "\n" //
                     );
               entityRefColumnName = QNAME_TABLE_PK_COLUMN_NAME;
            }
            else
            {
               throw new IllegalArgumentException("Did not how to create table for qName type: " + type + ".");
            }
            builder.append( //
                  "FOREIGN KEY(" + entityRefColumnName + ") REFERENCES " + schemaName + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED" + "\n" + //
                        ");" //
                  );
            stmt.execute(builder.toString());
            
            ps.setString(1, qNameInfo.getQName().toString());
            ps.setString(2, qNameInfo.getTableName());
            ps.execute();
            
            this._state.tablePKs().get().put(qNameInfo.getTableName(), 0L);

         }
      }
      finally
      {
         stmt.close();
         ps.close();
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
