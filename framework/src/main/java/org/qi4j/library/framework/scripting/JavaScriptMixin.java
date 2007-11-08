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
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.annotation.AppliesTo;
import org.qi4j.annotation.AppliesToFilter;
import org.qi4j.annotation.scope.Structure;

/**
 * Generic mixin that implements interfaces by delegating to JavaScript functions
 * using Rhino. Each method in an interface is declared as a JS function
 * in a file located in classpath with the name "<interface>.<method>.js",
 * where the interface name includes the package, and has "." replaced with "/".
 * <p/>
 * Example:
 * org/qi4j/samples/hello/domain/HelloWorldSpeaker.say.js
 */
@AppliesTo( JavaScriptMixin.AppliesTo.class )
public class JavaScriptMixin
    implements InvocationHandler
{
    public static class AppliesTo
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class compositeType, Class mixin, Class modelClass )
        {
            return getFunctionResoure( method ) != null;
        }
    }

    static Scriptable standardScope;

    static
    {
        Context cx = Context.enter();
        standardScope = cx.initStandardObjects();
        Context.exit();
    }

    @Structure CompositeBuilderFactory factory;
    Scriptable instanceScope;

    public JavaScriptMixin()
    {
        Context cx = Context.enter();
        instanceScope = cx.newObject( standardScope );
        instanceScope.setPrototype( standardScope );
        Context.exit();
    }

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

    protected Function getFunction( Context cx, Scriptable scope, Method aMethod )
        throws IOException
    {
        URL scriptUrl = getFunctionResoure( aMethod );

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

        return cx.compileFunction( scope, script, "<" + scriptUrl.getFile() + ">", 0, null );
    }

    protected static URL getFunctionResoure( Method method )
    {
        String scriptFile = method.getDeclaringClass().getName().replace( '.', File.separatorChar ) + "." + method.getName() + ".js";
        URL scriptUrl = method.getDeclaringClass().getClassLoader().getResource( scriptFile );

        return scriptUrl;
    }
}
