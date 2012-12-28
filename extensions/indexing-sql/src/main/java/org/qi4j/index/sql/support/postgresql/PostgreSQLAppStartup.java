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
package org.qi4j.index.sql.support.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.index.sql.support.skeletons.AbstractSQLStartup;
import org.qi4j.library.sql.common.SQLUtil;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
import org.sql.generation.api.grammar.definition.table.TableScope;
import org.sql.generation.api.grammar.definition.table.pgsql.PgSQLTableCommitAction;
import org.sql.generation.api.grammar.factories.DataTypeFactory;
import org.sql.generation.api.grammar.factories.DefinitionFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.vendor.PostgreSQLVendor;

public class PostgreSQLAppStartup extends AbstractSQLStartup
{

    private PostgreSQLVendor _vendor;

    public PostgreSQLAppStartup( @Uses ServiceDescriptor descriptor )
    {
        super( descriptor );

        this._vendor = descriptor.metaInfo( PostgreSQLVendor.class );
    }

    @Override
    protected void modifyPrimitiveTypes( Map<Class<?>, SQLDataType> primitiveTypes,
            Map<Class<?>, Integer> jdbcTypes )
    {
        // Set TEXT as default type for strings, since PgSQL can optimize that better than some
        // VARCHAR with weird max
        // length
        primitiveTypes.put( String.class, this._vendor.getDataTypeFactory().text() );
    }

    @Override
    protected void testRequiredCapabilities( Connection connection )
        throws SQLException
    {
        // If collection structure matching will ever be needed, using ltree as path to each leaf
        // item in
        // collection-generated tree will be very useful
        // ltree module provides specific datatype for such path, which may be indexed in order to
        // greatly improve
        // performance

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
                        .addTableElement(
                            d.createColumnDefinition( "test_column", dt.userDefined( "ltree" ) ) )
                        .createExpression() ).createExpression() ) );
        }
        catch ( SQLException sqle )
        {
            throw new InternalError(
                "It seems that your database doesn't have ltree as type. It is needed to store collections. Please refer to hopefully supplied instructions on how to add ltree type (hint: run <pg_install_dir>/share/contrib/ltree.sql script or command 'CREATE EXTENSION ltree;')." );
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
        }
    }

    @Override
    protected SQLDataType getCollectionPathDataType()
    {
        return this._vendor.getDataTypeFactory().userDefined( "ltree" );
    }

}
