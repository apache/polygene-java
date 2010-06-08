/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.bootstrap;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.DependencyVisitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * This helper is used when building the application model. It keeps track
 * of already created classloaders and various models
 */
public class AssemblyHelper
{
    Map<Class, Class> instantiationClasses = new HashMap<Class, Class>();
    Map<ClassLoader, ClassLoader> modifierClassLoaders = new HashMap<ClassLoader, ClassLoader>();

    public MixinModel getMixinModel( Class mixinClass )
    {
        return new MixinModel( mixinClass, instantiationClass( mixinClass ) );
    }

    public MethodConcernModel getConcernModel( Class concernClass )
    {
        return new MethodConcernModel( concernClass, instantiationClass( concernClass ) );
    }

    public MethodSideEffectModel getSideEffectModel( Class sideEffectClass )
    {
        return new MethodSideEffectModel( sideEffectClass, instantiationClass( sideEffectClass ) );
    }

    private Class instantiationClass( Class fragmentClass )
    {
        Class instantiationClass = fragmentClass;
        if( !InvocationHandler.class.isAssignableFrom( fragmentClass ) )
        {
            instantiationClass = instantiationClasses.get( fragmentClass );

            if( instantiationClass == null )
            {
                try
                {
                    ClassLoader jClassLoader = getModifierClassLoader( fragmentClass.getClassLoader() );
                    instantiationClass = jClassLoader.loadClass( fragmentClass.getName().replace( '$', '_' ) + "_Stub" );
                    instantiationClasses.put( fragmentClass, instantiationClass );
                } catch (ClassNotFoundException e)
                {
                    throw new ConstructionException( "Could not generate mixin subclass", e );
                }
            }
        }
        return instantiationClass;
    }

    private ClassLoader getModifierClassLoader( ClassLoader classLoader )
    {
        ClassLoader cl = modifierClassLoaders.get( classLoader );
        if( cl == null )
        {
            cl = new FragmentClassLoader( classLoader );
            modifierClassLoaders.put( classLoader, cl );
//            System.out.println( "Created classloader for " + classLoader );
        } else
        {
//            System.out.println( "Reused classloader for " + classLoader );
        }
        return cl;
    }
}
