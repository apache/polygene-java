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
package org.qi4j.logging.service;

import org.qi4j.Qi4j;
import org.qi4j.composite.Composite;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.logging.logtypes.ErrorType;
import org.qi4j.logging.logtypes.InfoType;
import org.qi4j.logging.logtypes.WarningType;
import org.qi4j.logging.SimpleLog;
import org.qi4j.logging.service.LogService;

public final class SimpleLogConcern
    implements SimpleLog
{
    @Structure private Qi4j api;
    @Service( optional = true ) private LogService logService;
    private Composite composite;
    private String category;

    public SimpleLogConcern( @This Composite composite )
    {
        this.composite = composite;
        Class<?> type = composite.type();
        category = type.getName();
    }

    public void info( String message )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( InfoType.INSTANCE, api.dereference( composite ), category, message );
    }

    public void info( String message, Object param1 )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( InfoType.INSTANCE, api.dereference( composite ), category, message, param1 );
    }

    public void info( String message, Object param1, Object param2 )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( InfoType.INSTANCE, api.dereference( composite ), category, message, param1, param2 );
    }

    public void info( String message, Object... params )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( InfoType.INSTANCE, api.dereference( composite ), category, message, params );
    }

    public void warning( String message )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( WarningType.INSTANCE, api.dereference( composite ), category, message );
    }

    public void warning( String message, Object param1 )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( WarningType.INSTANCE, api.dereference( composite ), category, message, param1 );
    }

    public void warning( String message, Object param1, Object param2 )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( WarningType.INSTANCE, api.dereference( composite ), category, message, param1, param2 );
    }

    public void warning( String message, Object... params )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( WarningType.INSTANCE, api.dereference( composite ), category, message, params );
    }

    public void error( String message )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( ErrorType.INSTANCE, api.dereference( composite ), category, message );
    }

    public void error( String message, Object param1 )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( ErrorType.INSTANCE, api.dereference( composite ), category, message, param1 );
    }

    public void error( String message, Object param1, Object param2 )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( ErrorType.INSTANCE, api.dereference( composite ), category, message, param1, param2 );
    }

    public void error( String message, Object... params )
    {
        if( logService == null )
        {
            return;
        }
        logService.log( ErrorType.INSTANCE, api.dereference( composite ), category, message, params );
    }
}
