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
package org.apache.polygene.migration.assembly;

import java.util.Arrays;
import javax.json.JsonObject;
import org.apache.polygene.migration.Migrator;
import org.apache.polygene.spi.entitystore.helpers.JSONKeys;
import org.apache.polygene.spi.entitystore.helpers.StateStore;

/**
 * Migration rule for a specific set of entity types
 */
public class EntityMigrationRule
    extends AbstractMigrationRule
{
    private final String[] entityTypes;
    private final EntityMigrationOperation entityOperation;

    public EntityMigrationRule( String fromVersion,
                                String toVersion,
                                String[] entityTypes,
                                EntityMigrationOperation entityOperation
    )
    {
        super( fromVersion, toVersion );
        this.entityTypes = entityTypes;
        this.entityOperation = entityOperation;
    }

    public String[] entityTypes()
    {
        return entityTypes;
    }

    public JsonObject upgrade( MigrationContext context, JsonObject state, StateStore stateStore, Migrator migrator )
    {
        if( appliesTo( state.getString( JSONKeys.TYPE ) ) )
        {
            return entityOperation.upgrade( context, state, stateStore, migrator );
        }
        else
        {
            context.addFailure( entityOperation.toString() );
            return state;
        }
    }

    public JsonObject downgrade( MigrationContext context, JsonObject state, StateStore stateStore, Migrator migrator )
    {
        if( appliesTo( state.getString( JSONKeys.TYPE ) ) )
        {
            return entityOperation.downgrade( context, state, stateStore, migrator );
        }
        else
        {
            context.addFailure( entityOperation.toString() );
            return state;
        }
    }

    public boolean appliesTo( String entityType )
    {
        for( String type : entityTypes )
        {
            if( entityType.equals( type ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return fromVersion + "=>" + toVersion + ": on " + Arrays.asList( entityTypes ) + " do " + entityOperation;
    }
}
