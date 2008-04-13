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
import org.jruby.Ruby;
import org.jruby.RubyObjectAdapter;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.This;

/**
 * Generic mixin that implements interfaces by delegating to Ruby functions
 * using JRuby. Each method in an interface is declared by a Ruby method
 * in a file located in classpath with the name "<interface>.js",
 * where the interface name includes the package, and has "." replaced with "/".
 * <p/>
 * Example:
 * org/qi4j/samples/hello/domain/HelloWorldSpeaker.rb
 */
@AppliesTo( JRubyMixin.AppliesTo.class )
public class JRubyMixin
    implements InvocationHandler
{
    @This Composite me;

    private Ruby runtime;

    private IRubyObject rubyObject;
    private RubyObjectAdapter rubyObjectAdapter;

    public static class AppliesTo
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class compositeType, Class mixin, Class modelClass )
        {
            return getFunctionResoure( method ) != null;
        }
    }

    @Structure CompositeBuilderFactory factory;

    public JRubyMixin()
    {
        runtime = Ruby.getCurrentInstance();
        if( runtime == null )
        {
            runtime = Ruby.newInstance();
        }
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        if( rubyObject == null )
        {
            // Evaluate Ruby script
            runtime.evalScriptlet( getFunction( method ) );

            // Create object instance
            rubyObject = runtime.evalScriptlet( method.getDeclaringClass().getSimpleName() + ".new()" );

            // Set @this variable to Composite
            IRubyObject meRuby = JavaEmbedUtils.javaToRuby( runtime, me );
            rubyObjectAdapter = JavaEmbedUtils.newObjectAdapter();
            rubyObjectAdapter.setInstanceVariable( rubyObject, "@this", meRuby );
        }

        // Convert method arguments and invoke the method
        IRubyObject rubyResult;
        if( args != null )
        {
            IRubyObject[] rubyArgs = new IRubyObject[args.length];
            for( int i = 0; i < args.length; i++ )
            {
                Object arg = args[ i ];
                rubyArgs[ i ] = JavaEmbedUtils.javaToRuby( runtime, arg );
            }
            rubyResult = rubyObjectAdapter.callMethod( rubyObject, method.getName() );
        }
        else
        {
            rubyResult = rubyObjectAdapter.callMethod( rubyObject, method.getName() );
        }

        // Convert result to Java
        Object result = org.jruby.javasupport.JavaEmbedUtils.rubyToJava( runtime, rubyResult, method.getReturnType() );
        return result;
    }

    protected String getFunction( Method aMethod )
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

        return script;
    }

    protected static URL getFunctionResoure( Method method )
    {
        String scriptFile = method.getDeclaringClass().getName().replace( '.', File.separatorChar ) + ".rb";
        URL scriptUrl = method.getDeclaringClass().getClassLoader().getResource( scriptFile );

        return scriptUrl;
    }
}