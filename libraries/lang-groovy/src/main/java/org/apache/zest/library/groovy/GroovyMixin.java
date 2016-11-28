/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.library.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.common.AppliesToFilter;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.injection.scope.This;

/**
 * Generic mixin that implements interfaces by delegating to Groovy functions
 * using Groovy. Each method in an interface is declared by a Groovy method
 * in a file located in classpath with the name "&lt;interface&gt;.groovy",
 * where the interface name includes the package, and has "." replaced with "/".
 * <p>
 * Example:
 * </p>
 * <pre><code>
 * org/apache/zest/samples/hello/domain/HelloWorldSpeaker.groovy
 * org/apache/zest/samples/hello/domain/HelloWorldSpeaker.sayAgain.groovy
 * </code></pre>
 *
 */
@AppliesTo( GroovyMixin.AppliesTo.class )
public class GroovyMixin
    implements InvocationHandler
{

    @This
    private Composite me;

    private final Map<Class, GroovyObject> groovyObjects;

    public static class AppliesTo
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class compositeType, Class mixin, Class modelClass )
        {
            return getFunctionResource( method ) != null;
        }
    }

    public GroovyMixin()
    {
        groovyObjects = new HashMap<>();
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        final FunctionResource groovySource = getFunctionResource( method );
        if( groovySource != null )
        {
            if( groovySource.script )
            {
                return invokeAsObject( method, args, groovySource.url );
            }
            return invokeAsScript( method, args, groovySource.url );
        }
        throw new RuntimeException( "Internal error: Mixin invoked even if it does not apply" );
    }

    private Object invokeAsObject( Method method, Object[] args, URL groovySource )
        throws Throwable
    {
        try
        {
            Class declaringClass = method.getDeclaringClass();
            GroovyObject groovyObject = groovyObjects.get( declaringClass );
            if( groovyObject == null )
            {
                final Class groovyClass;
                try( InputStream inputStream = groovySource.openStream() )
                {
                    String sourceText = new Scanner( inputStream ).useDelimiter( "\\Z" ).next();
                    GroovyClassLoader groovyClassLoader = new GroovyClassLoader( declaringClass.getClassLoader() );
                    groovyClass = groovyClassLoader.parseClass( sourceText );
                }
                groovyObject = (GroovyObject) groovyClass.newInstance();
                if( hasProperty( groovyObject, "This" ) )
                {
                    groovyObject.setProperty( "This", me );
                }
                groovyObjects.put( declaringClass, groovyObject );
            }
            return groovyObject.invokeMethod( method.getName(), args );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private boolean hasProperty( GroovyObject groovyObject, String propertyName )
    {
        try
        {
            groovyObject.getProperty( propertyName );
            return true;
        }
        catch( MissingPropertyException ex )
        {
            return false;
        }
    }

    private Object invokeAsScript( Method method, Object[] args, URL groovySource )
        throws Throwable
    {
        try
        {
            Binding binding = new Binding();
            binding.setVariable( "This", me );
            binding.setVariable( "args", args );
            GroovyShell shell = new GroovyShell( binding );
            InputStream is = null;
            try
            {
                is = groovySource.openStream();
                return shell.evaluate( new InputStreamReader( is ) );
            }
            finally
            {
                if( is != null )
                {
                    is.close();
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private static FunctionResource getFunctionResource( final Method method )
    {
        boolean script = false;
        final String scriptPath = method.getDeclaringClass().getName().replace( '.', File.separatorChar );
        String scriptFile = scriptPath + "." + method.getName() + ".groovy";
        URL scriptUrl = method.getDeclaringClass().getClassLoader().getResource( scriptFile );
        if( scriptUrl == null )
        {
            script = true;
            scriptFile = scriptPath + ".groovy";
            scriptUrl = method.getDeclaringClass().getClassLoader().getResource( scriptFile );
        }
        if( scriptUrl != null )
        {
            return new FunctionResource( script, scriptUrl );
        }
        return null;
    }

    private static class FunctionResource
    {

        URL url;
        boolean script;

        private FunctionResource( final boolean script, final URL url )
        {
            this.script = script;
            this.url = url;
        }
    }
}