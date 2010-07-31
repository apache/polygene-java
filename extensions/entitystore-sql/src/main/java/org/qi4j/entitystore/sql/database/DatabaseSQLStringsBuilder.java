/*
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
package org.qi4j.entitystore.sql.database;

import org.qi4j.api.injection.scope.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public interface DatabaseSQLStringsBuilder
{

    String[] buildSQLForSchemaCreation();

    String[] buildSQLForIndexCreation();

    String[] buildSQLForTableCreation();

    String buildSQLForSelectAllEntitiesStatement();

    String buildSQLForSelectEntityStatement();

    String buildSQLForInsertEntityStatement();

    String buildSQLForUpdateEntityStatement();

    String buildSQLForRemoveEntityStatement();

    abstract class CommonMixin
            implements DatabaseSQLStringsBuilder
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( DatabaseSQLStringsBuilder.class );

        @This
        private DatabaseSQLServiceSpi spi;

        public String[] buildSQLForSchemaCreation()
        {
            String[] sql = new String[]{ String.format( SQLs.CREATE_SCHEMA_SQL, this.spi.getCurrentSchemaName() ) };
            LOGGER.trace( "SQL for schema creation: {}", sql );
            return sql;
        }

        public String[] buildSQLForIndexCreation()
        {
            // TODO
            String[] sql = new String[]{};
            LOGGER.trace( "SQL for index creation: {}", sql );
            return sql;
        }

        public String buildSQLForSelectAllEntitiesStatement()
        {
            String sql = String.format( SQLs.SELECT_ALL_ENTITIES_SQL, this.spi.getCurrentSchemaName() );
            LOGGER.trace( "SQL for select all entities: {}", sql );
            return sql;
        }

        public String buildSQLForSelectEntityStatement()
        {
            String sql = String.format( SQLs.SELECT_ENTITY_SQL, this.spi.getCurrentSchemaName() );
            LOGGER.trace( "SQL for select entity: {}", sql );
            return sql;
        }

        public String buildSQLForInsertEntityStatement()
        {
            String sql = String.format( SQLs.INSERT_ENTITY_SQL, this.spi.getCurrentSchemaName() );
            LOGGER.trace( "SQL for insert entity: {}", sql );
            return sql;
        }

        public String buildSQLForUpdateEntityStatement()
        {
            String sql = String.format( SQLs.UPDATE_ENTITY_SQL, this.spi.getCurrentSchemaName() );
            LOGGER.trace( "SQL for update entity: {}", sql );
            return sql;
        }

        public String buildSQLForRemoveEntityStatement()
        {
            String sql = String.format( SQLs.REMOVE_ENTITY_SQL, this.spi.getCurrentSchemaName() );
            LOGGER.trace( "SQL for remove entity: {}", sql );
            return sql;
        }

    }

}
