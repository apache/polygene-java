/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.entitystore.sql.internal;

import org.qi4j.api.injection.scope.This;
import org.qi4j.library.sql.common.SQLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.grammar.factories.QueryFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.vendor.SQLVendor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface DatabaseSQLServiceSpi
{

    boolean schemaExists( Connection connection )
        throws SQLException;

    String getCurrentSchemaName();

    boolean tableExists( Connection connection )
        throws SQLException;

    long readNextEntityPK( Connection connection )
        throws SQLException;

    @SuppressWarnings("PublicInnerClass")
    public abstract class CommonMixin
        implements DatabaseSQLServiceSpi
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( DatabaseSQLServiceSpi.class );

        @This
        private DatabaseSQLServiceState state;

        public boolean schemaExists( Connection connection )
            throws SQLException
        {
            ResultSet rs = null;
            try
            {
                Boolean schemaFound = false;
                rs = connection.getMetaData().getSchemas();
                String schemaName = this.getCurrentSchemaName();

                while( rs.next() && !schemaFound )
                {
                    String eachResult = rs.getString( 1 );
                    LOGGER.trace( "Schema candidate: {}", eachResult );
                    schemaFound = eachResult.equalsIgnoreCase( schemaName );
                }
                LOGGER.trace( "Schema {} found? {}", schemaName, schemaFound );
                return schemaFound;
            }
            finally
            {
                SQLUtil.closeQuietly( rs );
            }
        }

        public String getCurrentSchemaName()
        {
            return this.state.schemaName().get();
        }

        public long readNextEntityPK( Connection connection )
            throws SQLException
        {
            Statement stmt = null;
            ResultSet rs = null;
            long result = 0L;
            try
            {
                stmt = connection.createStatement();
                SQLVendor vendor = this.state.vendor().get();
                QueryFactory q = vendor.getQueryFactory();
                TableReferenceFactory t = vendor.getTableReferenceFactory();

                // Cheat a little on SQL functions
                // @formatter:off
                rs = stmt.executeQuery( 
                    vendor.toString(
                        q.simpleQueryBuilder()
                            .select( "COUNT(" + SQLs.ENTITY_PK_COLUMN_NAME + ")", "MAX(" + SQLs.ENTITY_PK_COLUMN_NAME + ")" )
                            .from( t.tableName( this.state.schemaName().get(), SQLs.TABLE_NAME ) )
                            .createExpression()
                        )
                    );
                // @formatter:on

                if( rs.next() )
                {
                    Long count = rs.getLong( 1 );
                    if( count > 0 )
                    {
                        result = rs.getLong( 2 ) + 1;
                    }
                }
            }
            finally
            {
                SQLUtil.closeQuietly( rs );
                SQLUtil.closeQuietly( stmt );
            }

            return result;
        }

    }

}
