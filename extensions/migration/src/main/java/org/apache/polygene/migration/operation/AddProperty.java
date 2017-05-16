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

package org.apache.polygene.migration.operation;

import javax.json.JsonObject;
import org.apache.polygene.migration.assembly.MigrationContext;
import org.apache.polygene.migration.Migrator;
import org.apache.polygene.migration.assembly.EntityMigrationOperation;
import org.apache.polygene.spi.entitystore.helpers.StateStore;

/**
 * Add a property
 */
public class AddProperty
    implements EntityMigrationOperation
{
    private String property;
    private Object defaultValue;

    public AddProperty( String property, Object defaultValue )
    {
        this.property = property;
        this.defaultValue = defaultValue;
    }

    @Override
    public JsonObject upgrade( MigrationContext context, JsonObject state, StateStore stateStore, Migrator migrator )
    {
        return migrator.addProperty( context, state, property, defaultValue );
    }

    @Override
    public JsonObject downgrade( MigrationContext context, JsonObject state, StateStore stateStore, Migrator migrator )
    {
        return migrator.removeProperty( context, state, property );
    }

    @Override
    public String toString()
    {
        return "Add property " + property + ", default:" + defaultValue;
    }
}
