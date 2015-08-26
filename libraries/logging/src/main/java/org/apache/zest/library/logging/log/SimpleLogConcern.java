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
package org.apache.zest.library.logging.log;

import java.io.Serializable;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.library.logging.log.service.LoggingService;

public final class SimpleLogConcern
    implements SimpleLog
{
    @Structure
    private ZestAPI api;
    @Optional
    @Service
    private LoggingService loggingService;
    private Composite composite;
    private String category;

    public SimpleLogConcern( @This Composite composite )
    {
        this.composite = composite;
        Class<?> type = ZestAPI.FUNCTION_DESCRIPTOR_FOR.apply( composite ).types().findFirst().orElse( null );
        category = type.getName();
    }

    @Override
    public void info( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message );
    }

    @Override
    public void info( String message, Serializable param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1 );
    }

    @Override
    public void info( String message, Serializable param1, Serializable param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1, param2 );
    }

    @Override
    public void info( String message, Serializable... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, params );
    }

    @Override
    public void warning( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message );
    }

    @Override
    public void warning( String message, Serializable param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1 );
    }

    @Override
    public void warning( String message, Serializable param1, Serializable param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1, param2 );
    }

    @Override
    public void warning( String message, Serializable... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, params );
    }

    @Override
    public void error( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message );
    }

    @Override
    public void error( String message, Serializable param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1 );
    }

    @Override
    public void error( String message, Serializable param1, Serializable param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, param1, param2 );
    }

    @Override
    public void error( String message, Serializable... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( LogType.INFO, api.dereference( composite ), category, message, params );
    }
}
