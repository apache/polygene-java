/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.framework.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.annotation.scope.Qi4j;

/**
 * Generic mixin that implements interfaces by delegating to JavaScript functions
 * using Rhino. Each method in an interface is declared as a JS function
 * in a file located in classpath with the name "<interface>.<method>.js",
 * where the interface name includes the package, and has "." replaced with "/".
 * <p/>
 * Example:
 * org/qi4j/samples/hello/domain/HelloWorldSpeaker.say.js
 */
public class JavaScriptMixin
    implements InvocationHandler
{
    @Qi4j CompositeBuilderFactory factory;

    // Static --------------------------------------------------------
    static Scriptable standardScope;

    static
    {
        Context cx = Context.enter();
        standardScope = cx.initStandardObjects();
        Context.exit();
    }

    // Attributes ----------------------------------------------------
    Scriptable instanceScope;

    // Constructors --------------------------------------------------
    public JavaScriptMixin()
    {
        Context cx = Context.enter();
        instanceScope = cx.newObject( standardScope );
        instanceScope.setPrototype( standardScope );
        Context.exit();
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        Context cx = Context.enter();
        try
        {
            Scriptable proxyScope = Context.toObject( proxy, instanceScope );
            proxyScope.setPrototype( instanceScope );
            proxyScope.put( "factory", proxyScope, factory );
            Function fn = getFunction( cx, proxyScope, method );
            Object result = fn.call( cx, instanceScope, proxyScope, args );

            if( result instanceof Undefined )
            {
                return null;
            }
            else if( result instanceof Wrapper )
            {
                return ( (Wrapper) result ).unwrap();
            }
            else
            {
                return result;
            }
        }
        finally
        {
            Context.exit();
        }
    }

    // Protected -----------------------------------------------------
    protected Function getFunction( Context cx, Scriptable scope, Method aMethod )
        throws IOException
    {
        String scriptFile = aMethod.getDeclaringClass().getSimpleName() + "." + aMethod.getName() + ".js";

        URL scriptUrl = getClass().getResource( scriptFile );
        if( scriptUrl == null )
        {
            scriptFile = aMethod.getDeclaringClass().getName().replace( '.', File.separatorChar ) + "." + aMethod.getName() + ".js";
            scriptUrl = aMethod.getDeclaringClass().getClassLoader().getResource( scriptFile );
        }

        if( scriptUrl == null )
        {
            throw new IOException( "No script found for method " + aMethod.getName() );
        }

        InputStream in = scriptUrl.openStream();
        BufferedReader scriptReader = new BufferedReader( new InputStreamReader( in ) );
        String line;
        String script = "";
        while( ( line = scriptReader.readLine() ) != null )
        {
            script += line + "\n";
        }

        return cx.compileFunction( scope, script, "<" + scriptFile + ">", 0, null );
    }
}
