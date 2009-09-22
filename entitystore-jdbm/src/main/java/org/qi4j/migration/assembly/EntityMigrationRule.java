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

import org.qi4j.spi.util.json.JSONObject;
import org.qi4j.spi.util.json.JSONException;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.entitystore.map.MapEntityStore;

/**
 * JAVADOC
 */
public class EntityMigrationRule
    extends MigrationRule
{
    private String[] entityTypes;

    public EntityMigrationRule( String fromVersion, String toVersion, String[] entityTypes , MigrationOperation operation)
    {
        super( fromVersion, toVersion, operation );
        this.entityTypes = entityTypes;
    }

    public String[] getEntityTypes()
    {
        return entityTypes;
    }

    @Override public boolean upgrade( JSONObject state, StateStore stateStore ) throws JSONException
    {
        if (appliesTo( state.getString( MapEntityStore.JSONKeys.type.name() )))
            return super.upgrade( state, stateStore );
        else
            return false;
    }

    @Override public boolean downgrade( JSONObject state, StateStore stateStore ) throws JSONException
    {
        if (appliesTo( state.getString( MapEntityStore.JSONKeys.type.name() )))
            return super.downgrade( state, stateStore );
        else
            return false;
    }

    public boolean appliesTo( String entityType )
    {
        for( String type : entityTypes )
        {
            if (entityType.equals( type ))
                return true;
        }
        return false;
    }
}