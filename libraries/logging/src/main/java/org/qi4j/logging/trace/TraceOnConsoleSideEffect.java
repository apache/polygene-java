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
package org.qi4j.logging.trace;

import java.io.PrintStream;
import java.lang.reflect.Method;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.logging.trace.service.TraceService;

/**
 * The ConsoleViewSideEffect is just a temporary solution for logging output, until a more
 * robust framework has been designed.
 */
public abstract class TraceOnConsoleSideEffect extends SideEffectOf<TraceService>
    implements TraceService
{
    private static PrintStream OUT = System.err;

    @Override
    public void traceSuccess( Class compositeType, Composite object, Method method, Object[] args, Object result, long entryTime, long durationNano )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( durationNano / 1000000 );
        buf.append( " ms: " );
        formatMethod( buf, object, compositeType, method, args );
        OUT.println( buf.toString() );
        OUT.println( result );
    }

    @Override
    public void traceException( Class compositeType, Composite object, Method method, Object[] args, Throwable t, long entryTime, long durationNano )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "Exception: " );
        buf.append( durationNano / 1000000 );
        buf.append( " ms: " );
        OUT.println( buf.toString() );
        t.printStackTrace( OUT );
    }

    private void formatMethod( StringBuffer buf, Composite object, Class compositeType, Method method, Object[] args )
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
        buf.append( " )  [ " );
        if( object == null )
        {
            buf.append( "<null>" );
        }
        else
        {
            buf.append( object.toString() );
        }
        buf.append( " ]" );
    }
}