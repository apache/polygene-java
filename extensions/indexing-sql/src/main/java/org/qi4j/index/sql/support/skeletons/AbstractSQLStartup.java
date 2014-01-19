/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.sql.support.skeletons;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.index.sql.support.api.SQLAppStartup;
import org.qi4j.index.sql.support.api.SQLTypeInfo;
import org.qi4j.index.sql.support.common.DBNames;
import org.qi4j.index.sql.support.common.QNameInfo;
import org.qi4j.index.sql.support.common.QNameInfo.QNameType;
import org.qi4j.index.sql.support.common.RebuildingStrategy;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.grammar.builders.definition.TableElementListBuilder;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
import org.sql.generation.api.grammar.definition.table.AutoGenerationPolicy;
import org.sql.generation.api.grammar.definition.table.ConstraintCharacteristics;
import org.sql.generation.api.grammar.definition.table.ReferentialAction;
import org.sql.generation.api.grammar.definition.table.UniqueSpecification;
import org.sql.generation.api.grammar.factories.DataTypeFactory;
import org.sql.generation.api.grammar.factories.DefinitionFactory;
import org.sql.generation.api.grammar.factories.LiteralFactory;
import org.sql.generation.api.grammar.factories.ModificationFactory;
import org.sql.generation.api.grammar.factories.QueryFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.grammar.manipulation.DropBehaviour;
import org.sql.generation.api.grammar.manipulation.ObjectType;
import org.sql.generation.api.grammar.modification.DeleteBySearch;
import org.sql.generation.api.grammar.modification.InsertStatement;
import org.sql.generation.api.grammar.query.QueryExpression;
import org.sql.generation.api.vendor.SQLVendor;

import static org.qi4j.functional.Iterables.first;
import static org.qi4j.index.sql.support.common.DBNames.ALL_QNAMES_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ALL_QNAMES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.APP_VERSION_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.APP_VERSION_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_MODIFIED_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_VERSION_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_JOIN_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENUM_LOOKUP_TABLE_ENUM_VALUE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENUM_LOOKUP_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENUM_LOOKUP_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_NAME_PREFIX;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_VALUE_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_CLASSES_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_CLASSES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_QNAMES_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_QNAMES_TABLE_QNAME_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME;

