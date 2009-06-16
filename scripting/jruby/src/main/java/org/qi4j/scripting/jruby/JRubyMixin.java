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
package org.qi4j.scripting.jruby;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyNameError;
import org.jruby.RubyObjectAdapter;
import org.jruby.exceptions.RaiseException;
import org.jruby.internal.runtime.methods.CallConfiguration;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.scripting.common.ScriptReloadable;

/**
 * Generic mixin that implements interfaces by delegating to Ruby functions
 * using JRuby. Each method in an interface is declared by a Ruby method
 * in a file located in classpath with the name "<interface>.rb",
 * where the interface name includes the package, and has "." replaced with "/".
 * <p/>
 * Example:
 * org/qi4j/samples/hello/domain/HelloWorldSpeaker.rb
 */
@AppliesTo( JRubyMixin.AppliesTo.class )
public class JRubyMixin
    implements InvocationHandler, ScriptReloadable
{
    @This private Composite me;

    @Service private Ruby runtime;

    private Map<Class, IRubyObject> rubyObjects = new HashMap<Class, IRubyObject>();

    public static class AppliesTo
        implements AppliesToFilter
    {

        public boolean appliesTo( Method method, Class compositeType, Class mixin, Class modelClass )
        {
            return getFunctionResoure( method ) != null;
        }

    }

    @Structure TransientBuilderFactory factory;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        try
        {
            // Get Ruby object for declaring class of the method
            Class declaringClass = method.getDeclaringClass();
            IRubyObject rubyObject = rubyObjects.get( declaringClass );

            // If not yet created, create one
            if( rubyObject == null )
            {
                // Create object instance
                try
                {
                    rubyObject = runtime.evalScriptlet( declaringClass.getSimpleName() + ".new()" );
                }
                catch( RaiseException e )
                {
                    if( e.getException() instanceof RubyNameError )
                    {
                        // Initialize Ruby class
                        String script = getFunction( method );
                        runtime.evalScriptlet( script );

                        // Try creating a Ruby instance again
                        rubyObject = runtime.evalScriptlet( declaringClass.getSimpleName() + ".new()" );
                    }
                    else
                    {
                        throw e;
                    }
                }

                // Set @this variable to Composite
                IRubyObject meRuby = JavaEmbedUtils.javaToRuby( runtime, me );
                RubyClass rubyClass = meRuby.getMetaClass();
                if( !rubyClass.isFrozen() )
                {
                    SetterDynamicMethod setter = new SetterDynamicMethod( runtime.getObjectSpaceModule(), Visibility.PUBLIC, null );
                    GetterDynamicMethod getter = new GetterDynamicMethod( runtime.getObjectSpaceModule(), Visibility.PUBLIC, null );
                    Method[] compositeMethods = me.getClass().getInterfaces()[ 0 ].getMethods();
                    for( Method compositeMethod : compositeMethods )
                    {
                        if( Property.class.isAssignableFrom( compositeMethod.getReturnType() ) )
                        {

                            rubyClass.addMethod( compositeMethod.getName() + "=", setter );
                            rubyClass.addMethod( compositeMethod.getName(), getter );
                        }
                    }
                    rubyClass.freeze( ThreadContext.newContext( runtime ) );
                }

                RubyObjectAdapter rubyObjectAdapter = JavaEmbedUtils.newObjectAdapter();
                rubyObjectAdapter.setInstanceVariable( rubyObject, "@this", meRuby );
                rubyObjects.put( declaringClass, rubyObject );
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
                rubyResult = rubyObject.callMethod( runtime.getCurrentContext(), method.getName(), rubyArgs );
            }
            else
            {
                rubyResult = rubyObject.callMethod( runtime.getCurrentContext(), method.getName() );
            }

            // Convert result to Java
            Object result = JavaEmbedUtils.rubyToJava( runtime, rubyResult, method.getReturnType() );
            return result;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    public void reloadScripts()
    {
        rubyObjects.clear();
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
        Class<?> declaringClass = method.getDeclaringClass();
        String classname = declaringClass.getName();
        String scriptFile = classname.replace( '.', File.separatorChar ) + ".rb";
        ClassLoader loader = declaringClass.getClassLoader();
        URL scriptUrl = loader.getResource( scriptFile );
        return scriptUrl;
    }

    private static class SetterDynamicMethod
        extends DynamicMethod
    {
        private SetterDynamicMethod( RubyModule rubyModule, Visibility visibility, CallConfiguration callConfiguration )
        {
            super( rubyModule, visibility, callConfiguration );
        }

        public IRubyObject call( ThreadContext threadContext, IRubyObject iRubyObject, RubyModule rubyModule, String methodName, IRubyObject[] iRubyObjects, Block block )
        {
            String propertyName = methodName.substring( 0, methodName.length() - 1 );
            IRubyObject prop = iRubyObject.callMethod( threadContext, propertyName );
            prop.callMethod( threadContext, "set", iRubyObjects );
            return null;
        }

        public DynamicMethod dup()
        {
            return this;
        }

    }

    private static class GetterDynamicMethod
        extends DynamicMethod
    {
        private GetterDynamicMethod( RubyModule rubyModule, Visibility visibility, CallConfiguration callConfiguration )
        {
            super( rubyModule, visibility, callConfiguration );
        }

        public IRubyObject call( ThreadContext threadContext, IRubyObject iRubyObject, RubyModule rubyModule, String methodName, IRubyObject[] iRubyObjects, Block block )
        {
            try
            {
                String propertyName = methodName;
                Object thisComposite = JavaEmbedUtils.rubyToJava( iRubyObject.getRuntime(), iRubyObject, Object.class );
                Method propertyMethod = thisComposite.getClass().getMethod( propertyName );
                Property property = (Property) propertyMethod.invoke( thisComposite );
                Object propertyValue = property.get();
                IRubyObject prop = JavaEmbedUtils.javaToRuby( iRubyObject.getRuntime(), propertyValue );
                return prop;
            }
            catch( Exception e )
            {
                throw new RaiseException( new RubyNameError( iRubyObject.getRuntime(), iRubyObject.getMetaClass(), "Could not find property " + methodName ) );
            }
        }

        public DynamicMethod dup()
        {
            return this;
        }

    }
}