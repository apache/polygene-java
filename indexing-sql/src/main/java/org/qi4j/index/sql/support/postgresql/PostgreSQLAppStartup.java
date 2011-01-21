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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.index.sql.support.skeletons.AbstractSQLStartup;
import org.qi4j.library.sql.ds.DataSourceService;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
import org.sql.generation.api.grammar.definition.table.TableScope;
import org.sql.generation.api.grammar.definition.table.pgsql.PgSQLTableCommitAction;
import org.sql.generation.api.grammar.factories.DataTypeFactory;
import org.sql.generation.api.grammar.factories.DefinitionFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.grammar.manipulation.DropBehaviour;
import org.sql.generation.api.grammar.manipulation.ObjectType;
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
    private ServiceComposite _myselfAsService;

    @Service
    private DataSourceService _dataSource;

    private PostgreSQLVendor _vendor;

    @Override
    public void activate()
        throws Exception
    {
        super.activate();
        this._vendor = this._myselfAsService.metaInfo( PostgreSQLVendor.class );
    }

    //    @Override
    //    protected void dropTablesIfExist( DatabaseMetaData metaData, String schemaName, String tableName, Statement stmt )
    //        throws SQLException
    //    {
    //        ResultSet rs = metaData.getTables( null, schemaName, tableName, new String[]
    //        {
    //            "TABLE"
    //        } );
    //        try
    //        {
    //            while( rs.next() )
    //            {
    //                stmt.execute( this._vendor.toString( this._vendor.getManipulationFactory()
    //                    .createDropTableOrViewStatement(
    //                        this._vendor.getTableReferenceFactory().tableName( schemaName, tableName ), ObjectType.TABLE,
    //                        DropBehaviour.CASCADE, true ) ) );
    //            }
    //        }
    //        finally
    //        {
    //            rs.close();
    //        }
    //    }

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
                .setTableName( t.tableName( "ltree_test" ) )
                .setCommitAction( PgSQLTableCommitAction.DROP )
                .setTableContentsSource(
                    d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( "test_column", dt.userDefined( "ltree" ) ) )
                        .createExpression() ).createExpression() ) );
        }
        catch( SQLException sqle )
        {
            throw new InternalError(
                "It seems that your database doesn't have ltree as type. It is needed to store collections. Please refer to hopefully supplied instructions on how to add ltree type (hint: run <pg_install_dir>/share/contrib/ltree.sql script)." );
        }
    }

    @Override
    protected void modifyPrimitiveTypes( Map<Class<?>, SQLDataType> primitiveTypes, Map<Class<?>, Integer> jdbcTypes )
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

    @Override
    protected void setVendor( SQLVendor vendor )
    {
        this._vendor = (PostgreSQLVendor) vendor;
    }
}