public abstract class AbstractSQLStartup
    implements SQLAppStartup
{
    private interface SQLTypeCustomizer
    {
        SQLDataType customizeType( Type propertyType, SQLTypeInfo sqlTypeInfo );
    }

    public static final String DEFAULT_SCHEMA_NAME = "qi4j";

    private static final Class<?> ENTITY_PK_TYPE = Long.class;
    private static final Class<?> ENTITY_TYPE_PK_TYPE = Integer.class;

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractSQLStartup.class.getName() );

    static final ThreadLocal<Connection> CONNECTION_FOR_REINDEXING = new ThreadLocal<Connection>()
    {
        @Override
        protected Connection initialValue()
        {
            return null;
        }
    };

    @This
    private SQLDBState _state;

    @This
    private Configuration<SQLConfiguration> _configuration;

    @Service
    @Optional
    private ReindexingStrategy _reindexingStrategy;

    @Service
    @Optional
    private RebuildingStrategy _rebuildingStrategy;

    @Service
    private DataSource _dataSource;

    @Service
    private Reindexer _reindexer;

    @Structure
    private Application _app;

    private final SQLVendor _vendor;

    private Map<Class<?>, SQLTypeCustomizer> _customizableTypes;

    private Map<Class<?>, SQLDataType> _primitiveTypes;

    public AbstractSQLStartup( @Uses ServiceDescriptor descriptor )
    {
        this._vendor = descriptor.metaInfo( SQLVendor.class );
    }

    @Override
    public void initConnection()
        throws SQLException
    {
        this._configuration.refresh();

        this.initTypes();
        this.modifyPrimitiveTypes( this._primitiveTypes, this._state.javaTypes2SQLTypes().get() );

        String schemaName = this._configuration.get().schemaName().get();
        if( schemaName == null )
        {
            schemaName = DEFAULT_SCHEMA_NAME;
        }
        else
        {
            this.checkSchemaName( schemaName );
        }
        LOGGER.debug( "Will use '{}' as schema name", schemaName );

        this._state.schemaName().set( schemaName );
        this._state.entityTypePKs().set( new HashMap<String, Integer>() );
        this._state.usedClassesPKs().set( new HashMap<CompositeDescriptor, Integer>() );
        this._state.entityUsedQNames().set( new HashMap<EntityDescriptor, Set<QualifiedName>>() );
        this._state.qNameInfos().set( new HashMap<QualifiedName, QNameInfo>() );
        this._state.enumPKs().set( new HashMap<String, Integer>() );

        Connection connection = this._dataSource.getConnection();
        try
        {
            connection.setAutoCommit( true );
            connection.setReadOnly( false );
            this.syncDB( connection );
        }
        finally
        {
            SQLUtil.closeQuietly( connection );
        }

        if( LOGGER.isDebugEnabled() )
        {

            String newline = "\n";
            String tab = "\t";
            String colonspace = ": ";
            StringBuilder report = new StringBuilder();

            report.append( "schemaName: " ).append( _state.schemaName().get() ).append( newline );

            report.append( "qNameInfos: " ).append( newline );
            for( Map.Entry<QualifiedName, QNameInfo> entry : _state.qNameInfos().get().entrySet() )
            {
                report.append( tab ).append( entry.getKey() ).append( colonspace )
                    .append( entry.getValue() ).append( newline );
            }

            report.append( "entityUsedQNames:" ).append( newline );
            for( Map.Entry<EntityDescriptor, Set<QualifiedName>> entry : _state.entityUsedQNames()
                .get()
                .entrySet() )
            {
                report.append( tab ).append( entry.getKey() ).append( colonspace )
                    .append( entry.getValue() ).append( newline );
            }

            report.append( "usedClassesPKs:" ).append( newline );
            for( Map.Entry<CompositeDescriptor, Integer> entry : _state.usedClassesPKs().get()
                .entrySet() )
            {
                report.append( tab ).append( entry.getKey() ).append( colonspace )
                    .append( entry.getValue() ).append( newline );
            }

            report.append( "javaTypes2SQLTypes:" ).append( newline );
            for( Map.Entry<Class<?>, Integer> entry : _state.javaTypes2SQLTypes().get().entrySet() )
            {
                report.append( tab ).append( entry.getKey() ).append( colonspace )
                    .append( entry.getValue() ).append( newline );
            }

            report.append( "entityTypePKs:" ).append( newline );
            for( Map.Entry<String, Integer> entry : _state.entityTypePKs().get().entrySet() )
            {
                report.append( tab ).append( entry.getKey() ).append( colonspace )
                    .append( entry.getValue() ).append( newline );
            }

            report.append( "enumPKs:" ).append( newline );
            for( Map.Entry<String, Integer> entry : _state.enumPKs().get().entrySet() )
            {
                report.append( tab ).append( entry.getKey() ).append( colonspace )
                    .append( entry.getValue() ).append( newline );
            }

            LOGGER.debug( "SQLDBState after initConnection:\n{}", report.toString() );
        }
    }

    private void initTypes()
    {

        DataTypeFactory dt = this._vendor.getDataTypeFactory();

        this._primitiveTypes = new HashMap<>();
        this._primitiveTypes.put( Boolean.class, dt.sqlBoolean() );
        this._primitiveTypes.put( Byte.class, dt.smallInt() );
        this._primitiveTypes.put( Short.class, dt.smallInt() );
        this._primitiveTypes.put( Integer.class, dt.integer() );
        this._primitiveTypes.put( Long.class, dt.bigInt() );
        this._primitiveTypes.put( Float.class, dt.real() );
        this._primitiveTypes.put( Double.class, dt.doublePrecision() );
        this._primitiveTypes.put( Date.class, dt.timeStamp( true ) );
        this._primitiveTypes.put( Character.class, dt.integer() );
        this._primitiveTypes.put( String.class, dt.sqlVarChar( 5000 ) );
        this._primitiveTypes.put( BigInteger.class, dt.decimal() );
        this._primitiveTypes.put( BigDecimal.class, dt.decimal() );

        Map<Class<?>, Integer> jdbcTypes = new HashMap<>();
        jdbcTypes.put( Boolean.class, Types.BOOLEAN );
        jdbcTypes.put( Byte.class, Types.SMALLINT );
        jdbcTypes.put( Short.class, Types.SMALLINT );
        jdbcTypes.put( Integer.class, Types.INTEGER );
        jdbcTypes.put( Long.class, Types.BIGINT );
        jdbcTypes.put( Float.class, Types.REAL );
        jdbcTypes.put( Double.class, Types.DOUBLE );
        jdbcTypes.put( Date.class, Types.TIMESTAMP );
        jdbcTypes.put( Character.class, Types.INTEGER );
        jdbcTypes.put( String.class, Types.VARCHAR );
        jdbcTypes.put( BigInteger.class, Types.NUMERIC );
        jdbcTypes.put( BigDecimal.class, Types.NUMERIC );
        this._state.javaTypes2SQLTypes().set( jdbcTypes );

        this._customizableTypes = new HashMap<>();
        this._customizableTypes.put( //
            String.class, //
            new SQLTypeCustomizer()
        {
            @Override
            public SQLDataType customizeType( Type propertyType, SQLTypeInfo sqlTypeInfo )
            {
                return _vendor.getDataTypeFactory().sqlVarChar( sqlTypeInfo.maxLength() );
            }
            } //
        );
        this._customizableTypes.put( //
            BigInteger.class, //
            new SQLTypeCustomizer()
        {
            @Override
            public SQLDataType customizeType( Type propertyType, SQLTypeInfo sqlTypeInfo )
            {
                return _vendor.getDataTypeFactory().decimal( sqlTypeInfo.maxLength() );
            }
            } //
        );
        this._customizableTypes.put( //
            BigDecimal.class, //
            new SQLTypeCustomizer()
        {
            @Override
            public SQLDataType customizeType( Type propertyType, SQLTypeInfo sqlTypeInfo )
            {
                return _vendor.getDataTypeFactory().decimal( sqlTypeInfo.maxLength() );
            }
            } //
        );
    }

    protected void checkSchemaName( String schemaName )
    {
        // By default, we accept alphanumeric strings with underscores in them
        if( !Pattern.matches( "^\\p{L}(\\_|\\p{L}|\\p{N})*$", schemaName ) )
        {
            throw new IllegalStateException( "Illegal schema name: " + schemaName + "." );
        }
    }

    private static class ApplicationInfo
    {

        private final Map<String, EntityDescriptor> entityDescriptors = new HashMap<>();

        private final Set<CompositeDescriptorInfo> usedValueComposites = new HashSet<>();

        private final Set<String> enumValues = new HashSet<>();

    }

    private static class CompositeDescriptorInfo
    {
        final LayerDescriptor layer;
        final ModuleDescriptor module;
        final CompositeDescriptor composite;

        private CompositeDescriptorInfo( LayerDescriptor theLayer, ModuleDescriptor theModule,
                                         CompositeDescriptor theComposite )
        {
            this.layer = theLayer;
            this.module = theModule;
            this.composite = theComposite;
        }

        @Override
        public boolean equals( Object obj )
        {
            return this == obj
                   || ( obj instanceof CompositeDescriptorInfo && this.composite
                       .equals( ( (CompositeDescriptorInfo) obj ).composite ) );
        }

        @Override
        public int hashCode()
        {
            return this.composite.hashCode();
        }
    }

    private void syncDB( Connection connection )
        throws SQLException
    {
        String schemaName = this._state.schemaName().get();
        String appVersion = this._app.version();
        String dbAppVersion = this.readAppVersionFromDB( connection, schemaName );

        // Rebuild if needed
        boolean rebuildingNeeded = dbAppVersion == null;

        if( !rebuildingNeeded && this._rebuildingStrategy != null )
        {
            rebuildingNeeded = this._rebuildingStrategy.rebuildingRequired( dbAppVersion, appVersion );
        }

        ApplicationInfo appInfo = this.constructApplicationInfo( !rebuildingNeeded );

        if( rebuildingNeeded )
        {
            LOGGER.debug( "(Re)building schema " + schemaName );
            this.destroyNeededSchemaTables( connection, schemaName, this._state.qNameInfos().get().size() );

            Map<String, Long> tablePKs = new HashMap<>();
            this.createSchemaAndRequiredTables( connection, schemaName, tablePKs );
            this.writeAppMetadataToDB( connection, appInfo, tablePKs );
        }
        else
        {
            this.testRequiredCapabilities( connection );
            this.readAppMetadataFromDB( connection, appInfo.entityDescriptors );
            LOGGER.debug( "Application metadata loaded from database" );
        }

        boolean reindexingNeeded = dbAppVersion == null;
        if( !reindexingNeeded && this._reindexingStrategy != null )
        {
            reindexingNeeded = this._reindexingStrategy.reindexingNeeded( dbAppVersion, appVersion );
        }

        if( reindexingNeeded )
        {
            LOGGER.debug( "(Re)indexing entitystore, using schema " + schemaName );
            this.performReindex( connection );
        }
    }

    private void createSchemaAndRequiredTables( Connection connection, String schemaName,
                                                Map<String, Long> tablePKs )
        throws SQLException
    {
        boolean schemaFound = false;

        ResultSet rs = connection.getMetaData().getSchemas();
        try
        {
            while( rs.next() && !schemaFound )
            {
                schemaFound = rs.getString( 1 ).equals( schemaName );
            }
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
        }

        SQLVendor vendor = this._vendor;
        DefinitionFactory d = vendor.getDefinitionFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        Statement stmt = connection.createStatement();

        // @formatter:off
        try
        {
            if( !schemaFound )
            {
                stmt.execute(
                    vendor.toString(
                        d
                        .createSchemaDefinitionBuilder()
                        .setSchemaName( schemaName )
                        .createExpression()
                    )
                );
                LOGGER.debug( "Database schema created" );
            }

            this.testRequiredCapabilities( connection );
            LOGGER.debug( "Underlying database fullfill required capabilities" );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( schemaName, USED_CLASSES_TABLE_NAME ) )
                    .setTableContentsSource(
                        d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( USED_CLASSES_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                .get( Integer.class ), false ) )
                        .addTableElement( d.createColumnDefinition( USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                .addColumns( USED_CLASSES_TABLE_PK_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.UNIQUE )
                                .addColumns( USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .createExpression()
                    )
                    .createExpression()
                )
            );

            tablePKs.put( USED_CLASSES_TABLE_NAME, 0L );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( schemaName, ENTITY_TYPES_TABLE_NAME ) )
                    .setTableContentsSource(
                        d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( ENTITY_TYPES_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                .get( ENTITY_TYPE_PK_TYPE ), false ) )
                        .addTableElement( d.createColumnDefinition( ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                .addColumns( ENTITY_TYPES_TABLE_PK_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.UNIQUE )
                                .addColumns( ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .createExpression()
                    )
                    .createExpression()
                )
            );

            tablePKs.put( ENTITY_TYPES_TABLE_NAME, 0L );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( schemaName, ENTITY_TABLE_NAME ) )
                    .setTableContentsSource(
                        d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                .get( ENTITY_PK_TYPE ), false, AutoGenerationPolicy.BY_DEFAULT ) )
                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_IDENTITY_COLUMN_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_MODIFIED_COLUMN_NAME, this._primitiveTypes
                                .get( Date.class ), false ) )
                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_VERSION_COLUMN_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                .addColumns( ENTITY_TABLE_PK_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.UNIQUE )
                                .addColumns( ENTITY_TABLE_IDENTITY_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .createExpression()
                    )
                    .createExpression()
                )
            );
            tablePKs.put( ENTITY_TABLE_NAME, 0L );

            stmt.execute(
                d.createTableDefinitionBuilder()
                .setTableName( t.tableName( schemaName, ENTITY_TYPES_JOIN_TABLE_NAME ) )
                .setTableContentsSource(
                    d.createTableElementListBuilder()
                    .addTableElement( d.createColumnDefinition( ENTITY_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                            .get( ENTITY_PK_TYPE ), false ) )
                    .addTableElement( d.createColumnDefinition( ENTITY_TYPES_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                            .get( ENTITY_TYPE_PK_TYPE ), false ) )
                    .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                            .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                            .addColumns( ENTITY_TABLE_PK_COLUMN_NAME, ENTITY_TYPES_TABLE_PK_COLUMN_NAME )
                            .createExpression()
                        ) )
                    .addTableElement( d.createTableConstraintDefinition( d.createForeignKeyConstraintBuilder()
                            .addSourceColumns( ENTITY_TABLE_PK_COLUMN_NAME )
                            .setTargetTableName( t.tableName( schemaName, ENTITY_TABLE_NAME ) )
                            .addTargetColumns( ENTITY_TABLE_PK_COLUMN_NAME )
                            .setOnDelete( ReferentialAction.CASCADE )
                            .setOnUpdate( ReferentialAction.CASCADE )
                            .createExpression(), ConstraintCharacteristics.INITIALLY_DEFERRED_DEFERRABLE
                        ) )
                    .addTableElement( d.createTableConstraintDefinition( d.createForeignKeyConstraintBuilder()
                            .addSourceColumns( ENTITY_TYPES_TABLE_PK_COLUMN_NAME )
                            .setTargetTableName( t.tableName( schemaName, ENTITY_TYPES_TABLE_NAME ) )
                            .addTargetColumns( ENTITY_TYPES_TABLE_PK_COLUMN_NAME )
                            .setOnDelete( ReferentialAction.RESTRICT )
                            .setOnDelete( ReferentialAction.CASCADE )
                            .createExpression(), ConstraintCharacteristics.NOT_DEFERRABLE ) )
                    .createExpression()
                ).createExpression()
                .toString()
            );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( schemaName, ENUM_LOOKUP_TABLE_NAME ) )
                    .setTableContentsSource(
                        d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( ENUM_LOOKUP_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                .get( Integer.class ), false ) )
                        .addTableElement( d.createColumnDefinition( ENUM_LOOKUP_TABLE_ENUM_VALUE_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                .addColumns( ENUM_LOOKUP_TABLE_PK_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .createExpression()
                    )
                    .createExpression()
                )
            );

            tablePKs.put( ENUM_LOOKUP_TABLE_NAME, 0L );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( schemaName, USED_QNAMES_TABLE_NAME ) )
                    .setTableContentsSource(
                        d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( USED_QNAMES_TABLE_QNAME_COLUMN_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createColumnDefinition( USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                .addColumns( USED_QNAMES_TABLE_QNAME_COLUMN_NAME, USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .createExpression()
                    )
                    .createExpression()
                )
            );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( schemaName, ALL_QNAMES_TABLE_NAME ) )
                    .setTableContentsSource(
                        d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( ALL_QNAMES_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                .get( Integer.class ), false ) )
                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                .get( ENTITY_PK_TYPE ), false ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                .addColumns( ALL_QNAMES_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createForeignKeyConstraintBuilder()
                                .addSourceColumns( ENTITY_TABLE_PK_COLUMN_NAME )
                                .setTargetTableName( t.tableName( schemaName, ENTITY_TABLE_NAME ) )
                                .addTargetColumns( ENTITY_TABLE_PK_COLUMN_NAME )
                                .setOnUpdate( ReferentialAction.CASCADE )
                                .setOnDelete( ReferentialAction.CASCADE )
                                .createExpression(), ConstraintCharacteristics.INITIALLY_DEFERRED_DEFERRABLE
                            ) )
                        .createExpression()
                    )
                    .createExpression()
                )
            );

            tablePKs.put( ALL_QNAMES_TABLE_NAME, 0L );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( schemaName, APP_VERSION_TABLE_NAME ) )
                    .setTableContentsSource(
                        d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( APP_VERSION_PK_COLUMN_NAME, this._primitiveTypes
                                .get( String.class ), false ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                .addColumns( APP_VERSION_PK_COLUMN_NAME )
                                .createExpression()
                            ) )
                        .createExpression()
                    )
                    .createExpression()
                )
            );

            ModificationFactory m = vendor.getModificationFactory();

            PreparedStatement ps = connection.prepareStatement(
                vendor.toString(
                    m.insert()
                    .setTableName( t.tableName( schemaName, APP_VERSION_TABLE_NAME ) )
                    .setColumnSource(
                        m.columnSourceByValues()
                        .addValues( vendor.getLiteralFactory().param() )
                        .createExpression()
                    )
                    .createExpression()
                )
            );
            ps.setString( 1, this._app.version() );
            ps.execute();

            // TODO INDICES!!!!
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
        }

        // @formatter:on
        LOGGER.debug( "Indexing SQL database tables created" );
    }

    private void performReindex( Connection connection )
        throws SQLException
    {
        LOGGER.info( "Performing reindexing..." );
        // @formatter:off
        // First delete all entity data
        DeleteBySearch clearEntityData = this._vendor.getModificationFactory().deleteBySearch()
            .setTargetTable(
                this._vendor.getModificationFactory().createTargetTable(
                    this._vendor.getTableReferenceFactory().tableName(
                        this._state.schemaName().get(),
                        ENTITY_TABLE_NAME
                    )
                )
            ).createExpression();
        connection.prepareStatement( this._vendor.toString( clearEntityData ) ).execute();
        // @formatter:on

        CONNECTION_FOR_REINDEXING.set( connection );
        try
        {
            this._reindexer.reindex();
        }
        finally
        {
            CONNECTION_FOR_REINDEXING.set( null );
        }

        LOGGER.info( "Reindexing complete." );
    }

    private void readAppMetadataFromDB( Connection connection,
                                        Map<String, EntityDescriptor> entityDescriptors )
        throws SQLException
    {

        String schemaName = this._state.schemaName().get();
        Statement stmt = connection.createStatement();

        SQLVendor vendor = this._vendor;
        QueryFactory q = vendor.getQueryFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        try
        {
            // @formatter:off

            q.simpleQueryBuilder()
                .select( ENTITY_TYPES_TABLE_PK_COLUMN_NAME, ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME )
                .from( t.tableName( schemaName, ENTITY_TYPES_TABLE_NAME ) )
                .createExpression();

            ResultSet rs = stmt.executeQuery(
                vendor.toString(
                    q.simpleQueryBuilder()
                    .select( ENTITY_TYPES_TABLE_PK_COLUMN_NAME, ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME )
                    .from( t.tableName( schemaName, ENTITY_TYPES_TABLE_NAME ) )
                    .createExpression()
                )
            );

            long pk;
            try
            {
                while( rs.next() )
                {
                    pk = rs.getInt( 1 );
                    String entityTypeName = rs.getString( 2 );
                    this._state.entityTypePKs().get().put( entityTypeName, (int) pk );
//                    this._state.entityTypeInfos().get()
//                        .put( entityTypeName, new EntityTypeInfo( entityDescriptors.get( entityTypeName ), (int) pk ) );
                }
            }
            finally
            {
                SQLUtil.closeQuietly( rs );
            }
            rs = stmt.executeQuery(
                vendor.toString(
                    q.simpleQueryBuilder()
                    .select( USED_CLASSES_TABLE_PK_COLUMN_NAME, USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME )
                    .from( t.tableName( schemaName, USED_CLASSES_TABLE_NAME ) )
                    .createExpression()
                )
            );

            try
            {
                while( rs.next() )
                {
                    pk = rs.getInt( 1 );
                    String descriptorTextualFormat = rs.getString( 2 );
                    this._state.usedClassesPKs().get().put(
                        stringToCompositeDescriptor( ValueDescriptor.class,
                                                     this._app.descriptor(),
                                                     descriptorTextualFormat ),
                        (int) pk );
                }
            }
            finally
            {
                SQLUtil.closeQuietly( rs );
            }

            rs = stmt.executeQuery(
                vendor.toString(
                    q.simpleQueryBuilder()
                    .select( ENUM_LOOKUP_TABLE_PK_COLUMN_NAME, ENUM_LOOKUP_TABLE_ENUM_VALUE_NAME )
                    .from( t.tableName( schemaName, ENUM_LOOKUP_TABLE_NAME ) )
                    .createExpression()
                )
            );

            try
            {
                while( rs.next() )
                {
                    pk = rs.getInt( 1 );
                    String enumName = rs.getString( 2 );
                    this._state.enumPKs().get().put( enumName, (int) pk );
                }
            }
            finally
            {
                SQLUtil.closeQuietly( rs );
            }

            rs = stmt.executeQuery(
                q.simpleQueryBuilder()
                .select( USED_QNAMES_TABLE_QNAME_COLUMN_NAME, USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME )
                .from( t.tableName( schemaName, USED_QNAMES_TABLE_NAME ) )
                .createExpression()
                .toString()
            );
            try
            {
                while( rs.next() )
                {
                    String qName = rs.getString( 1 );
                    String tableName = rs.getString( 2 );
                    this._state.qNameInfos().get().get( QualifiedName.fromFQN( qName ) ).setTableName( tableName );
                }
            }
            finally
            {
                SQLUtil.closeQuietly( rs );
            }

            // @formatter:on
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
        }
    }

    private void writeAppMetadataToDB( Connection connection, ApplicationInfo appInfo,
                                       Map<String, Long> tablePKs )
        throws SQLException
    {
        String schemaName = this._state.schemaName().get();

        SQLVendor vendor = this._vendor;
        ModificationFactory m = vendor.getModificationFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        LiteralFactory l = vendor.getLiteralFactory();

        // @formatter:off
        PreparedStatement ps = connection.prepareStatement(
            vendor.toString(
                m.insert()
                .setTableName( t.tableName( schemaName, ENTITY_TYPES_TABLE_NAME ) )
                .setColumnSource( m.columnSourceByValues()
                    .addValues( l.param(), l.param() )
                    .createExpression()
                )
                .createExpression()
            )
        );

        try
        {
            Set<String> insertedTypeNames = new HashSet<>();
            for( EntityDescriptor descriptor : appInfo.entityDescriptors.values() )
            {
                for( Class<?> entityType : descriptor.types() )
                {
                    String entityTypeName = entityType.getName();
                    if( !insertedTypeNames.contains( entityTypeName ) )
                    {
                        long pk = tablePKs.get( ENTITY_TYPES_TABLE_NAME );
                        ps.setInt( 1, (int) pk );
                        ps.setString( 2, entityTypeName );
                        ps.executeUpdate();
                        this._state.entityTypePKs().get().put( entityTypeName, (int) pk );
//                      this._state.entityTypeInfos().get().put( entityTypeName, new EntityTypeInfo( descriptor, (int) pk ) );
                        tablePKs.put( ENTITY_TYPES_TABLE_NAME, pk + 1 );
                    }
                }
            }
        }
        finally
        {
            SQLUtil.closeQuietly( ps );
        }

        ps = connection.prepareStatement(
            vendor.toString(
                m.insert()
                .setTableName( t.tableName( schemaName, USED_CLASSES_TABLE_NAME ) )
                .setColumnSource( m.columnSourceByValues()
                    .addValues( l.param(), l.param() )
                    .createExpression()
                )
                .createExpression()
            )
        );

        try
        {
            for( CompositeDescriptorInfo descInfo : appInfo.usedValueComposites )
            {
                String vDescStr = compositeDescriptorToString( descInfo.layer, descInfo.module, descInfo.composite );
                long pk = tablePKs.get( USED_CLASSES_TABLE_NAME );
                ps.setInt( 1, (int) pk );
                ps.setString( 2, vDescStr );
                ps.executeUpdate();
                this._state.usedClassesPKs().get().put( descInfo.composite, (int) pk );
                tablePKs.put( USED_CLASSES_TABLE_NAME, pk + 1 );
            }
        }
        finally
        {
            SQLUtil.closeQuietly( ps );
        }

        ps = connection.prepareStatement(
            vendor.toString(
                m.insert()
                .setTableName( t.tableName( schemaName, ENUM_LOOKUP_TABLE_NAME ) )
                .setColumnSource( m.columnSourceByValues()
                    .addValues( l.param(), l.param() )
                    .createExpression()
                )
                .createExpression()
            )
        );

        try
        {
            for( String enumValue : appInfo.enumValues )
            {
                long pk = tablePKs.get( ENUM_LOOKUP_TABLE_NAME );
                ps.setInt( 1, (int) pk );
                ps.setString( 2, enumValue );
                ps.executeUpdate();
                this._state.enumPKs().get().put( enumValue, (int) pk );
                tablePKs.put( ENUM_LOOKUP_TABLE_NAME, pk + 1 );
            }
        }
        finally
        {
            SQLUtil.closeQuietly( ps );
        }

        Statement stmt = connection.createStatement();
        ps = connection.prepareStatement(
            this.createInsertStatementForQNameInfo( connection, schemaName, vendor ).toString()
        );

        try
        {
            DefinitionFactory d = vendor.getDefinitionFactory();

            for( QNameInfo qNameInfo : this._state.qNameInfos().get().values() )
            {
                QNameType type = qNameInfo.getQNameType();

                TableElementListBuilder builder = d.createTableElementListBuilder();
                builder
                    .addTableElement( d.createColumnDefinition( ALL_QNAMES_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                            .get( Integer.class ), false ) )
                    .addTableElement( d.createColumnDefinition( ENTITY_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                            .get( ENTITY_PK_TYPE ), false ) );

                if( type.equals( QNameType.PROPERTY ) )
                {
                    builder.addTableElement( d.createColumnDefinition( QNAME_TABLE_PARENT_QNAME_COLUMN_NAME, this._primitiveTypes
                        .get( Integer.class ), true ) );

                    if( qNameInfo.getCollectionDepth() > 0 )
                    {
                        builder.addTableElement( d.createColumnDefinition( QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME, this
                            .getCollectionPathDataType(), false ) );
                    }

                    this.appendColumnDefinitionsForProperty( builder, qNameInfo );

                    builder.addTableElement( d.createTableConstraintDefinition( d.createForeignKeyConstraintBuilder()
                        .addSourceColumns( QNAME_TABLE_PARENT_QNAME_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                        .setTargetTableName( t.tableName( schemaName, ALL_QNAMES_TABLE_NAME ) )
                        .addTargetColumns( ALL_QNAMES_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                        .setOnUpdate( ReferentialAction.CASCADE )
                        .setOnDelete( ReferentialAction.CASCADE )
                        .createExpression(), ConstraintCharacteristics.INITIALLY_DEFERRED_DEFERRABLE
                    ) );
                }
                else
                {
                    if( type.equals( QNameType.ASSOCIATION ) )
                    {
                        builder
                            .addTableElement( d.createColumnDefinition( QNAME_TABLE_VALUE_COLUMN_NAME, this._primitiveTypes
                                    .get( ENTITY_PK_TYPE ), false ) )
                            .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                    .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                    .addColumns( ALL_QNAMES_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                                    .createExpression()
                                ) );
                    }
                    else if( type.equals( QNameType.MANY_ASSOCIATION ) )
                    {
                        builder
                            .addTableElement( d.createColumnDefinition( QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME, this._primitiveTypes
                                    .get( Integer.class ), false ) )
                            .addTableElement( d.createColumnDefinition( QNAME_TABLE_VALUE_COLUMN_NAME, this._primitiveTypes
                                    .get( ENTITY_PK_TYPE ), false ) )
                            .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                    .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                    .addColumns( ALL_QNAMES_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                                    .createExpression()
                                ) );
                    }
                    else
                    {
                        throw new IllegalArgumentException( "Did not how to create table for qName type: " + type + "." );
                    }

                    builder
                        .addTableElement( d.createTableConstraintDefinition( d.createForeignKeyConstraintBuilder()
                                .addSourceColumns( QNAME_TABLE_VALUE_COLUMN_NAME )
                                .setTargetTableName( t.tableName( schemaName, ENTITY_TABLE_NAME ) )
                                .addTargetColumns( ENTITY_TABLE_PK_COLUMN_NAME )
                                .setOnUpdate( ReferentialAction.CASCADE )
                                .setOnDelete( ReferentialAction.CASCADE )
                                .createExpression(), ConstraintCharacteristics.INITIALLY_DEFERRED_DEFERRABLE
                            )
                        );

                    tablePKs.put( qNameInfo.getTableName(), 0L );
                }

                builder
                    .addTableElement(
                        d.createTableConstraintDefinition( d.createForeignKeyConstraintBuilder()
                            .addSourceColumns( ALL_QNAMES_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                            .setTargetTableName( t.tableName( schemaName, ALL_QNAMES_TABLE_NAME ) )
                            .addTargetColumns( ALL_QNAMES_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                            .setOnUpdate( ReferentialAction.CASCADE )
                            .setOnDelete( ReferentialAction.CASCADE )
                            .createExpression(), ConstraintCharacteristics.INITIALLY_DEFERRED_DEFERRABLE
                        )
                    );

                stmt.execute( this._vendor.toString( d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( schemaName, qNameInfo.getTableName() ) )
                    .setTableContentsSource( builder.createExpression() )
                    .createExpression()
                ) );

                //                stmt.execute( "COMMENT ON TABLE " + schemaName + "." + qNameInfo.getTableName() + " IS '"
                //                    + qNameInfo.getQName() + "'" );
                ps.setString( 1, qNameInfo.getQName().toString() );
                ps.setString( 2, qNameInfo.getTableName() );
                ps.execute();
            }
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
            SQLUtil.closeQuietly( ps );
        }

        // @formatter:off
    }

    private InsertStatement createInsertStatementForQNameInfo( Connection connection,
                                                               String schemaName, SQLVendor vendor )
    {
        ModificationFactory m = vendor.getModificationFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        LiteralFactory l = vendor.getLiteralFactory();

        return m.insert()
            .setTableName( t.tableName( schemaName, USED_QNAMES_TABLE_NAME ) )
            .setColumnSource( m.columnSourceByValues()
                .addValues( l.param(), l.param() )
                .createExpression()
            ).createExpression();
    }

    private void appendColumnDefinitionsForProperty( TableElementListBuilder builder,
                                                     QNameInfo qNameInfo )
    {
        Type finalType = qNameInfo.getFinalType();
        if( finalType instanceof ParameterizedType )
        {
            finalType = ( (ParameterizedType) finalType ).getRawType();
        }
        Class<?> finalClass = (Class<?>) finalType;
        SQLDataType sqlType = null;
        String valueRefTableName = null;
        String valueRefTablePKColumnName = null;
        if( qNameInfo.isFinalTypePrimitive() )
        {

            if( this._customizableTypes.keySet().contains( finalClass )
                && qNameInfo.getPropertyDescriptor().accessor()
                .isAnnotationPresent( SQLTypeInfo.class ) )
            {
                sqlType = this._customizableTypes.get( finalClass ).customizeType( finalClass,
                                                                                   qNameInfo.getPropertyDescriptor()
                    .accessor()
                    .getAnnotation( SQLTypeInfo.class ) );
            }
            else if( Enum.class.isAssignableFrom( finalClass ) )
            {
                // Enum - reference the lookup table
                sqlType = this._primitiveTypes.get( Integer.class );
                valueRefTableName = ENUM_LOOKUP_TABLE_NAME;
                valueRefTablePKColumnName = ENUM_LOOKUP_TABLE_PK_COLUMN_NAME;
            }
            else
            {
                // Primitive type, default sqlType
                sqlType = this._primitiveTypes.get( finalClass );
            }

            if( sqlType == null )
            {
                throw new InternalError( "Could not find sql type for java type [" + finalType + "]" );
            }
        }
        else
        {
            // Value composite - just need used class
            sqlType = this._primitiveTypes.get( Integer.class );
            valueRefTableName = USED_CLASSES_TABLE_NAME;
            valueRefTablePKColumnName = USED_CLASSES_TABLE_PK_COLUMN_NAME;
        }

        SQLVendor vendor = this._vendor;
        DefinitionFactory d = vendor.getDefinitionFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        builder
            .addTableElement(
                d.createColumnDefinition( QNAME_TABLE_VALUE_COLUMN_NAME, sqlType,
                                          qNameInfo.getCollectionDepth() > 0 ) )
            .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                    .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                    .addColumns( ALL_QNAMES_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                    .createExpression()
                ) );

        if( valueRefTableName != null && valueRefTablePKColumnName != null )
        {
            builder
                .addTableElement( d.createTableConstraintDefinition( d
                        .createForeignKeyConstraintBuilder()
                        .addSourceColumns( QNAME_TABLE_VALUE_COLUMN_NAME )
                        .setTargetTableName( t.tableName( this._state
                                .schemaName()
                                .get(), valueRefTableName ) )
                        .addTargetColumns( valueRefTablePKColumnName )
                        .setOnUpdate( ReferentialAction.CASCADE )
                        .setOnDelete( ReferentialAction.RESTRICT )
                        .createExpression(), ConstraintCharacteristics.NOT_DEFERRABLE
                    ) );
        }
    }

    protected Long getNextPK( Statement stmt, String schemaName, String columnName,
                              String tableName, Long defaultPK
    )
        throws SQLException
    {
        ResultSet rs = null;
        Long result = defaultPK;
        try
        {
            SQLVendor vendor = this._vendor;
            QueryFactory q = vendor.getQueryFactory();
            // Let's cheat a bit on SQL functions, so we won't need to use heavy query builder.
            // Also, currently there are no arithmetic statements
            rs
            = stmt.executeQuery(
                vendor
                .toString(
                    q.simpleQueryBuilder()
                    .select( "COUNT(" + columnName + ")", "MAX(" + columnName + ") + 1" )
                    .from(
                        vendor.getTableReferenceFactory().tableName( schemaName, tableName ) )
                    .createExpression()
                )
            );
            if( rs.next() )
            {
                Long count = rs.getLong( 1 );
                if( count > 0 )
                {
                    result = rs.getLong( 2 );
                }
            }
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
        }

        return result;
    }

    // This method assume that the schema exists
    private Boolean isReindexingNeeded( Connection connection )
        throws SQLException
    {
        Boolean result = true;
        String schemaName = this._state.schemaName().get();
        Statement stmt = connection.createStatement();
        try
        {
            QueryExpression getAppVersionQuery
                            = this._vendor
                .getQueryFactory()
                .simpleQueryBuilder()
                .select( APP_VERSION_PK_COLUMN_NAME )
                .from(
                    this._vendor.getTableReferenceFactory().tableName( schemaName,
                                                                       APP_VERSION_TABLE_NAME ) )
                .createExpression();
            ResultSet rs = null;
            try
            {
                rs = stmt.executeQuery( this._vendor.toString( getAppVersionQuery ) );
            }
            catch( SQLException sqle )
            {
                // Sometimes meta data claims table exists, even when it really doesn't exist
            }

            if( rs != null )
            {
                result = !rs.next();

                if( !result )
                {

                    String dbAppVersion = rs.getString( 1 );
                    if( this._reindexingStrategy != null )
                    {
                        result
                        = this._reindexingStrategy.reindexingNeeded( dbAppVersion,
                                                                     this._app.version() );
                    }
                }
            }
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
        }

        return result;
    }

    private String readAppVersionFromDB( Connection connection, String schemaName )
        throws SQLException
    {
        Statement stmt = connection.createStatement();
        String result = null;
        try
        {
            QueryExpression getAppVersionQuery
                            = this._vendor
                .getQueryFactory()
                .simpleQueryBuilder()
                .select( APP_VERSION_PK_COLUMN_NAME )
                .from(
                    this._vendor.getTableReferenceFactory().tableName( schemaName,
                                                                       APP_VERSION_TABLE_NAME ) )
                .createExpression();
            ResultSet rs = null;
            try
            {
                rs = stmt.executeQuery( getAppVersionQuery.toString() );

                if( rs.next() )
                {
                    result = rs.getString( 1 );
                }
            }
            catch( SQLException sqle )
            {
                // Sometimes meta data claims table exists, even when it really doesn't exist
            }
            finally
            {
                SQLUtil.closeQuietly( rs );
            }
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
        }

        return result;
    }

    private static void clearSchema( Connection connection, String schemaName, SQLVendor vendor )
        throws SQLException
    {
        ModificationFactory m = vendor.getModificationFactory();
        Statement stmt = null;
        try
        {
            connection.setReadOnly( false );
            stmt = connection.createStatement();
            stmt.execute( m.deleteBySearch().setTargetTable( m.createTargetTable(
                vendor.getTableReferenceFactory().tableName( schemaName, DBNames.ENTITY_TABLE_NAME ) ) )
                .createExpression().toString()
            );
            connection.commit();
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
        }
    }

    private void destroyNeededSchemaTables( Connection connection, String schemaName, int maxQNameUsed )
        throws SQLException
    {
        Statement stmt = connection.createStatement();
        try
        {
            this.dropTablesIfExist( schemaName, ENTITY_TABLE_NAME, stmt );
            this.dropTablesIfExist( schemaName, ALL_QNAMES_TABLE_NAME, stmt );
            this.dropTablesIfExist( schemaName, APP_VERSION_TABLE_NAME, stmt );
            this.dropTablesIfExist( schemaName, ENTITY_TYPES_TABLE_NAME, stmt );
            this.dropTablesIfExist( schemaName, ENTITY_TYPES_JOIN_TABLE_NAME, stmt );
            this.dropTablesIfExist( schemaName, ENUM_LOOKUP_TABLE_NAME, stmt );
            this.dropTablesIfExist( schemaName, USED_CLASSES_TABLE_NAME, stmt );
            this.dropTablesIfExist( schemaName, USED_QNAMES_TABLE_NAME, stmt );

            for( int x = 0; x <= maxQNameUsed; ++x )
            {
                this.dropTablesIfExist( schemaName, DBNames.QNAME_TABLE_NAME_PREFIX
                                                    + x, stmt );
            }
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
        }
    }

    private ApplicationInfo constructApplicationInfo( Boolean setQNameTableNameToNull )
        throws SQLException
    {
        final ApplicationInfo appInfo = new ApplicationInfo();
        final List<CompositeDescriptorInfo> valueDescriptors = new ArrayList<>();
        final Deque<Object> currentPath = new ArrayDeque<>();
        _app.descriptor().accept(
            new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
            {
                @Override
                public boolean visitEnter( Object visited )
                throws RuntimeException
                {
                    if( visited instanceof LayerDescriptor || visited instanceof ModuleDescriptor )
                    {
                        currentPath.push( visited );
                    }
                    if( visited instanceof EntityDescriptor || visited instanceof ValueDescriptor )
                    {
                        // TODO filter non-visible descriptors away.
                        if( visited instanceof EntityDescriptor )
                        {
                            EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
                            if( entityDescriptor.queryable() )
                            {
                                LOGGER.debug( "THIS ONE WORKS: {}", entityDescriptor );
                                appInfo.entityDescriptors.put( first( entityDescriptor.types() )
                                    .getName(), entityDescriptor );
                            }
                        }
                        else
                        {
                            valueDescriptors.add( new CompositeDescriptorInfo(
                                    (LayerDescriptor) Iterables
                                    .first( Iterables.skip( 1, currentPath ) ),
                                    (ModuleDescriptor) Iterables.first( currentPath ),
                                    (CompositeDescriptor) visited ) );
                        }

                        return false;
                    }

                    return true;
                }

                @Override
                public boolean visitLeave( Object visited )
                {
                    if( visited instanceof LayerDescriptor || visited instanceof ModuleDescriptor )
                    {
                        currentPath.pop();
                    }
                    return true;
                }
            } );

        for( EntityDescriptor descriptor : appInfo.entityDescriptors.values() )
        {
            Set<QualifiedName> newQNames = new HashSet<>();
            this.extractPropertyQNames( descriptor, this._state.qNameInfos().get(), newQNames,
                                        valueDescriptors,
                                        appInfo.enumValues, setQNameTableNameToNull );
            this.extractAssociationQNames( descriptor, this._state.qNameInfos().get(), newQNames,
                                           setQNameTableNameToNull );
            this.extractManyAssociationQNames( descriptor, this._state.qNameInfos().get(),
                                               newQNames,
                                               setQNameTableNameToNull );
            this._state.entityUsedQNames().get().put( descriptor, newQNames );
        }

        appInfo.usedValueComposites.addAll( valueDescriptors );
        return appInfo;
    }

    private void processPropertyTypeForQNames( PropertyDescriptor pType,
                                               Map<QualifiedName, QNameInfo> qNameInfos,
                                               Set<QualifiedName> newQNames,
                                               List<CompositeDescriptorInfo> vDescriptors,
                                               Set<String> enumValues,
                                               Boolean setQNameTableNameToNull
    )
    {
        QualifiedName qName = pType.qualifiedName();
        if( !newQNames.contains( qName ) && !qName.name().equals( Identity.class.getName() ) )
        {
            newQNames.add( qName );
            // System.out.println("QName: " + qName + ", hc: " + qName.hashCode());
            QNameInfo info = qNameInfos.get( qName );
            if( info == null )
            {
                info
                = QNameInfo.fromProperty(
                    //
                    qName, //
                    setQNameTableNameToNull ? null : ( QNAME_TABLE_NAME_PREFIX + qNameInfos
                                                      .size() ), //
                    pType//
                );
                qNameInfos.put( qName, info );
            }
            Type vType = info.getFinalType();

            while( vType instanceof ParameterizedType )
            {
                vType = ( (ParameterizedType) vType ).getRawType();
            }
            if( vType instanceof Class<?> ) //
            {
                final Class<?> vTypeClass = (Class<?>) vType;
                if( ( (Class<?>) vType ).isInterface() )
                {
                    for( CompositeDescriptorInfo descInfo : vDescriptors )
                    {
                        CompositeDescriptor desc = descInfo.composite;
                        if( desc instanceof ValueDescriptor )
                        {
                            ValueDescriptor vDesc = (ValueDescriptor) desc;
                            // TODO this doesn't understand, say, Map<String, String>, or indeed,
                            // any
                            // other Serializable
                            if( Iterables.matchesAny( new Specification<Class<?>>()
                            {
                                @Override
                                public boolean satisfiedBy( Class<?> item )
                                {
                                    return vTypeClass.isAssignableFrom( item );
                                }
                            }, vDesc.types() ) )
                            {
                                for( PropertyDescriptor subPDesc : vDesc.state().properties() )
                                {
                                    this.processPropertyTypeForQNames( //
                                        subPDesc, //
                                        qNameInfos, //
                                        newQNames, //
                                        vDescriptors, //
                                        enumValues, //
                                        setQNameTableNameToNull //
                                    );
                                }
                            }
                        }
                    }
                }
                else if( Enum.class.isAssignableFrom( (Class<?>) vType ) )
                {
                    for( Object value : ( (Class<?>) vType ).getEnumConstants() )
                    {
                        enumValues.add( QualifiedName
                            .fromClass( (Class<?>) vType, value.toString() ).toString() );
                    }
                }
            }
        }
    }

    private void extractPropertyQNames( EntityDescriptor entityDesc,
                                        Map<QualifiedName, QNameInfo> qNameInfos,
                                        Set<QualifiedName> newQNames,
                                        List<CompositeDescriptorInfo> vDescriptors,
                                        Set<String> enumValues,
                                        Boolean setQNameTableNameToNull
    )
    {
        for( PropertyDescriptor pDesc : entityDesc.state().properties() )
        {
            if( SQLSkeletonUtil.isQueryable( pDesc.accessor() ) )
            {
                this.processPropertyTypeForQNames( //
                    pDesc, //
                    qNameInfos, //
                    newQNames, //
                    vDescriptors, //
                    enumValues, //
                    setQNameTableNameToNull //
                );
            }
        }
    }

    private void extractAssociationQNames( EntityDescriptor entityDesc,
                                           Map<QualifiedName, QNameInfo> extractedQNames,
                                           Set<QualifiedName> newQNames, Boolean setQNameTableNameToNull
    )
    {
        for( AssociationDescriptor assoDesc : entityDesc.state().associations() )
        {
            if( SQLSkeletonUtil.isQueryable( assoDesc.accessor() ) )
            {
                QualifiedName qName = assoDesc.qualifiedName();
                if( !extractedQNames.containsKey( qName ) )
                {
                    extractedQNames.put( qName,//
                                         QNameInfo.fromAssociation( //
                        qName, //
                        setQNameTableNameToNull ? null
                        : ( QNAME_TABLE_NAME_PREFIX + extractedQNames
                           .size() ), //
                        assoDesc //
                    ) //
                    );
                    newQNames.add( qName );
                }
            }
        }
    }

    private void extractManyAssociationQNames( EntityDescriptor entityDesc,
                                               Map<QualifiedName, QNameInfo> extractedQNames,
                                               Set<QualifiedName> newQNames,
                                               Boolean setQNameTableNameToNull
    )
    {
        for( AssociationDescriptor mAssoDesc : entityDesc.state().manyAssociations() )
        {
            QualifiedName qName = mAssoDesc.qualifiedName();
            if( SQLSkeletonUtil.isQueryable( mAssoDesc.accessor() ) )
            {
                if( !extractedQNames.containsKey( qName ) )
                {
                    extractedQNames.put( //
                        qName, //
                        QNameInfo.fromManyAssociation( //
                        qName, //
                        setQNameTableNameToNull ? null
                        : ( QNAME_TABLE_NAME_PREFIX + extractedQNames
                           .size() ), //
                        mAssoDesc //
                    ) //
                    );
                    newQNames.add( qName );
                }
            }
        }
    }

    protected abstract void testRequiredCapabilities( Connection connection )
        throws SQLException;

    protected boolean dropTablesIfExist(
        String schemaName,
        String tableName,
        Statement stmt
    )
        throws SQLException
    {
        boolean result = false;
        try
        {
            stmt.execute( this._vendor.toString( this._vendor.getManipulationFactory()
                .createDropTableOrViewStatement(
                    this._vendor
                    .getTableReferenceFactory()
                    .tableName( schemaName, tableName ), ObjectType.TABLE,
                    DropBehaviour.CASCADE
                ) ) );
            result = true;
        }
        catch( SQLException sqle )
        {
            // Ignore
        }
        return result;
    }

    private static final String DESCRIPTOR_COMPONENT_SEPARATOR_START = "{";
    private static final String DESCRIPTOR_COMPONENT_SEPARATOR_END = "}";
    private static final String DESCRIPTOR_TYPE_SEPARATOR = ",";
    private static final Pattern DESCRIPTOR_TYPES_REGEXP = Pattern.compile(
        "[^" + Pattern.quote( DESCRIPTOR_TYPE_SEPARATOR ) + "]+" );
    private static final Pattern DESCRIPTOR_TEXTUAL_REGEXP = Pattern.compile(
        "^"
        + Pattern.quote( DESCRIPTOR_COMPONENT_SEPARATOR_START ) + "(.*)"
        + Pattern.quote( DESCRIPTOR_COMPONENT_SEPARATOR_END )
        + Pattern.quote( DESCRIPTOR_COMPONENT_SEPARATOR_START ) + "(.*)"
        + Pattern.quote( DESCRIPTOR_COMPONENT_SEPARATOR_END )
        + Pattern.quote( DESCRIPTOR_COMPONENT_SEPARATOR_START ) + "(" + "[^"
        + Pattern.quote( DESCRIPTOR_COMPONENT_SEPARATOR_END + DESCRIPTOR_TYPE_SEPARATOR )
        + "]+)" + Pattern.quote( DESCRIPTOR_COMPONENT_SEPARATOR_END ) + "$" );

    protected static String compositeDescriptorToString( LayerDescriptor layer,
                                                         ModuleDescriptor module, CompositeDescriptor descriptor )
    {
        return DESCRIPTOR_COMPONENT_SEPARATOR_START + layer.name()
               + DESCRIPTOR_COMPONENT_SEPARATOR_END + DESCRIPTOR_COMPONENT_SEPARATOR_START
               + module.name() + DESCRIPTOR_COMPONENT_SEPARATOR_END
               + DESCRIPTOR_COMPONENT_SEPARATOR_START
               + Iterables.toString( descriptor.types(), new Function<Class<?>, String>()
        {
            @Override
            public String map( Class<?> item )
            {
                return item.getName();
            }
        }, DESCRIPTOR_TYPE_SEPARATOR ) + DESCRIPTOR_COMPONENT_SEPARATOR_END;
    }

    protected static <TCompositeDescriptor extends CompositeDescriptor> TCompositeDescriptor
        stringToCompositeDescriptor( final Class<TCompositeDescriptor> descriptorClass,
                                     ApplicationDescriptor appDesc, String str )
    {
        Matcher matcher = DESCRIPTOR_TEXTUAL_REGEXP.matcher( str );
        if( !matcher.matches() )
        {
            throw new IllegalArgumentException( "Descriptor textual description " + str
                                                + " was invalid." );
        }

        final String layerName = matcher.group( 1 );
        final String moduleName = matcher.group( 2 );
        final Set<String> classNames = new HashSet<>();
        Matcher typesMatcher = DESCRIPTOR_TYPES_REGEXP.matcher( matcher.group( 3 ) );
        while( typesMatcher.find() )
        {
            classNames.add( typesMatcher.group( 0 ) );
        }
        final CompositeDescriptor[] result = new CompositeDescriptor[ 1 ];

        appDesc.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
            {
                boolean thisResult = true;
                if( visited instanceof LayerDescriptor )
                {
                    thisResult = ( (LayerDescriptor) visited ).name().equals( layerName );
                }
                else if( visited instanceof ModuleDescriptor )
                {
                    thisResult = ( (ModuleDescriptor) visited ).name().equals( moduleName );
                }
                else if( descriptorClass.isAssignableFrom( visited.getClass() ) )
                {
                    CompositeDescriptor desc = (CompositeDescriptor) visited;
                    if( classNames.equals( new HashSet<>( Iterables.toList( Iterables.map(
                        new Function<Class<?>, String>()
                        {
                            @Override
                            public String map( Class<?> from )
                            {
                                return from.getName();
                            }
                        }, desc.types() ) ) ) ) )
                    {
                        result[0] = desc;
                        thisResult = false;
                    }
                }
                return thisResult;
            }

            @Override
            public boolean visitLeave( Object visited )
            {
                return result[0] == null;
            }
        } );

        return (TCompositeDescriptor) result[0];
    }

    protected abstract void modifyPrimitiveTypes( Map<Class<?>, SQLDataType> primitiveTypes,
                                                  Map<Class<?>, Integer> jdbcTypes
    );

    protected abstract SQLDataType getCollectionPathDataType();
}
