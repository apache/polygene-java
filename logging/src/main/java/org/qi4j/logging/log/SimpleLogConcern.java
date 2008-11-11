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
package org.qi4j.logging.log;

import org.qi4j.Qi4j;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.logging.log.SimpleLog;
import org.qi4j.logging.log.LogTypes;
import org.qi4j.logging.log.service.LoggingService;
import java.io.Serializable;

public final class SimpleLogConcern
    implements SimpleLog
{
    @Structure private Qi4j api;
    @Service( optional = true ) private LoggingService loggingService;
    private Composite composite;
    private String category;
    private LogTypes types;

    public SimpleLogConcern( @This Composite composite, @Structure CompositeBuilderFactory cbf )
    {
        this.composite = composite;
        Class<?> type = composite.type();
        category = type.getName();
        types = cbf.newComposite( LogTypes.class );
    }

    public void info( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.info(), api.dereference( composite ), category, message );
    }

    public void info( String message, Serializable param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.info(), api.dereference( composite ), category, message, param1 );
    }

    public void info( String message, Serializable param1, Serializable param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.info(), api.dereference( composite ), category, message, param1, param2 );
    }

    public void info( String message, Serializable... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.info(), api.dereference( composite ), category, message, params );
    }

    public void warning( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.warning(), api.dereference( composite ), category, message );
    }

    public void warning( String message, Serializable param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.warning(), api.dereference( composite ), category, message, param1 );
    }

    public void warning( String message, Serializable param1, Serializable param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.warning(), api.dereference( composite ), category, message, param1, param2 );
    }

    public void warning( String message, Serializable... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.warning(), api.dereference( composite ), category, message, params );
    }

    public void error( String message )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.error(), api.dereference( composite ), category, message );
    }

    public void error( String message, Serializable param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.error(), api.dereference( composite ), category, message, param1 );
    }

    public void error( String message, Serializable param1, Serializable param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.error(), api.dereference( composite ), category, message, param1, param2 );
    }

    public void error( String message, Serializable... params )
    {
        if( loggingService == null )
        {
            return;
        }
        loggingService.log( types.error(), api.dereference( composite ), category, message, params );
    }
}
