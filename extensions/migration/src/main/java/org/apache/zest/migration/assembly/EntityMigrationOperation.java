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

package org.apache.zest.migration.assembly;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.zest.migration.Migrator;
import org.apache.zest.spi.entitystore.helpers.StateStore;

/**
 * Migration operation for a specific entity.
 */
public interface EntityMigrationOperation
{
    boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator )
        throws JSONException;

    boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator )
        throws JSONException;
}