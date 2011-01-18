/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.eventsourcing.domain.source.helper;

import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;

/**
 * Utility class to pick out parameters by name or index as strings from a DomainEventValue
 */
public class EventParameters
{
    /**
     * Get the named parameter from an eventValue.
     *
     * @param eventValue eventValue with parameters
     * @param name  name of parameter
     * @return the parameter with the given name or null
     */
    public static String getParameter( DomainEventValue eventValue, String name )
    {
        String parametersJson = eventValue.parameters().get();
        try
        {
            JSONObject jsonObject = new JSONObject( parametersJson );
            return jsonObject.get( name ).toString();
        } catch (JSONException e)
        {
            return null;
        }
    }

    /**
     * Get parameter with given index.
     *
     * @param eventValue eventValue with parameters
     * @param idx   index of parameter
     * @return the parameter with the given index or null
     * @throws JSONException
     */
    public static String getParameter( DomainEventValue eventValue, int idx )
    {
        try
        {
            String parametersJson = eventValue.parameters().get();
            JSONObject jsonObject = new JSONObject( parametersJson );
            return jsonObject.get( "param" + idx ).toString();
        } catch (JSONException e)
        {
            return null;
        }
    }
}
