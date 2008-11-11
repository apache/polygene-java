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
package org.qi4j.logging.debug.service;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.qi4j.composite.Composite;
import org.qi4j.composite.SideEffectOf;
import org.qi4j.injection.scope.Invocation;
import org.qi4j.logging.log.service.LoggingService;

/**
 * The ConsoleViewSideEffect is just a temporary solution for logging output, until a more
 * robust framework has been designed.
 */
public abstract class DebugOnConsoleSideEffect extends SideEffectOf<LoggingService>
    implements DebuggingService
{
    private static PrintStream OUT = System.err;

    private final ResourceBundle bundle;

    public DebugOnConsoleSideEffect( @Invocation Method thisMethod )
    {
        bundle = ResourceBundle.getBundle( thisMethod.getDeclaringClass().getName() );
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
}