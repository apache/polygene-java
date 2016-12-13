/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.entitystore.sql.internal;

public interface SQLs
{

    String DEFAULT_SCHEMA_NAME = "polygene_es";

    String TABLE_NAME = "polygene_entities";

    String ENTITY_PK_COLUMN_NAME = "entity_pk";

    String ENTITY_IDENTITY_COLUMN_NAME = "entity_id";

    String ENTITY_STATE_COLUMN_NAME = "entity_state";

    String ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME = "entity_optimistic_lock";

    String ENTITY_LAST_MODIFIED_COLUMN_NAME = "entity_last_modified";

}
