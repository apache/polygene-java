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

import static org.qi4j.library.sql.postgresql.internal.SQLs.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
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
import org.qi4j.library.sql.api.SQLTypeInfo;
import org.qi4j.library.sql.common.EntityTypeInfo;
import org.qi4j.library.sql.common.QNameInfo;
import org.qi4j.library.sql.common.ReindexingStrategy;
import org.qi4j.library.sql.common.SQLUtil;
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

   private interface SQLTypeCustomizer
   {
      String customizeType(Type propertyType, SQLTypeInfo sqlTypeInfo);
   }

   @Structure
   private Application _app;

   @This
   private PostgreSQLDBState _state;

   @This
   private Configuration<PostgreSQLConfiguration> _configuration;

   @Service private ReindexingStrategy _reindexingStrategy;

   @Service private Reindexer _reindexer;

   private Map<Class<?>, String> _creationTypeStrings;

   private Map<Class<?>, SQLTypeCustomizer> _customizableTypes;

   public static final String DEFAULT_SCHEMA_NAME = "qi4j";

   private static final Logger _log = Logger.getLogger(PostgreSQLAppStartup.class.getName());

   private void initTypes()
   {
      Map<Class<?>, Integer> primitiveTypes = new HashMap<Class<?>, Integer>();
      primitiveTypes.put(Boolean.class, Types.BOOLEAN);
      primitiveTypes.put(Byte.class, Types.SMALLINT);
      primitiveTypes.put(Short.class, Types.SMALLINT);
      primitiveTypes.put(Integer.class, Types.INTEGER);
      primitiveTypes.put(Long.class, Types.BIGINT);
      primitiveTypes.put(Float.class, Types.REAL);
      primitiveTypes.put(Double.class, Types.DOUBLE);
      primitiveTypes.put(Date.class, Types.TIMESTAMP);
      primitiveTypes.put(Character.class, Types.INTEGER);
      primitiveTypes.put(String.class, Types.VARCHAR);
      primitiveTypes.put(BigInteger.class, Types.NUMERIC);
      primitiveTypes.put(BigDecimal.class, Types.NUMERIC);

      this._state.javaTypes2SQLTypes().set(primitiveTypes);

      // TODO make use of Qi4j's MinLength/MaxLength -styled constraints
      // TODO some of the sizes of the values are quite improvised. Maybe allow user
      // some control over it, for example via annotations?
      // Like to lib-sql-api add annotation @SQLType(String) which would specify custom
      // sql type
      this._creationTypeStrings = new HashMap<Class<?>, String>();
      this._creationTypeStrings.put(Boolean.class, "BOOLEAN");
      this._creationTypeStrings.put(Byte.class, "SMALLINT");
      this._creationTypeStrings.put(Short.class, "INTEGER");
      this._creationTypeStrings.put(Integer.class, "INTEGER");
      this._creationTypeStrings.put(Long.class, "BIGINT");
      this._creationTypeStrings.put(Float.class, "REAL");
      this._creationTypeStrings.put(Double.class, "DOUBLE PRECISION");
      this._creationTypeStrings.put(Date.class, "TIMESTAMP WITH TIME ZONE");
      this._creationTypeStrings.put(Character.class, "INTEGER");
      this._creationTypeStrings.put(String.class, "VARCHAR(1024)");
      this._creationTypeStrings.put(BigInteger.class, "NUMERIC(50, 0)");
      this._creationTypeStrings.put(BigDecimal.class, "NUMERIC(50, 50)");

      this._customizableTypes = new HashMap<Class<?>, SQLTypeCustomizer>();
      this._customizableTypes.put( //
            String.class, //
            new SQLTypeCustomizer()
            {
               @Override
               public String customizeType(Type propertyType, SQLTypeInfo sqlTypeInfo)
               {
                  return "VARCHAR(" + sqlTypeInfo.maxLength() + ")";
               }
            } //
            );
      this._customizableTypes.put( //
            BigInteger.class, //
            new SQLTypeCustomizer()
            {
               @Override
               public String customizeType(Type propertyType, SQLTypeInfo sqlTypeInfo)
               {
                  return "NUMERIC(" + sqlTypeInfo.maxLength() + ", 0)";
               }
            } //
            );
      this._customizableTypes.put( //
            BigDecimal.class, //
            new SQLTypeCustomizer()
            {
               @Override
               public String customizeType(Type propertyType, SQLTypeInfo sqlTypeInfo)
               {
                  return "NUMERIC(" + sqlTypeInfo.maxLength() + ", " + sqlTypeInfo.scale() + ")";
               }
            } //
            );
   }

   @Override
   public Connection createConnection() throws SQLException
   {
      this.initTypes();

      Connection connection = DriverManager.getConnection(this._configuration.configuration().connectionString().get());
      this._state.connection().set(connection);

      connection.setAutoCommit(false);

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
      this._state.enumPKs().set(new HashMap<String, Integer>());

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

   private void constructApplicationInfo(final Map<String, EntityDescriptor> entityDescriptors, Set<String> usedClassNames, Set<String> enumValues, Boolean setQNameTableNameToNull) throws SQLException
   {
      final List<ValueDescriptor> valueDescriptors = new ArrayList<ValueDescriptor>();
      ApplicationSPI appSPI = (ApplicationSPI)this._app;
      appSPI.visitDescriptor(new DescriptorVisitor()
      {
         @Override
         public void visit(EntityDescriptor entityDescriptor)
         {
             if (entityDescriptor.entityType().queryable())
             {
               entityDescriptors.put(entityDescriptor.type().getName(), entityDescriptor);
             }
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
         this.extractPropertyQNames(descriptor, this._state.qNameInfos().get(), newQNames, valueDescriptors, usedVCClassNames, enumValues, setQNameTableNameToNull);
         this.extractAssociationQNames(descriptor, this._state.qNameInfos().get(), newQNames, setQNameTableNameToNull);
         this.extractManyAssociationQNames(descriptor, this._state.qNameInfos().get(), newQNames, setQNameTableNameToNull);
         this._state.entityUsedQNames().get().put(descriptor.type().getName(), newQNames);
      }

      usedClassNames.addAll(usedVCClassNames);
   }

   private void processPropertyTypeForQNames(PropertyDescriptor pType, Map<QualifiedName, QNameInfo> qNameInfos, Set<QualifiedName> newQNames, List<ValueDescriptor> vDescriptors, Set<String> usedVCClassNames, Set<String> enumValues, Boolean setQNameTableNameToNull)
   {
      QualifiedName qName = pType.qualifiedName();
      if (!newQNames.contains(qName) && !qName.typeName().name().equals(Identity.class.getName()))
      {
         newQNames.add(qName);
//         System.out.println("QName: " + qName + ", hc: " + qName.hashCode());
         QNameInfo info = qNameInfos.get(qName);
         if (info == null)
         {
            info = QNameInfo.fromProperty( //
                  qName, //
                  setQNameTableNameToNull ? null : (QNAME_TABLE_NAME_PREFIX + qNameInfos.size()), //
                  pType//
                  );
            qNameInfos.put(qName, info);
         }
         Type vType = info.getFinalType();

         while(vType instanceof ParameterizedType)
         {
            vType = ((ParameterizedType)vType).getRawType();
         }
         if (vType instanceof Class<?> ) //
         {
            if (((Class<?>)vType).isInterface())
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
                              enumValues, //
                              setQNameTableNameToNull //
                           );
                     }
                  }
               }
            } else if (Enum.class.isAssignableFrom((Class<?>)vType))
            {
               for (Object value : ((Class<?>)vType).getEnumConstants())
               {
                  enumValues.add(QualifiedName.fromClass((Class<?>)vType, value.toString()).toString());
               }
            }
         }

      }
   }

   private void extractPropertyQNames(EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> qNameInfos, Set<QualifiedName> newQNames, List<ValueDescriptor> vDescriptors, Set<String> usedVCClassNames, Set<String> enumValues, Boolean setQNameTableNameToNull)
   {
      for (PropertyDescriptor pDesc : entityDesc.state().properties())
      {
          if (!pDesc.isComputed() /* TODO or is queryable */)
          {
              this.processPropertyTypeForQNames( //
                  pDesc, //
                  qNameInfos, //
                  newQNames, //
                  vDescriptors, //
                  usedVCClassNames, //
                  enumValues, //
                  setQNameTableNameToNull //
                  );
          }
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
                        assoDesc //
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
                        mAssoDesc //
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

      Map<String, EntityDescriptor> entityDescriptors = new HashMap<String, EntityDescriptor>();
      Set<String> usedClassNames = new HashSet<String>();
      Set<String> enumValues = new HashSet<String>();
      Boolean reindexingRequired = this.isReindexingNeeded(schemaFound, entityDescriptors, usedClassNames, enumValues);

      if (schemaFound && !reindexingRequired)
      {
          this.testRequiredCapabilities( );
//         this.constructApplicationInfo(entityDescriptors, usedClassNames, enumValues, true);
         this.readAppMetadataFromDB(entityDescriptors);
      } else
      {
//         this.constructApplicationInfo(entityDescriptors, usedClassNames, enumValues, false);
         this.createSchema(schemaFound);
         this.writeAppMetadataToDB(entityDescriptors, usedClassNames, enumValues);

         if (reindexingRequired)
         {
            this.performReindex();
         }
      }

      return reindexingRequired;
   }

   private Boolean isReindexingNeeded(
       Boolean schemaExists,
       Map<String, EntityDescriptor> entityDescriptors,
       Set<String> usedClassNames,
       Set<String> enumValues
       ) throws SQLException
   {
      Boolean result = true;
      if (schemaExists)
      {
         Connection connection = this._state.connection().get();
         String schemaName = this._state.schemaName().get();
         Statement stmt = connection.createStatement();
         try
         {
            ResultSet rs = connection.getMetaData().getTables(null, schemaName, APP_VERSION_TABLE_NAME, new String[] { "TABLE" });
            if (rs.next())
            {
               SQLUtil.closeQuietly( rs );
               rs = stmt.executeQuery("SELECT " + APP_VERSION_TABLE_NAME + "." + APP_VERSION_PK_COLUMN_NAME + " FROM " + schemaName + "." + APP_VERSION_TABLE_NAME);

               result = !rs.next();

               if (!result)
               {

                  String dbAppVersion = rs.getString(1);
                  if (this._reindexingStrategy != null)
                  {
                     result = this._reindexingStrategy.reindexingNeeded(dbAppVersion, this._app.version());
                  }
               }
            }

         }
         finally
         {
            SQLUtil.closeQuietly( stmt );
         }
      }

      this.constructApplicationInfo( entityDescriptors, usedClassNames, enumValues, !result );

      if (schemaExists && result)
      {
          this.clearSchema( );
      }

      return result;
   }

   private void clearSchema() throws SQLException
   {
       Connection connection = this._state.connection( ).get( );
       String schemaName = this._state.schemaName( ).get( );
       DatabaseMetaData metaData = connection.getMetaData( );

       Statement stmt = connection.createStatement( );
       try
       {

           // Don't drop all entities table.
           this.dropTablesIfExist( metaData, schemaName, ALL_QNAMES_TABLE_NAME, stmt );
           this.dropTablesIfExist( metaData, schemaName, APP_VERSION_TABLE_NAME, stmt );
           this.dropTablesIfExist( metaData, schemaName, ENTITY_TYPES_TABLE_NAME, stmt );
           this.dropTablesIfExist( metaData, schemaName, ENUM_LOOKUP_TABLE_NAME, stmt );
           this.dropTablesIfExist( metaData, schemaName, USED_CLASSES_TABLE_NAME, stmt );
           this.dropTablesIfExist( metaData, schemaName, USED_QNAMES_TABLE_NAME, stmt );

           for (Integer x = 0; x < this._state.qNameInfos().get().size(); ++x)
           {
               this.dropTablesIfExist( metaData, schemaName, SQLs.QNAME_TABLE_NAME_PREFIX + x , stmt );
           }

       } finally
       {
           stmt.close( );
       }

   }

   private void dropTablesIfExist(DatabaseMetaData metaData, String schemaName, String tableName, Statement stmt) throws SQLException
   {
       ResultSet rs = metaData.getTables( null, schemaName, tableName, new String[] { "TABLE" } );
       try
       {
           while (rs.next( ))
           {
               stmt.execute( "DROP TABLE IF EXISTS " + schemaName + "." + rs.getString( 3 ) + " CASCADE");
           }
       } finally
       {
           rs.close( );
       }
   }

   private void createSchema(Boolean schemaFound) throws SQLException
   {
      Connection connection = this._state.connection().get();
      String schemaName = this._state.schemaName().get();

      Statement stmt = connection.createStatement();
      try
      {
          if (!schemaFound)
          {
              stmt.execute("CREATE SCHEMA " + schemaName + ";");
          }
          this.testRequiredCapabilities( );
         stmt.execute( //
               "CREATE TABLE " + schemaName + "." + USED_CLASSES_TABLE_NAME + "(" + "\n" + //
                     USED_CLASSES_TABLE_PK_COLUMN_NAME + " " + USED_CLASSES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                     USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME + " " + USED_CLASSES_TABLE_CLASS_NAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                     "PRIMARY KEY(" + USED_CLASSES_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                     "UNIQUE(" + USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME + ")" + "\n" + //
                     ")" //
               );

         this._state.tablePKs().get().put(USED_CLASSES_TABLE_NAME, 0L);

         stmt.execute( //
               "CREATE TABLE " + schemaName + "." + ENTITY_TYPES_TABLE_NAME + "(" + "\n" + //
                     ENTITY_TYPES_TABLE_PK_COLUMN_NAME + " " + ENTITY_TYPES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                     ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME + " " + ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                     "PRIMARY KEY(" + ENTITY_TYPES_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                     "UNIQUE(" + ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME + ")" + "\n" + //
                     ")");
         this._state.tablePKs().get().put(ENTITY_TYPES_TABLE_NAME, 0L);

         ResultSet rs = null;
         try
         {
             rs = connection.getMetaData().getTables( null, schemaName, ENTITY_TABLE_NAME, new String[] { "TABLE" } );
             if (!rs.next())
             {
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
                           ")" //
                     );
               this._state.tablePKs().get().put(ENTITY_TABLE_NAME, 0L);
             } else
             {
                 this._state.tablePKs().get().put( ENTITY_TABLE_NAME, this.getNextPK( Long.class, stmt, schemaName, ENTITY_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_NAME, 0L ) );
             }
         } finally
         {
             SQLUtil.closeQuietly( rs );
         }


         stmt.execute(
               "CREATE TABLE " + schemaName + "." + ENUM_LOOKUP_TABLE_NAME + "(" + "\n" + //
               ENUM_LOOKUP_TABLE_PK_COLUMN_NAME + " " + ENUM_LOOKUP_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
               ENUM_LOOKUP_TABLE_ENUM_VALUE_NAME + " " + ENUM_LOOKUP_TABLE_ENUM_VALUE_DATA_TYPE + " NOT NULL," + "\n" + //
               "PRIMARY KEY(" + ENUM_LOOKUP_TABLE_PK_COLUMN_NAME + ")" + "\n" + //
               ")" //
               );
         this._state.tablePKs().get().put(ENUM_LOOKUP_TABLE_NAME, 0L);

         stmt.execute( //
               "CREATE TABLE " + schemaName + "." + USED_QNAMES_TABLE_NAME + "(" + "\n" + //
               USED_QNAMES_TABLE_QNAME_COLUMN_NAME + " " + USED_QNAMES_TABLE_QNAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
               USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME + " " + USED_QNAMES_TABLE_TABLE_NAME_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
               "PRIMARY KEY(" + USED_QNAMES_TABLE_QNAME_COLUMN_NAME + ", " + USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME + ")" + "\n" + //
               ")" //
               );

         stmt.execute(
             "CREATE TABLE " + schemaName + "." + ALL_QNAMES_TABLE_NAME + "(" + "\n" + //
             ALL_QNAMES_TABLE_PK_COLUMN_NAME + " " + ALL_QNAMES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
             ENTITY_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
             "PRIMARY KEY(" + ALL_QNAMES_TABLE_PK_COLUMN_NAME + ", " + ENTITY_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
             "FOREIGN KEY(" + ENTITY_TABLE_PK_COLUMN_NAME + ") REFERENCES " + schemaName + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED" + "\n" + //
             ")" //
             );
         this._state.tablePKs( ).get( ).put( ALL_QNAMES_TABLE_NAME, 0L );

         stmt.execute(
               "CREATE TABLE " + schemaName + "." + APP_VERSION_TABLE_NAME + "(" + "\n" + //
               APP_VERSION_PK_COLUMN_NAME + " " + APP_VERSION_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
               "PRIMARY KEY(" + APP_VERSION_PK_COLUMN_NAME + ")" + "\n" + //
               ")" //
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

    protected <PKType> PKType getNextPK( Class<PKType> pkClass, Statement stmt, String schemaName, String columnName, String tableName, PKType defaultPK )
        throws SQLException
    {
        ResultSet rs = null;
        PKType result = defaultPK;
        try
        {
            rs = stmt.executeQuery( String.format(SQLs.TWO_VALUE_SELECT, "COUNT(" + columnName +  ")", "MAX(" + columnName + ") + 1", schemaName, tableName) );
            if( rs.next() )
            {
                Long count = rs.getLong( 1 );
                if( count > 0 )
                {
                    result = pkClass.cast( rs.getObject( 2 ) );
                }
            }
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
        }

        return result;
    }

   private void readAppMetadataFromDB(Map<String, EntityDescriptor> entityDescriptors) throws SQLException
   {

      String schemaName = this._state.schemaName().get();
      Connection connection = this._state.connection().get();
      Statement stmt = connection.createStatement();
      try
      {
          Map<String, Long> pks = this._state.tablePKs().get();
          pks.put(ENTITY_TABLE_NAME, this.getNextPK( Long.class, stmt, schemaName, SQLs.ENTITY_TABLE_PK_COLUMN_NAME, SQLs.ENTITY_TABLE_NAME, 0L ));
         ResultSet rs = stmt.executeQuery(String.format(SQLs.TWO_VALUE_SELECT, SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME, SQLs.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME, schemaName, SQLs.ENTITY_TYPES_TABLE_NAME));

         long pk = 0L;
         while (rs.next())
         {
            pk = rs.getInt(1);
            String entityTypeName = rs.getString(2);
            this._state.entityTypeInfos().get().put(entityTypeName, new EntityTypeInfo(entityDescriptors.get(entityTypeName), (int)pk));

            if (!this._state.tablePKs().get().containsKey(ENTITY_TYPES_TABLE_NAME) || this._state.tablePKs().get().get(ENTITY_TYPES_TABLE_NAME) <= pk)
            {

               this._state.tablePKs().get().put(ENTITY_TYPES_TABLE_NAME, pk + 1);
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

         rs = stmt.executeQuery(String.format(SQLs.TWO_VALUE_SELECT, SQLs.ENUM_LOOKUP_TABLE_PK_COLUMN_NAME, SQLs.ENUM_LOOKUP_TABLE_ENUM_VALUE_NAME, schemaName, SQLs.ENUM_LOOKUP_TABLE_NAME));
         while (rs.next())
         {
            pk = rs.getInt(1);
            String enumName = rs.getString(2);
            if (!this._state.tablePKs().get().containsKey(ENUM_LOOKUP_TABLE_NAME) || this._state.tablePKs().get().get(ENUM_LOOKUP_TABLE_NAME) <= pk)
            {
               this._state.tablePKs().get().put(ENUM_LOOKUP_TABLE_NAME, pk + 1);
            }
            this._state.enumPKs().get().put(enumName, (int)pk);
         }

//         rs = stmt.executeQuery(String.format(SQLs.ONE_VALUE_SELECT, "MAX(" + SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME + ")", schemaName, SQLs.PROPERTY_QNAMES_TABLE_NAME));
//         pk = 0;
//         if (rs.next( ))
//         {
//             Object max = rs.getObject( 1 );
//             if (max != null)
//             {
//                 pk = rs.getLong( 1 ) + 1;
//             }
//         }
//         this._state.tablePKs( ).get( ).put( SQLs.PROPERTY_QNAMES_TABLE_NAME, pk );
//
//         rs = stmt.executeQuery(String.format(SQLs.TWO_VALUE_SELECT, SQLs.USED_QNAMES_TABLE_QNAME_COLUMN_NAME, SQLs.USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME, schemaName, SQLs.USED_QNAMES_TABLE_NAME));
//         Statement stmt2 = connection.createStatement();
//         try
//         {
//            while (rs.next())
//            {
//               String qNameName = rs.getString(1);
//               String qNameTableName = rs.getString(2);
//               QNameInfo info = this._state.qNameInfos().get().get(QualifiedName.fromQN(qNameName));
//               info.setTableName(qNameTableName);
//               if (!info.getQNameType( ).equals( QNameType.PROPERTY ))
//               {
//                   pk = 0;
//                   ResultSet rs2 = stmt2.executeQuery(String.format(SQLs.ONE_VALUE_SELECT, "MAX(" + SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME + ")", schemaName, qNameTableName));
//                   if (rs2.next())
//                   {
//                       pk = rs2.getLong(1);
//                   }
//                   this._state.tablePKs().get().put(qNameTableName, pk + 1);
//               }
//            }
//         }
//         finally
//         {
//            stmt2.close();
//         }
      } finally
      {
         SQLUtil.closeQuietly( stmt );
      }

   }

   private void writeAppMetadataToDB(Map<String, EntityDescriptor> entityDescriptors, Set<String> usedClassNames, Set<String> allEnums) throws SQLException
   {
      Connection connection = this._state.connection().get();
      String schemaName = this._state.schemaName().get();

      PreparedStatement ps = connection.prepareStatement(String.format(SQLs.TWO_VALUE_INSERT, this._state.schemaName().get(), ENTITY_TYPES_TABLE_NAME));
      try
      {
         for (EntityDescriptor descriptor : entityDescriptors.values())
         {
            String entityTypeName = descriptor.type().getName();
            long pk = this._state.tablePKs().get().get(ENTITY_TYPES_TABLE_NAME);
            ps.setInt(1, (int) pk);
            ps.setString(2, entityTypeName);
            ps.executeUpdate();
            this._state.entityTypeInfos().get().put(entityTypeName, new EntityTypeInfo(descriptor, (int) pk));
            this._state.tablePKs().get().put(ENTITY_TYPES_TABLE_NAME, pk + 1);
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
            long pk = this._state.tablePKs().get().get(USED_CLASSES_TABLE_NAME);
            ps.setInt(1, (int) pk);
            ps.setString(2, usedClass);
            ps.executeUpdate();
            this._state.usedClassesPKs().get().put(usedClass, (int) pk);
            this._state.tablePKs().get().put(USED_CLASSES_TABLE_NAME, pk + 1);
         }
      }
      finally
      {
         ps.close();
      }

      ps = connection.prepareStatement(String.format(SQLs.TWO_VALUE_INSERT, this._state.schemaName().get(), ENUM_LOOKUP_TABLE_NAME));
      try
      {
         for (String enumValue : allEnums)
         {
            long pk = this._state.tablePKs().get().get(ENUM_LOOKUP_TABLE_NAME);
            ps.setInt(1, (int) pk);
            ps.setString(2, enumValue);
            ps.executeUpdate();
            this._state.enumPKs().get().put(enumValue, (int) pk);
            this._state.tablePKs().get().put(ENUM_LOOKUP_TABLE_NAME, pk + 1);
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
                  "CREATE TABLE " + schemaName + "." + qNameInfo.getTableName() + "(" + "\n" + //
                  ALL_QNAMES_TABLE_PK_COLUMN_NAME + " " + ALL_QNAMES_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                  SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " " + SQLs.ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" //
                  );

            if (type.equals(QNameType.PROPERTY))
            {
               builder.append(
                   QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + " " + ALL_QNAMES_TABLE_PK_COLUMN_DATA_TYPE + "," + "\n" //
               );

               if (qNameInfo.getCollectionDepth() > 0)
               {
                  builder.append(QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME + " " + QNAME_TABLE_COLLECTION_PATH_COLUMN_DATA_TYPE + " NOT NULL," + "\n");
               }
               this.appendColumnDefinitionsForProperty(builder, qNameInfo);
               builder.append(
                   "FOREIGN KEY(" + QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + ", " + ENTITY_TABLE_PK_COLUMN_NAME + ") REFERENCES " + schemaName + "." + ALL_QNAMES_TABLE_NAME + "(" + ALL_QNAMES_TABLE_PK_COLUMN_NAME + ", " + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED," + "\n" //
//                   ");"
                   );
            }
            else
            {
                if (type.equals(QNameType.ASSOCIATION))
                {
                    builder.append( //
//                        ALL_QNAMES_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        QNAME_TABLE_VALUE_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        "PRIMARY KEY(" + ALL_QNAMES_TABLE_PK_COLUMN_NAME + ", " + ENTITY_TABLE_PK_COLUMN_NAME + ")," + "\n" + //
                        "FOREIGN KEY(" + QNAME_TABLE_VALUE_COLUMN_NAME + ") REFERENCES " + schemaName + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED," + "\n" //
                         );
                }
                else if (type.equals(QNameType.MANY_ASSOCIATION))
                {
                    builder.append( //
//                        ALL_QNAMES_TABLE_PK_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME + " " + QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        QNAME_TABLE_VALUE_COLUMN_NAME + " " + ENTITY_TABLE_PK_COLUMN_DATA_TYPE + " NOT NULL," + "\n" + //
                        "PRIMARY KEY(" + ALL_QNAMES_TABLE_PK_COLUMN_NAME + ", " + ENTITY_TABLE_PK_COLUMN_NAME + ", " + QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME + ")," + "\n" + //
                        "FOREIGN KEY(" + QNAME_TABLE_VALUE_COLUMN_NAME + ") REFERENCES " + schemaName + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED," + "\n" //
                         );
                }
                else
                {
                    throw new IllegalArgumentException("Did not how to create table for qName type: " + type + ".");
                }

//                builder.append( //
//                    "FOREIGN KEY(" + ALL_QNAMES_TABLE_PK_COLUMN_NAME + ") REFERENCES " + schemaName + "." + ENTITY_TABLE_NAME + "(" + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED" + "\n" + //
//                    ");" //
//                    );
                this._state.tablePKs().get().put(qNameInfo.getTableName(), 0L);

            }

            builder.append( //
                "FOREIGN KEY(" + ALL_QNAMES_TABLE_PK_COLUMN_NAME + ", " + ENTITY_TABLE_PK_COLUMN_NAME + ") REFERENCES " + schemaName + "." + ALL_QNAMES_TABLE_NAME + "(" + ALL_QNAMES_TABLE_PK_COLUMN_NAME + ", " + ENTITY_TABLE_PK_COLUMN_NAME + ") ON UPDATE CASCADE ON DELETE CASCADE INITIALLY DEFERRED" + "\n" + //
                ")"
                );
            stmt.execute(builder.toString());
            stmt.execute( "COMMENT ON TABLE " + schemaName + "." + qNameInfo.getTableName( ) + " IS '" + qNameInfo.getQName( ) + "'" );

            ps.setString(1, qNameInfo.getQName().toString());
            ps.setString(2, qNameInfo.getTableName());
            ps.execute();

         }
      }
      finally
      {
         stmt.close();
         ps.close();
      }
   }

   private void appendColumnDefinitionsForProperty(StringBuilder builder, QNameInfo qNameInfo)
   {
      Type finalType = qNameInfo.getFinalType();
      if (finalType instanceof ParameterizedType)
      {
         finalType = ((ParameterizedType)finalType).getRawType();
      }
      Class<?> finalClass = (Class<?>)finalType;
      String sqlType = null;
      String valueRefTableName = null;
      String valueRefTablePKColumnName = null;
      if (qNameInfo.isFinalTypePrimitive())
      {

         if (this._customizableTypes.keySet().contains(finalClass) && qNameInfo.getPropertyDescriptor().accessor().isAnnotationPresent(SQLTypeInfo.class))
         {
            sqlType = this._customizableTypes.get(finalClass).customizeType(finalClass, qNameInfo.getPropertyDescriptor().accessor().getAnnotation(SQLTypeInfo.class));

         } else if (Enum.class.isAssignableFrom(finalClass))
         {
            // Enum - reference the lookup table
            sqlType = ENUM_LOOKUP_TABLE_PK_COLUMN_DATA_TYPE;
            valueRefTableName = ENUM_LOOKUP_TABLE_NAME;
            valueRefTablePKColumnName = ENUM_LOOKUP_TABLE_PK_COLUMN_NAME;
         } else
         {
            // Primitive type, default sqlType
            for (Map.Entry<Class<?>, String> entry : this._creationTypeStrings.entrySet())
            {
               if (entry.getKey().isAssignableFrom(finalClass))
               {
                  sqlType = entry.getValue();
                  break;
               }
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
         valueRefTableName = USED_CLASSES_TABLE_NAME;
         valueRefTablePKColumnName = USED_CLASSES_TABLE_PK_COLUMN_NAME;
      }

      builder.append(//
            QNAME_TABLE_VALUE_COLUMN_NAME + " " + sqlType + (qNameInfo.getCollectionDepth( ) > 0 ? "" : " NOT NULL") + "," + "\n" + //
            "PRIMARY KEY(" + ALL_QNAMES_TABLE_PK_COLUMN_NAME + ", " + ENTITY_TABLE_PK_COLUMN_NAME + ")," + "\n"
            );

      if (valueRefTableName != null && valueRefTablePKColumnName != null)
      {
         builder.append( //
               "FOREIGN KEY(" + QNAME_TABLE_VALUE_COLUMN_NAME + ") REFERENCES " + this._state.schemaName().get() + "." + valueRefTableName + "(" + valueRefTablePKColumnName + ") ON UPDATE CASCADE ON DELETE RESTRICT NOT DEFERRABLE," + "\n" //
               );
      }

   }

    private void testRequiredCapabilities( ) throws SQLException
    {
        // If collection structure matching will ever be needed, using ltree as path to each leaf item in collection-generated tree will be very useful
        // ltree module provides specific datatype for such path, which may be indexed in order to greatly improve performance

        Connection connection = this._state.connection( ).get( );
        Statement stmt = connection.createStatement( );
        try
        {
            stmt.execute( "CREATE TABLE " + this._state.schemaName( ).get() + ".ltree_test ( test_column ltree )");
            stmt.execute( "DROP TABLE " + this._state.schemaName( ).get( ) + ".ltree_test" );
        } catch (SQLException sqle)
        {
            throw new InternalError("It seems that your database doesn't have ltree as type. It is needed to store collections. Please refer to hopefully supplied instructions on how to add ltree type (hint: run <pg_install_dir>/share/contrib/ltree.sql script).");
        }
    }

}
