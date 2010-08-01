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

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public interface SQLs
{

    String DEFAULT_SCHEMA_NAME = "qi4j_es";

    String CREATE_SCHEMA_SQL = "CREATE SCHEMA %s";

    String TABLE_NAME = "qi4j_entities";

    String ENTITY_PK_COLUMN_NAME = "entity_pk";

    String ENTITY_IDENTITY_COLUMN_NAME = "entity_id";

    String ENTITY_STATE_COLUMN_NAME = "entity_state";

    String ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME = "entity_optimistic_lock";

    String SELECT_ALL_ENTITIES_SQL = "SELECT " + ENTITY_PK_COLUMN_NAME + ", " + ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME + ", " + ENTITY_STATE_COLUMN_NAME + " FROM %s." + TABLE_NAME;

    String SELECT_ENTITY_SQL = SELECT_ALL_ENTITIES_SQL + " WHERE " + ENTITY_IDENTITY_COLUMN_NAME + " = ?";

    String INSERT_ENTITY_SQL = "INSERT INTO %s." + TABLE_NAME + " VALUES(?, 0, ?, ?)";

    String UPDATE_ENTITY_SQL = "UPDATE %s." + TABLE_NAME + " SET " + ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME + " = ?, " + ENTITY_STATE_COLUMN_NAME + " = ? WHERE " + ENTITY_PK_COLUMN_NAME + " = ? AND " + ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME + " = ?";

    String REMOVE_ENTITY_SQL = "DELETE FROM %s." + TABLE_NAME + " WHERE " + ENTITY_PK_COLUMN_NAME + " = ?";

    String READ_NEXT_ENTITY_PK_SQL = "SELECT COUNT(" + ENTITY_PK_COLUMN_NAME + "), MAX(" + ENTITY_PK_COLUMN_NAME + ") FROM %s." + TABLE_NAME;

    String DROP_TABLE_SQL = "DROP TABLE IF EXISTS %s." + TABLE_NAME + " CASCADE";

}
