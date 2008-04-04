/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.logging.logtypes;

import org.qi4j.logging.LogType;
import org.qi4j.property.GenericPropertyInfo;
import org.qi4j.property.Property;
import org.qi4j.spi.property.ImmutablePropertyInstance;

public final class ErrorType
    implements LogType
{
    public static final LogType INSTANCE = new ErrorType();

    private static Property PROPERTY;

    static
    {
        try
        {
            GenericPropertyInfo info = new GenericPropertyInfo( LogType.class.getMethod( "logTypeName" ) );
            PROPERTY = new ImmutablePropertyInstance( info, "WARN" );
        }
        catch( NoSuchMethodException e )
        {
            e.printStackTrace();
        }
    }

    private ErrorType()
    {
    }

    public Property<String> logTypeName()
    {
        return PROPERTY;
    }
}
