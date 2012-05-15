/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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

package org.qi4j.index.sql.support.skeletons;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.Application;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import static org.qi4j.functional.Iterables.first;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.index.sql.support.api.SQLAppStartup;
import org.qi4j.index.sql.support.api.SQLTypeInfo;
import org.qi4j.index.sql.support.common.DBNames;
import static org.qi4j.index.sql.support.common.DBNames.*;
import org.qi4j.index.sql.support.common.EntityTypeInfo;
import org.qi4j.index.sql.support.common.QNameInfo;
import org.qi4j.index.sql.support.common.QNameInfo.QNameType;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.grammar.builders.definition.TableElementListBuilder;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
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
import org.sql.generation.api.grammar.query.QueryExpression;
import org.sql.generation.api.vendor.SQLVendor;

/**
 * @author Stanislav Muhametsin
 */
public abstract class AbstractSQLStartup
    implements SQLAppStartup, Activatable
{
    private interface SQLTypeCustomizer
    {
        SQLDataType customizeType( Type propertyType, SQLTypeInfo sqlTypeInfo );
    }

    public static final String DEFAULT_SCHEMA_NAME = "qi4j";

    private static final Logger _log = LoggerFactory.getLogger( AbstractSQLStartup.class.getName() );

    @This
    private SQLDBState _state;

    @Uses
    private ServiceDescriptor descriptor;

    @This
    private Configuration<SQLConfiguration> _configuration;

    @Service
    private ReindexingStrategy _reindexingStrategy;

    @Service
    private DataSource _dataSource;

    @Service
    private Reindexer _reindexer;

    @Structure
    private Application _app;

    private SQLVendor _vendor;

    private Map<Class<?>, SQLTypeCustomizer> _customizableTypes;

    private Map<Class<?>, SQLDataType> _primitiveTypes;

    public void activate()
        throws Exception
    {
        this._vendor = this.descriptor.metaInfo( SQLVendor.class );
        this.setVendor( this._vendor );
        this.initTypes();
        this.modifyPrimitiveTypes( this._primitiveTypes, this._state.javaTypes2SQLTypes().get() );
    }

    public void passivate()
        throws Exception
    {
    }

    public void initConnection()
        throws SQLException
    {
        String schemaName = this._configuration.configuration().schemaName().get();
        if( schemaName == null )
        {
            schemaName = DEFAULT_SCHEMA_NAME;
        }
        else
        {
            this.checkSchemaName( schemaName );
        }
        _log.debug( "Will use '{}' as schema name", schemaName );

        this._state.schemaName().set( schemaName );
        this._state.tablePKs().set( new HashMap<String, Long>() );
        this._state.usedClassesPKs().set( new HashMap<String, Integer>() );
        this._state.entityTypeInfos().set( new HashMap<String, EntityTypeInfo>() );
        this._state.entityUsedQNames().set( new HashMap<String, Set<QualifiedName>>() );
        this._state.qNameInfos().set( new HashMap<QualifiedName, QNameInfo>() );
        this._state.enumPKs().set( new HashMap<String, Integer>() );

        Connection connection = this._dataSource.getConnection();
        Boolean wasAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit( true );
        try
        {
            this.syncDB();
        }
        finally
        {
            connection.setAutoCommit( wasAutoCommit );
            SQLUtil.closeQuietly( connection );
        }
        
        if ( _log.isDebugEnabled() ) {

            String newline = "\n";
            String tab = "\t";
            String colonspace = ": ";
            StringBuilder report = new StringBuilder();

            report.append( "schemaName: " ).append( _state.schemaName().get() ).append( newline );

            report.append( "qNameInfos: " ).append( newline );
            for ( Map.Entry<QualifiedName, QNameInfo> entry : _state.qNameInfos().get().entrySet() ) {
                report.append( tab ).append( entry.getKey() ).append( colonspace ).append( entry.getValue() ).append( newline );
            }

            report.append( "entityUsedQNames:" ).append( newline );
            for ( Map.Entry<String, Set<QualifiedName>> entry : _state.entityUsedQNames().get().entrySet() ) {
                report.append( tab ).append( entry.getKey() ).append( colonspace ).append( entry.getValue() ).append( newline );
            }

            report.append( "tablePKs:" ).append( newline );
            for ( Map.Entry<String, Long> entry : _state.tablePKs().get().entrySet() ) {
                report.append( tab ).append( entry.getKey() ).append( colonspace ).append( entry.getValue() ).append( newline );
            }

            report.append( "usedClassesPKs:" ).append( newline );
            for ( Map.Entry<String, Integer> entry : _state.usedClassesPKs().get().entrySet() ) {
                report.append( tab ).append( entry.getKey() ).append( colonspace ).append( entry.getValue() ).append( newline );
            }

            report.append( "entityTypeInfos:" ).append( newline );
            for ( Map.Entry<String, EntityTypeInfo> entry : _state.entityTypeInfos().get().entrySet() ) {
                report.append( tab ).append( entry.getKey() ).append( colonspace ).append( entry.getValue() ).append( newline );
            }

            report.append( "javaTypes2SQLTypes:" ).append( newline );
            for ( Map.Entry<Class<?>, Integer> entry : _state.javaTypes2SQLTypes().get().entrySet() ) {
                report.append( tab ).append( entry.getKey() ).append( colonspace ).append( entry.getValue() ).append( newline );
            }

            report.append( "enumPKs:" ).append( newline );
            for ( Map.Entry<String, Integer> entry : _state.enumPKs().get().entrySet() ) {
                report.append( tab ).append( entry.getKey() ).append( colonspace ).append( entry.getValue() ).append( newline );
            }

            _log.debug( "SQLDBState after initConnection:\n{}", report.toString() );
        }
    }

    private void initTypes()
    {

        DataTypeFactory dt = this._vendor.getDataTypeFactory();

        this._primitiveTypes = new HashMap<Class<?>, SQLDataType>();
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

        Map<Class<?>, Integer> jdbcTypes = new HashMap<Class<?>, Integer>();
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

        this._customizableTypes = new HashMap<Class<?>, SQLTypeCustomizer>();
        this._customizableTypes.put( //
                                     String.class, //
                                     new SQLTypeCustomizer()
                                     {
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

        private Map<String, EntityDescriptor> entityDescriptors = new HashMap<String, EntityDescriptor>();

        private Set<String> usedClassNames = new HashSet<String>();

        private Set<String> enumValues = new HashSet<String>();

    }

    private void syncDB()
        throws SQLException
    {
        Connection connection = this._dataSource.getConnection();
        String schemaName = this._state.schemaName().get();
        ResultSet rs = connection.getMetaData().getSchemas();

        Boolean schemaFound = false;
        try
        {
            while( rs.next() && !schemaFound )
            {
                schemaFound = rs.getString( 1 ).equals( schemaName );
            }

            Boolean reindexingRequired = schemaFound ? this.isReindexingNeeded() : false;
            ApplicationInfo appInfo = this.constructApplicationInfo( false ); // !reindexingRequired );

            if( schemaFound && reindexingRequired )
            {
                _log.debug( "Schema Found & Reindexing Required" );
                this.clearSchema();
                _log.debug( "Reindexing needed, Application metadata from database has been cleared." );
            }
            if( schemaFound && !reindexingRequired )
            {
                _log.debug( "Schema Found & Reindexing NOT Required" );
                this.testRequiredCapabilities();
                _log.debug( "Underlying database fullfill required capabilities" );
                this.readAppMetadataFromDB( appInfo.entityDescriptors );
                _log.debug( "Application metadata loaded from database" );
            }
            else
            {
                _log.debug( "Schema {}Found & Reindexing {}Required", schemaFound ? "" : "NOT ", reindexingRequired ? "" : "NOT " );
                this.createSchema( schemaFound );
                this.writeAppMetadataToDB( appInfo );

                if( reindexingRequired )
                {
                    this.performReindex( connection );
                }
            }
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
            SQLUtil.closeQuietly( connection );
        }
    }

    private void createSchema( Boolean schemaFound )
        throws SQLException
    {
        Connection connection = this._dataSource.getConnection();
        String schemaName = this._state.schemaName().get();

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
                _log.debug( "Database schema created" );
            }
            
            this.testRequiredCapabilities();
            _log.debug( "Underlying database fullfill required capabilities" );
                        
            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                        .setTableName( t.tableName( schemaName, USED_CLASSES_TABLE_NAME ) )
                        .setTableContentsSource(
                            d.createTableElementListBuilder()
                                .addTableElement( d.createColumnDefinition( USED_CLASSES_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                    .get( Integer.class ), false ) )
                                .addTableElement( d.createColumnDefinition( USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME, this
                                    ._primitiveTypes
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

            this._state.tablePKs().get().put( USED_CLASSES_TABLE_NAME, 0L );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                        .setTableName( t.tableName( schemaName, ENTITY_TYPES_TABLE_NAME ) )
                        .setTableContentsSource(
                            d.createTableElementListBuilder()
                                .addTableElement( d.createColumnDefinition( ENTITY_TYPES_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                    .get( Integer.class ), false ) )
                                .addTableElement( d.createColumnDefinition( ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME, this
                                    ._primitiveTypes
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

            this._state.tablePKs().get().put( ENTITY_TYPES_TABLE_NAME, 0L );

            ResultSet rs = null;
            try
            {
                rs = connection.getMetaData().getTables( null, schemaName, ENTITY_TABLE_NAME, new String[]
                                                         {
                                                             "TABLE"
                                                         } );
                if( !rs.next() )
                {
                    stmt.execute(
                        vendor.toString(
                            d.createTableDefinitionBuilder()
                                .setTableName( t.tableName( schemaName, ENTITY_TABLE_NAME ) )
                                .setTableContentsSource(
                                    d.createTableElementListBuilder()
                                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_PK_COLUMN_NAME, this._primitiveTypes
                                            .get( Long.class ), false ) )
                                        .addTableElement( d.createColumnDefinition( ENTITY_TYPES_TABLE_PK_COLUMN_NAME, this
                                            ._primitiveTypes
                                            .get( Integer.class ), false ) )
                                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_IDENTITY_COLUMN_NAME, this
                                            ._primitiveTypes
                                            .get( String.class ), false ) )
                                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_MODIFIED_COLUMN_NAME, this
                                            ._primitiveTypes
                                            .get( Date.class ), false ) )
                                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_VERSION_COLUMN_NAME, this
                                            ._primitiveTypes
                                            .get( String.class ), false ) )
                                        .addTableElement( d.createColumnDefinition( ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME, this
                                            ._primitiveTypes
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
                                        .addTableElement( d.createTableConstraintDefinition( d.createForeignKeyConstraintBuilder()
                                                                                                 .addSourceColumns( ENTITY_TYPES_TABLE_PK_COLUMN_NAME )
                                                                                                 .setTargetTableName( t.tableName( schemaName, ENTITY_TYPES_TABLE_NAME ) )
                                                                                                 .addTargetColumns( ENTITY_TYPES_TABLE_PK_COLUMN_NAME )
                                                                                                 .setOnDelete( ReferentialAction.RESTRICT )
                                                                                                 .setOnUpdate( ReferentialAction.CASCADE )
                                                                                                 .createExpression()
                                        ) )
                                        .createExpression()
                                )
                                .createExpression()
                        )
                    );
                    this._state.tablePKs().get().put( ENTITY_TABLE_NAME, 0L );
                }
                else
                {
                    this._state
                        .tablePKs()
                        .get()
                        .put(
                            ENTITY_TABLE_NAME,
                            this.getNextPK( stmt, schemaName, ENTITY_TABLE_PK_COLUMN_NAME,
                                            ENTITY_TABLE_NAME, 0L ) );
                }
            }
            finally
            {
                SQLUtil.closeQuietly( rs );
            }

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

            this._state.tablePKs().get().put( ENUM_LOOKUP_TABLE_NAME, 0L );

            stmt.execute(
                vendor.toString(
                    d.createTableDefinitionBuilder()
                        .setTableName( t.tableName( schemaName, USED_QNAMES_TABLE_NAME ) )
                        .setTableContentsSource(
                            d.createTableElementListBuilder()
                                .addTableElement( d.createColumnDefinition( USED_QNAMES_TABLE_QNAME_COLUMN_NAME, this._primitiveTypes
                                    .get( String.class ), false ) )
                                .addTableElement( d.createColumnDefinition( USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME, this
                                    ._primitiveTypes
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
                                    .get( Long.class ), false ) )
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

            this._state.tablePKs().get().put( ALL_QNAMES_TABLE_NAME, 0L );

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
            SQLUtil.closeQuietly( connection );
        }

        // @formatter:on
        _log.debug( "Indexing SQL database tables created" );
    }

    private void performReindex( Connection connection )
        throws SQLException
    {
        _log.info( "Performing reindexing..." );
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

        this._reindexer.reindex();
        _log.info( "Reindexing complete." );
    }

    private void readAppMetadataFromDB( Map<String, EntityDescriptor> entityDescriptors )
        throws SQLException
    {

        String schemaName = this._state.schemaName().get();
        Connection connection = this._dataSource.getConnection();
        Statement stmt = connection.createStatement();

        SQLVendor vendor = this._vendor;
        QueryFactory q = vendor.getQueryFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        try
        {
            // @formatter:off
            Map<String, Long> pks = this._state.tablePKs().get();
            pks.put( ENTITY_TABLE_NAME, this.getNextPK( stmt, schemaName,
                                                        DBNames.ENTITY_TABLE_PK_COLUMN_NAME, DBNames.ENTITY_TABLE_NAME, 0L ) );

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

            long pk = 0L;
            while( rs.next() )
            {
                pk = rs.getInt( 1 );
                String entityTypeName = rs.getString( 2 );
                this._state.entityTypeInfos().get()
                    .put( entityTypeName, new EntityTypeInfo( entityDescriptors.get( entityTypeName ), (int) pk ) );

                if( !this._state.tablePKs().get().containsKey( ENTITY_TYPES_TABLE_NAME )
                    || this._state.tablePKs().get().get( ENTITY_TYPES_TABLE_NAME ) <= pk )
                {

                    this._state.tablePKs().get().put( ENTITY_TYPES_TABLE_NAME, pk + 1 );
                }
            }

            rs = stmt.executeQuery(
                vendor.toString(
                    q.simpleQueryBuilder()
                        .select( USED_CLASSES_TABLE_PK_COLUMN_NAME, USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME )
                        .from( t.tableName( schemaName, USED_CLASSES_TABLE_NAME ) )
                        .createExpression()
                )
            );

            while( rs.next() )
            {
                pk = rs.getInt( 1 );
                String className = rs.getString( 2 );
                if( !this._state.tablePKs().get().containsKey( USED_CLASSES_TABLE_NAME )
                    || this._state.tablePKs().get().get( USED_CLASSES_TABLE_NAME ) <= pk )
                {
                    this._state.tablePKs().get().put( USED_CLASSES_TABLE_NAME, pk + 1 );
                }
                this._state.usedClassesPKs().get().put( className, (int) pk );
            }

            rs = stmt.executeQuery(
                vendor.toString(
                    q.simpleQueryBuilder()
                        .select( ENUM_LOOKUP_TABLE_PK_COLUMN_NAME, ENUM_LOOKUP_TABLE_ENUM_VALUE_NAME )
                        .from( t.tableName( schemaName, ENUM_LOOKUP_TABLE_NAME ) )
                        .createExpression()
                )
            );

            while( rs.next() )
            {
                pk = rs.getInt( 1 );
                String enumName = rs.getString( 2 );
                if( !this._state.tablePKs().get().containsKey( ENUM_LOOKUP_TABLE_NAME )
                    || this._state.tablePKs().get().get( ENUM_LOOKUP_TABLE_NAME ) <= pk )
                {
                    this._state.tablePKs().get().put( ENUM_LOOKUP_TABLE_NAME, pk + 1 );
                }
                this._state.enumPKs().get().put( enumName, (int) pk );
            }
            // @formatter:on
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
            SQLUtil.closeQuietly( connection );
        }
    }

    private void writeAppMetadataToDB( ApplicationInfo appInfo )
        throws SQLException
    {
        Connection connection = this._dataSource.getConnection();
        String schemaName = this._state.schemaName().get();

        SQLVendor vendor = this._vendor;
        ModificationFactory m = vendor.getModificationFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        
        try {

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
                for( EntityDescriptor descriptor : appInfo.entityDescriptors.values() )
                {
                    String entityTypeName = first( descriptor.types() ).getName();
                    long pk = this._state.tablePKs().get().get( ENTITY_TYPES_TABLE_NAME );
                    ps.setInt( 1, (int) pk );
                    ps.setString( 2, entityTypeName );
                    ps.executeUpdate();
                    this._state.entityTypeInfos().get().put( entityTypeName, new EntityTypeInfo( descriptor, (int) pk ) );
                    this._state.tablePKs().get().put( ENTITY_TYPES_TABLE_NAME, pk + 1 );
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
                for( String usedClass : appInfo.usedClassNames )
                {
                    long pk = this._state.tablePKs().get().get( USED_CLASSES_TABLE_NAME );
                    ps.setInt( 1, (int) pk );
                    ps.setString( 2, usedClass );
                    ps.executeUpdate();
                    this._state.usedClassesPKs().get().put( usedClass, (int) pk );
                    this._state.tablePKs().get().put( USED_CLASSES_TABLE_NAME, pk + 1 );
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
                    long pk = this._state.tablePKs().get().get( ENUM_LOOKUP_TABLE_NAME );
                    ps.setInt( 1, (int) pk );
                    ps.setString( 2, enumValue );
                    ps.executeUpdate();
                    this._state.enumPKs().get().put( enumValue, (int) pk );
                    this._state.tablePKs().get().put( ENUM_LOOKUP_TABLE_NAME, pk + 1 );
                }
            }
            finally
            {
                SQLUtil.closeQuietly( ps );
            }

            Statement stmt = connection.createStatement();
            ps = connection.prepareStatement(
                vendor.toString(
                    m.insert()
                        .setTableName( t.tableName( schemaName, USED_QNAMES_TABLE_NAME ) )
                        .setColumnSource( m.columnSourceByValues()
                                            .addValues( l.param(), l.param() )
                                            .createExpression()
                        )
                        .createExpression()
                )
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
                            .get( Long.class ), false ) );

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
                                    .get( Long.class ), false ) )
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
                                    .get( Long.class ), false ) )
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

                        this._state.tablePKs().get().put( qNameInfo.getTableName(), 0L );
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
        
        }
        finally
        {
            SQLUtil.closeQuietly( connection );
        }

        // @formatter:off
    }

    private void appendColumnDefinitionsForProperty( TableElementListBuilder builder, QNameInfo qNameInfo )
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
                && qNameInfo.getPropertyDescriptor().accessor().isAnnotationPresent( SQLTypeInfo.class ) )
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
            .addTableElement( d.createColumnDefinition( QNAME_TABLE_VALUE_COLUMN_NAME, sqlType, qNameInfo.getCollectionDepth() > 0 ) )
            .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                                                                     .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                                                                     .addColumns( ALL_QNAMES_TABLE_PK_COLUMN_NAME, ENTITY_TABLE_PK_COLUMN_NAME )
                                                                     .createExpression()
            ) );

        if( valueRefTableName != null && valueRefTablePKColumnName != null )
        {
            builder
                .addTableElement( d.createTableConstraintDefinition( d.createForeignKeyConstraintBuilder()
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
            rs = stmt.executeQuery(
                vendor.toString(
                    q.simpleQueryBuilder()
                        .select( "COUNT(" + columnName + ")", "MAX(" + columnName + ") + 1" )
                        .from( vendor.getTableReferenceFactory().tableName( schemaName, tableName ) )
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
    private Boolean isReindexingNeeded()
            throws SQLException
    {
        Boolean result = true;
        Connection connection = this._dataSource.getConnection();
        String schemaName = this._state.schemaName().get();
        Statement stmt = connection.createStatement();
        try
        {
            QueryExpression getAppVersionQuery = this._vendor.getQueryFactory().simpleQueryBuilder()
                .select( APP_VERSION_PK_COLUMN_NAME )
                .from( this._vendor.getTableReferenceFactory().tableName( schemaName, APP_VERSION_TABLE_NAME ) )
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
                        result = this._reindexingStrategy.reindexingNeeded( dbAppVersion, this._app.version() );
                    }
                }
            }
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
            SQLUtil.closeQuietly( connection );
        }

        return result;
    }

    // Only used by isReindexingNeeded
    private void clearSchema()
        throws SQLException
    {
        Connection connection = this._dataSource.getConnection();
        String schemaName = this._state.schemaName().get();
        DatabaseMetaData metaData = connection.getMetaData();

        Statement stmt = connection.createStatement();
        try
        {

            // Don't drop all entities table.
            this.dropTablesIfExist( metaData, schemaName, ALL_QNAMES_TABLE_NAME, stmt );
            this.dropTablesIfExist( metaData, schemaName, APP_VERSION_TABLE_NAME, stmt );
            this.dropTablesIfExist( metaData, schemaName, ENTITY_TYPES_TABLE_NAME, stmt );
            this.dropTablesIfExist( metaData, schemaName, ENUM_LOOKUP_TABLE_NAME, stmt );
            this.dropTablesIfExist( metaData, schemaName, USED_CLASSES_TABLE_NAME, stmt );
            this.dropTablesIfExist( metaData, schemaName, USED_QNAMES_TABLE_NAME, stmt );

            Integer x = 0;
            while( this.dropTablesIfExist( metaData, schemaName, DBNames.QNAME_TABLE_NAME_PREFIX + x, stmt ) )
            {
                ++x;
            }
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
            SQLUtil.closeQuietly( connection );
        }
    }

    private ApplicationInfo constructApplicationInfo( Boolean setQNameTableNameToNull )
        throws SQLException
    {
        final ApplicationInfo appInfo = new ApplicationInfo();
        final List<ValueDescriptor> valueDescriptors = new ArrayList<ValueDescriptor>();
        _app.descriptor().accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof EntityDescriptor || visited instanceof ValueDescriptor)
                {
                    if( visited instanceof EntityDescriptor )
                    {
                        EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
                        if( entityDescriptor.queryable() )
                        {
                            _log.debug( "THIS ONE WORKS: {}",entityDescriptor );
                            appInfo.entityDescriptors.put( first( entityDescriptor.types() ).getName(), entityDescriptor );
                        }
                    }
                    else
                    {
                        valueDescriptors.add( (ValueDescriptor) visited );
                    }

                    return false;
                }

                return true;
            }
        } );

        Set<String> usedVCClassNames = new HashSet<String>();
        for( EntityDescriptor descriptor : appInfo.entityDescriptors.values() )
        {
            Set<QualifiedName> newQNames = new HashSet<QualifiedName>();
            this.extractPropertyQNames( descriptor, this._state.qNameInfos().get(), newQNames, valueDescriptors,
                                        usedVCClassNames, appInfo.enumValues, setQNameTableNameToNull );
            this.extractAssociationQNames( descriptor, this._state.qNameInfos().get(), newQNames,
                                           setQNameTableNameToNull );
            this.extractManyAssociationQNames( descriptor, this._state.qNameInfos().get(), newQNames,
                                               setQNameTableNameToNull );
            this._state.entityUsedQNames().get().put( first( descriptor.types() ).getName(), newQNames );
        }

        appInfo.usedClassNames.addAll( usedVCClassNames );
        return appInfo;
    }

    private void processPropertyTypeForQNames( PropertyDescriptor pType, Map<QualifiedName, QNameInfo> qNameInfos,
                                               Set<QualifiedName> newQNames,
                                               List<ValueDescriptor> vDescriptors,
                                               Set<String> usedVCClassNames,
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
                info = QNameInfo.fromProperty( //
                                               qName, //
                                               setQNameTableNameToNull ? null : ( QNAME_TABLE_NAME_PREFIX + qNameInfos.size() ), //
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
                if( ( (Class<?>) vType ).isInterface() )
                {
                    for( ValueDescriptor vDesc : vDescriptors )
                    {
                        String vcTypeName = first( vDesc.types() ).getName();
                        // TODO this doesn't understand, say, Map<String, String>, or indeed, any other Serializable
                        if( ( (Class<?>) vType ).isAssignableFrom( first( vDesc.types() ) ) )
                        {
                            usedVCClassNames.add( vcTypeName );
                            for( PropertyDescriptor subPDesc : vDesc.state().properties() )
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
                }
                else if( Enum.class.isAssignableFrom( (Class<?>) vType ) )
                {
                    for( Object value : ( (Class<?>) vType ).getEnumConstants() )
                    {
                        enumValues.add( QualifiedName.fromClass( (Class<?>) vType, value.toString() ).toString() );
                    }
                }
            }
        }
    }

    private void extractPropertyQNames( EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> qNameInfos,
                                        Set<QualifiedName> newQNames,
                                        List<ValueDescriptor> vDescriptors,
                                        Set<String> usedVCClassNames,
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
                                                   usedVCClassNames, //
                                                   enumValues, //
                                                   setQNameTableNameToNull //
                );
            }
        }
    }

    private void extractAssociationQNames( EntityDescriptor entityDesc, Map<QualifiedName, QNameInfo> extractedQNames,
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
                                                                    setQNameTableNameToNull ? null : ( QNAME_TABLE_NAME_PREFIX + extractedQNames
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
                                                                        setQNameTableNameToNull ? null : ( QNAME_TABLE_NAME_PREFIX + extractedQNames
                                                                            .size() ), //
                                                                        mAssoDesc //
                                         ) //
                    );
                    newQNames.add( qName );
                }
            }
        }
    }

    protected abstract void testRequiredCapabilities()
        throws SQLException;

    protected boolean dropTablesIfExist( DatabaseMetaData metaData,
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
                                                             .tableName( schemaName, tableName ), ObjectType.TABLE, DropBehaviour.CASCADE
                                                     ) ) );
            result = true;
        }
        catch( SQLException sqle )
        {
            // Ignore
        }
        return result;
    }

    protected abstract void modifyPrimitiveTypes( Map<Class<?>, SQLDataType> primitiveTypes,
                                                  Map<Class<?>, Integer> jdbcTypes
    );

    protected abstract SQLDataType getCollectionPathDataType();

    protected abstract void setVendor( SQLVendor vendor );
}
