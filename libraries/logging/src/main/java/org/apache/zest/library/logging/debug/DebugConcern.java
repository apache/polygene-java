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
package org.apache.zest.library.logging.debug;

import java.io.Serializable;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.library.logging.debug.service.DebuggingService;

public class DebugConcern
    implements Debug
{
    @Structure private ZestAPI api;
    @Optional @Service private DebuggingService loggingService;
    @This private Composite composite;

    @Override
    public Integer debugLevel()
    {
        if( loggingService != null )
        {
            return loggingService.debugLevel();
        }
        return OFF;
    }

    @Override
    public void debug( int priority, String message )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel() )
        {
            Composite derefed = api.dereference( composite );
            loggingService.debug( derefed, message );
        }
    }

    @Override
    public void debug( int priority, String message, Serializable param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel() )
        {
            loggingService.debug( api.dereference( composite ), message, param1 );
        }
    }

    @Override
    public void debug( int priority, String message, Serializable param1, Serializable param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel() )
        {
            loggingService.debug( api.dereference( composite ), message, param1, param2 );
        }
    }

    @Override
    public void debug( int priority, String message, Serializable... params )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel() )
        {
            loggingService.debug( api.dereference( composite ), message, params );
        }
    }
}
