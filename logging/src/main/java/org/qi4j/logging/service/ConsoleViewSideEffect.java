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

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.Invocation;
import org.qi4j.composite.scope.SideEffectFor;
import org.qi4j.logging.LogService;
import org.qi4j.logging.LogType;

/**
 * The ConsoleViewSideEffect is just a temporary solution for logging output, until a more
 * robust framework has been designed.
 */
public abstract class ConsoleViewSideEffect
    implements LogService
{
    private static PrintStream OUT = System.err;

    @SideEffectFor private LogService service;

    private final ResourceBundle bundle;

    public ConsoleViewSideEffect( @Invocation Method thisMethod )
    {
        bundle = ResourceBundle.getBundle( thisMethod.getDeclaringClass().getName() );
    }

    public void traceEntry( Class compositeType, Composite object, Method method, Object[] args )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "ENTRY: " );
        formatMethod( buf, compositeType, method, args );
        OUT.println( buf.toString() );
    }

    public void traceExit( Class compositeType, Composite object, Method method, Object[] args, Object result )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "EXIT: " );
        formatMethod( buf, compositeType, method, args );
        OUT.println( buf.toString() );
        OUT.println( result );
    }

    public void traceException( Class compositeType, Composite object, Method method, Object[] args, Throwable t )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "EXCEPTION: " );
        formatMethod( buf, compositeType, method, args );
        OUT.println( buf.toString() );
        t.printStackTrace( OUT );
    }

    public void debug( Composite composite, String message )
    {
        String localized = bundle.getString( message );
        OUT.println( "DEBUG:" + composite.type().getName() + ": " + localized );
    }

    public void debug( Composite composite, String message, Object param1 )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, param1 );
        OUT.println( "DEBUG:" + composite.type().getName() + ": " + formatted );
    }

    public void debug( Composite composite, String message, Object param1, Object param2 )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, param1, param2 );
        OUT.println( "DEBUG:" + composite.type().getName() + ": " + formatted );
    }

    public void debug( Composite composite, String message, Object... params )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, params );
        OUT.println( "DEBUG:" + composite.type().getName() + ": " + formatted );
    }

    public void log( LogType type, Composite composite, String category, String message )
    {
        String localized = bundle.getString( message );
        String logType = type.logTypeName().get();
        OUT.println( logType + ":" + category + ":" + composite.type().getName() + ": " + localized );
    }

    public void log( LogType type, Composite composite, String category, String message, Object param1 )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, param1 );
        String logType = type.logTypeName().get();
        OUT.println( logType + ":" + category + ":" + composite.type().getName() + ": " + formatted );
    }

    public void log( LogType type, Composite composite, String category, String message, Object param1, Object param2 )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, param1, param2 );
        String logtype = type.logTypeName().get();
        OUT.println( logtype + ":" + category + ":" + composite.type().getName() + ": " + formatted );
    }

    public void log( LogType type, Composite composite, String category, String message, Object... params )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, params );
        String logType = type.logTypeName().get();
        OUT.println( logType + ":" + category + ":" + composite.type().getName() + ": " + formatted );
    }

    private void formatMethod( StringBuffer buf, Class compositeType, Method method, Object[] args )
    {
        buf.append( compositeType.getClass().getName() );
        buf.append( "." );
        buf.append( method.getName() );
        buf.append( "( " );
        if( args != null )
        {
            boolean first = true;
            for( Object arg : args )
            {
                if( !first )
                {
                    buf.append( ", " );
                }
                first = false;
                if( arg instanceof String )
                {
                    buf.append( "\"" );
                }
                buf.append( arg );
                if( arg instanceof String )
                {
                    buf.append( "\"" );
                }
            }
        }
        buf.append( " )" );
    }
}
