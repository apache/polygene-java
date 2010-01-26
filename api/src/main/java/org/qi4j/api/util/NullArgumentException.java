/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.util;

/**
 * Thrown if an argument to a method was null, and the method required
 * it to be non-null.
 */
public class NullArgumentException
    extends IllegalArgumentException
{
    private static final long serialVersionUID = 4815431779868729780L;

    private NullArgumentException( String message )
    {
        super( message );
    }

    public static void validateNotNull( String parameterName, Object value )
    {
        if( value != null )
        {
            return;
        }
        String message = parameterName + " was null.";
        throw new NullArgumentException( message );
    }

    public static void validateNotEmpty( String parameterName, String value )
    {
        if( value == null )
        {
            String message = parameterName + " was null.";
            throw new NullArgumentException( message );
        }
        if( value.length() == 0 )
        {
            String message = parameterName + " was empty.";
            throw new NullArgumentException( message );
        }
    }
}
