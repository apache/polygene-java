/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.test.util;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

/**
 * A set of assertion methods useful for tests using org.json.
 */
public class JSONAssert
    extends Assert
{

    /**
     * Assert that two JSONObjects are equals without enforcing field order.
     */
    public static void jsonObjectsEquals( JSONObject o1, JSONObject o2 )
        throws JSONException
    {
        if( o1 != o2 )
        {
            if( o1.length() != o2.length() )
            {
                fail( "JSONObjects length differ: " + o1.length() + " / " + o2.length() );
            }
            @SuppressWarnings( "unchecked" )
            Iterator<String> o1Keys = o1.keys();
            while( o1Keys.hasNext() )
            {
                String key = o1Keys.next();
                Object o1Value = o1.get( key );
                Object o2Value = o2.get( key );
                if( !jsonValueEquals( o1Value, o2Value ) )
                {
                    fail( "JSONObject '" + key + "' values differ: " + o1Value + " / " + o2Value );
                }
            }
        }
    }

    /**
     * Assert that two JSONArrays are equals.
     */
    public static void jsonArraysEquals( JSONArray a1, JSONArray a2 )
        throws JSONException
    {
        if( a1 != a2 )
        {
            if( a1.length() != a2.length() )
            {
                fail( "JSONArrays length differ: " + a1.length() + " / " + a2.length() );
            }
            for( int idx = 0; idx < a1.length(); idx++ )
            {
                Object a1Value = a1.get( idx );
                Object a2Value = a2.get( idx );
                if( !jsonValueEquals( a1Value, a2Value ) )
                {
                    fail( "JSONArray '" + idx + "' values differ: " + a1Value + " / " + a2Value );
                }
            }
        }
    }

    private static boolean jsonValueEquals( Object o1Value, Object o2Value )
        throws JSONException
    {
        if( o1Value instanceof JSONObject )
        {

            if( !( o2Value instanceof JSONObject ) )
            {
                return false;
            }
            jsonObjectsEquals( (JSONObject) o1Value, (JSONObject) o2Value );

        }
        else if( o1Value instanceof JSONArray )
        {

            if( !( o2Value instanceof JSONArray ) )
            {
                return false;
            }
            jsonArraysEquals( (JSONArray) o1Value, (JSONArray) o2Value );

        }
        else if( !o1Value.equals( o2Value ) )
        {

            return false;

        }
        return true;
    }

    private JSONAssert()
    {
    }

}
