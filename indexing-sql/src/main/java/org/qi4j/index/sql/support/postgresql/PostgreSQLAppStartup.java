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

package org.qi4j.index.sql.support.postgresql;

import static org.qi4j.index.sql.support.common.DBNames.ALL_QNAMES_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ALL_QNAMES_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ALL_QNAMES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.APP_VERSION_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.APP_VERSION_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.APP_VERSION_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_APPLICATION_VERSION_COLUMN_DATATYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_IDENTITY_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_MODIFIED_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_MODIFIED_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_VERSION_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TABLE_VERSION_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENUM_LOOKUP_TABLE_ENUM_VALUE_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENUM_LOOKUP_TABLE_ENUM_VALUE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENUM_LOOKUP_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.ENUM_LOOKUP_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.ENUM_LOOKUP_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_COLLECTION_PATH_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_NAME_PREFIX;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.QNAME_TABLE_VALUE_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_CLASSES_TABLE_CLASS_NAME_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_CLASSES_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_CLASSES_TABLE_PK_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.USED_CLASSES_TABLE_PK_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_QNAMES_TABLE_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_QNAMES_TABLE_QNAME_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.USED_QNAMES_TABLE_QNAME_COLUMN_NAME;
import static org.qi4j.index.sql.support.common.DBNames.USED_QNAMES_TABLE_TABLE_NAME_COLUMN_DATA_TYPE;
import static org.qi4j.index.sql.support.common.DBNames.USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME;

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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.index.sql.support.api.SQLTypeInfo;
import org.qi4j.index.sql.support.common.DBNames;
import org.qi4j.index.sql.support.common.EntityTypeInfo;
import org.qi4j.index.sql.support.common.QNameInfo;
import org.qi4j.index.sql.support.common.GenericDatabaseExplorer.Deferrability;
import org.qi4j.index.sql.support.common.QNameInfo.QNameType;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.index.sql.support.skeletons.AbstractSQLStartup;
import org.qi4j.index.sql.support.skeletons.SQLDBState;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.ds.DataSourceService;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.value.ValueDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
import org.sql.generation.api.grammar.definition.table.TableCommitAction;
import org.sql.generation.api.grammar.definition.table.TableScope;
import org.sql.generation.api.grammar.definition.table.pgsql.PgSQLTableCommitAction;
import org.sql.generation.api.grammar.factories.DataTypeFactory;
import org.sql.generation.api.grammar.factories.DefinitionFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.grammar.query.QueryExpression;
import org.sql.generation.api.vendor.PostgreSQLVendor;
import org.sql.generation.api.vendor.SQLVendor;

/**
 * TODO refactoring
 * 
 * @author Stanislav Muhametsin
 */
public class PostgreSQLAppStartup extends AbstractSQLStartup
{

    @This
    private SQLDBState _state;

    @This
    private ServiceComposite _myselfAsService;

    @Service
    private Reindexer _reindexer;

    @Service
    private DataSourceService _dataSource;

    private PostgreSQLVendor _vendor;

    private static final Logger _log = LoggerFactory.getLogger( PostgreSQLAppStartup.class.getName() );

    @Override
    public void activate()
        throws Exception
    {
        super.activate();
        this._vendor = this._myselfAsService.metaInfo( PostgreSQLVendor.class );
    }

    @Override
    protected void dropTablesIfExist( DatabaseMetaData metaData, String schemaName, String tableName, Statement stmt )
        throws SQLException
    {
        ResultSet rs = metaData.getTables( null, schemaName, tableName, new String[]
        {
            "TABLE"
        } );
        try
        {
            while( rs.next() )
            {
                stmt.execute( "DROP TABLE IF EXISTS " + schemaName + "." + rs.getString( 3 ) + " CASCADE" );
            }
        }
        finally
        {
            rs.close();
        }
    }

    protected <PKType> PKType getNextPK( Class<PKType> pkClass, Statement stmt, String schemaName, String columnName,
        String tableName, PKType defaultPK )
        throws SQLException
    {
        ResultSet rs = null;
        PKType result = defaultPK;
        try
        {
            rs = stmt.executeQuery( String.format( DBNames.TWO_VALUE_SELECT, "COUNT(" + columnName + ")", "MAX("
                + columnName + ") + 1", schemaName, tableName ) );
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

    @Override
    protected void testRequiredCapabilities()
        throws SQLException
    {
        // If collection structure matching will ever be needed, using ltree as path to each leaf item in
        // collection-generated tree will be very useful
        // ltree module provides specific datatype for such path, which may be indexed in order to greatly improve
        // performance

        Connection connection = this._dataSource.getDataSource().getConnection();
        Statement stmt = connection.createStatement();
        try
        {
            DefinitionFactory d = this._vendor.getDefinitionFactory();
            TableReferenceFactory t = this._vendor.getTableReferenceFactory();
            DataTypeFactory dt = this._vendor.getDataTypeFactory();

            stmt.execute( this._vendor.toString( d
                .createTableDefinitionBuilder()
                .setTableScope( TableScope.LOCAL_TEMPORARY )
                .setTableName( t.tableName( this._state.schemaName().get(), "ltree_test" ) )
                .setCommitAction( PgSQLTableCommitAction.DROP )
                .setTableContentsSource(
                    d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( "test_column", dt.userDefined( "ltree" ) ) )
                        .createExpression() ).createExpression() ) );
            // stmt.execute( "CREATE TABLE " + this._state.schemaName().get() + ".ltree_test ( test_column ltree )" );
            // stmt.execute( "DROP TABLE " + this._state.schemaName().get() + ".ltree_test" );
        }
        catch( SQLException sqle )
        {
            throw new InternalError(
                "It seems that your database doesn't have ltree as type. It is needed to store collections. Please refer to hopefully supplied instructions on how to add ltree type (hint: run <pg_install_dir>/share/contrib/ltree.sql script)." );
        }
    }

    @Override
    protected void modifyPrimitiveTypes( Map<Class<?>, SQLDataType> primitiveTypes )
    {
        // Set TEXT as default type for strings, since PgSQL can optimize that better than some VARCHAR with weird max
        // length
        primitiveTypes.put( String.class, this._vendor.getDataTypeFactory().text() );
    }

    @Override
    protected SQLDataType getCollectionPathDataType()
    {
        return this._vendor.getDataTypeFactory().userDefined( "ltree" );
    }
}
