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
package org.apache.zest.library.logging.log.service;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.injection.scope.Invocation;
import org.apache.zest.api.sideeffect.SideEffectOf;
import org.apache.zest.library.logging.log.LogType;

/**
 * The ConsoleViewSideEffect is just a temporary solution for logging output, until a more
 * robust framework has been designed.
 */
public abstract class LogOnConsoleSideEffect extends SideEffectOf<LoggingService>
    implements LoggingService
{
    private static PrintStream OUT = System.err;

    private final ResourceBundle bundle;

    public LogOnConsoleSideEffect( @Invocation Method thisMethod )
    {
        bundle = ResourceBundle.getBundle( thisMethod.getDeclaringClass().getName() );
    }

    public void log( LogType type, Composite composite, String category, String message )
    {
        String localized = bundle.getString( message );
        String logType = type.name();
        OUT.println( logType + ":" + category + ":" + getCompositeName( composite ) + ": " + localized );
    }

    private String getCompositeName( Composite composite )
    {
        return ZestAPI.FUNCTION_DESCRIPTOR_FOR.apply( composite ).types().findFirst().get().getName();
    }

    public void log( LogType type, Composite composite, String category, String message, Object param1 )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, param1 );
        String logType = type.name();
        OUT.println( logType + ":" + category + ":" + getCompositeName( composite ) + ": " + formatted );
    }

    public void log( LogType type, Composite composite, String category, String message, Object param1, Object param2 )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, param1, param2 );
        String logtype = type.name();
        OUT.println( logtype + ":" + category + ":" + getCompositeName( composite ) + ": " + formatted );
    }

    public void log( LogType type, Composite composite, String category, String message, Object... params )
    {
        String localized = bundle.getString( message );
        String formatted = MessageFormat.format( localized, params );
        String logType = type.name();
        OUT.println( logType + ":" + category + ":" + getCompositeName( composite ) + ": " + formatted );
    }
}
