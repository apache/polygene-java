/*
 * Copyright 2008 Niclas Hedhman
 * Copyright 2007-2008 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.lang.beanshell;

import bsh.BshClassManager;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import bsh.classpath.BshClassPath;
import bsh.classpath.ClassManagerImpl;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.scripting.ScriptException;
import org.qi4j.library.scripting.ScriptReloadable;

/**
 * Generic mixin that implements interfaces by delegating to BeanShell methods
 * Each method in an interface is declared by a BeanShell method
 * in a file located in classpath with the name "&lt;interface&gt;.bsh",
 * where the interface name includes the package, and has "." replaced with "/".
 * <p/>
 * Example:
 * org/qi4j/samples/hello/domain/HelloWorldSpeaker.bsh
 */
@AppliesTo( BeanShellMixin.AppliesTo.class )
public class BeanShellMixin
    implements InvocationHandler, ScriptReloadable
{
    private static HashMap<Module, Interpreter> runtimes;

    @This private Composite me;
    @Structure private Module module;
    @Structure private TransientBuilderFactory compositeBuilderFactory;
    @Structure private UnitOfWorkFactory uowFactory;
    private Map<Class, Object> mixins;

    public static class AppliesTo
        implements AppliesToFilter
    {
        private static Interpreter runtime = new Interpreter();

        @Override
        public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modelClass )
        {
            // Need optimizations so that this method only build the namespace and extract the mixins
            // once per composite.
            try
            {

                NameSpace namespace = buildNamespace( compositeType, runtime );
                if( namespace == null )
                {
                    return false;
                }
                runtime.setNameSpace( namespace );
            }
            catch( IOException e )
            {
                e.printStackTrace();  //TODO: Auto-generated, need attention.
            }
            Map<Class, Object> mixins = extractMixins( runtime.getClassManager() );
            return mixins.containsKey( method.getDeclaringClass() );
        }
    }

    static
    {
        runtimes = new HashMap<Module, Interpreter>();
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        Interpreter runtime;
        synchronized( BeanShellMixin.class )
        {
            runtime = runtimes.get( module );
        }
        if( runtime == null )
        {
            runtime = new Interpreter();
            BshClassManager.createClassManager( runtime );
            Class compositeType = me.getClass().getInterfaces()[ 0 ];
            NameSpace namespace = buildNamespace( compositeType, runtime );
            runtime.setNameSpace( namespace );
            synchronized( BeanShellMixin.class )
            {
                runtimes.put( module, runtime );
            }
            runtime.set( "compositeBuilderFactory", compositeBuilderFactory );
            runtime.set( "unitOfWorkFactory", uowFactory );
        }
        if( mixins == null )
        {
            mixins = extractMixins( runtime.getClassManager() );
        }
        Object instance = mixins.get( method.getDeclaringClass() );
        return method.invoke( instance, args );
    }

    private static NameSpace buildNamespace( Class compositeType, Interpreter runtime )
        throws IOException
    {
        ClassLoader loader = compositeType.getClassLoader();
        BshClassManager classManager = BshClassManager.createClassManager( runtime );
        classManager.setClassLoader( loader );
        NameSpace namespace = new NameSpace( classManager, compositeType.getName() );

        URL scriptUrl = getFunctionResource( compositeType );
        if( scriptUrl == null )
        {
            return null;
        }
        Reader source = getSource( compositeType, scriptUrl );
        try
        {
            runtime.eval( source, namespace, scriptUrl.toString() );
        }
        catch( EvalError evalError )
        {
            evalError.printStackTrace();  //TODO: Auto-generated, need attention.
        }
        return namespace;
    }

    private static Map<Class, Object> extractMixins( BshClassManager classManager )
    {
        Class mixinImpl = null;
        try
        {
            Field field = ClassManagerImpl.class.getDeclaredField( "baseClassPath" );
            field.setAccessible( true );
            BshClassPath classpath = (BshClassPath) field.get( classManager );
            String[] scriptedMixinNames = classpath.getAllNames();
            Map<Class, Object> mixinTypes = new HashMap<Class, Object>();
            for( String mixinName : scriptedMixinNames )
            {
                mixinImpl = classManager.classForName( mixinName );
                Class[] interfaces = mixinImpl.getInterfaces();
                Object mixinInstance = mixinImpl.newInstance();
                for( Class mixinType : interfaces )
                {
                    mixinTypes.put( mixinType, mixinInstance );
                }
            }
            return mixinTypes;
        }
        catch( IllegalAccessException e )
        {
            // Can not happen. Accessible is set to true, and a SecurityException will be thrown
            // if the security doesn't allow direct inspection.
            return null;
        }
        catch( NoSuchFieldException e )
        {
            throw new InternalError( "BeanShell version has been updated and is no longer compatible with this Qi4j version." );
        }
        catch( InstantiationException e )
        {
            throw new ScriptException( "Unable to instantiate BeanShell class: " + mixinImpl );
        }
    }

    protected static Reader getSource( Class compositeType, URL scriptUrl )
        throws IOException
    {
        if( scriptUrl == null )
        {
            throw new IOException( "No script found for method " + compositeType.getName() );
        }
        InputStream in = scriptUrl.openStream();
        return new BufferedReader( new InputStreamReader( in ) );
    }

    private static URL getFunctionResource( Class compositeType )
    {
        String scriptName = getScriptName( compositeType );
        ClassLoader loader = compositeType.getClassLoader();
        return loader.getResource( scriptName );
    }

    private static String getScriptName( Class compositeType )
    {
        String classname = compositeType.getName();
        return classname.replace( '.', '/' ) + ".bsh";
    }

    @Override
    public void reloadScripts()
    {
        synchronized( BeanShellMixin.class )
        {
            runtimes.remove( module );
        }
    }
}