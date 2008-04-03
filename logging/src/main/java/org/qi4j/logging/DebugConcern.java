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
package org.qi4j.logging;

import org.qi4j.Qi4j;
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.property.Property;

public final class DebugConcern
    implements Debug
{
    @Structure Qi4j api;
    @Service private LogService logService;
    @ThisCompositeAs private Composite composite;


    public Property<Integer> debugLevel()
    {
        return logService.debugLevel();
    }

    public void debug( int priority, String message )
    {
        if( priority >= logService.debugLevel().get() )
        {
            logService.debug( api.dereference( composite ), message );
        }
    }

    public void debug( int priority, String message, Object param1 )
    {
        if( priority >= logService.debugLevel().get() )
        {
            logService.debug( api.dereference( composite ), message, param1 );
        }
    }

    public void debug( int priority, String message, Object param1, Object param2 )
    {
        if( priority >= logService.debugLevel().get() )
        {
            logService.debug( api.dereference( composite ), message, param1, param2 );
        }
    }

    public void debug( int priority, String message, Object... params )
    {
        if( priority >= logService.debugLevel().get() )
        {
            logService.debug( api.dereference( composite ), message, params );
        }
    }
}
