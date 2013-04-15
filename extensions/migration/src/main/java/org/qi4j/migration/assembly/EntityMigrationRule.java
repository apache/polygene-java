/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.migration.assembly;

import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.migration.Migrator;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;
import org.qi4j.spi.entitystore.helpers.StateStore;

/**
 * Migration rule for a specific set of entity types
 */
public class EntityMigrationRule
    extends AbstractMigrationRule
{
    private String[] entityTypes;
    private EntityMigrationOperation operationEntity;

    public EntityMigrationRule( String fromVersion,
                                String toVersion,
                                String[] entityTypes,
                                EntityMigrationOperation operationEntity
    )
    {
        super( fromVersion, toVersion );
        this.entityTypes = entityTypes;
        this.operationEntity = operationEntity;
    }

    public String[] entityTypes()
    {
        return entityTypes;
    }

    public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator )
        throws JSONException
    {
        if( appliesTo( state.getString( MapEntityStore.JSONKeys.type.name() ) ) )
        {
            return operationEntity.upgrade( state, stateStore, migrator );
        }
        else
        {
            return false;
        }
    }

    public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator )
        throws JSONException
    {
        if( appliesTo( state.getString( MapEntityStore.JSONKeys.type.name() ) ) )
        {
            return operationEntity.downgrade( state, stateStore, migrator );
        }
        else
        {
            return false;
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
        return fromVersion + "->" + toVersion + ": on " + Arrays.asList( entityTypes ) + " do " + operationEntity;
    }
}