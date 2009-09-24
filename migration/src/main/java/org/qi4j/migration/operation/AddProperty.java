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

package org.qi4j.migration.operation;

import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.migration.assembly.MigrationOperation;

/**
 * Add a property
 */
public class AddProperty
    implements MigrationOperation
{
    private String property;
    private Object defaultValue;

    public AddProperty( String property, Object defaultValue )
    {
        this.property = property;
        this.defaultValue = defaultValue;
    }

    public boolean upgrade( JSONObject state, StateStore stateStore )
        throws JSONException
    {
        JSONObject properties = (JSONObject) state.get( MapEntityStore.JSONKeys.properties.name() );
        properties.put( property, defaultValue );
        return true;
    }

    public boolean downgrade( JSONObject state, StateStore stateStore )
        throws JSONException
    {
        JSONObject properties = (JSONObject) state.get( MapEntityStore.JSONKeys.properties.name() );
        properties.remove( property );
        return true;
    }

    @Override public String toString()
    {
        return "Add property " + property + ", default:" + defaultValue;
    }
}