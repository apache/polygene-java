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
    Map<Class, MixinModel> mixinModels = new HashMap<Class, MixinModel>();
    Map<Class, MethodConcernModel> concernModels = new HashMap<Class, MethodConcernModel>();
    Map<Class, MethodSideEffectModel> sideEffectModels = new HashMap<Class, MethodSideEffectModel>();
    Map<ClassLoader, ClassLoader> modifierClassLoaders = new HashMap<ClassLoader, ClassLoader>();

    InvocationCheck check = new InvocationCheck();

    public MixinModel getMixinModel( Class mixinClass )
    {
        MixinModel model = mixinModels.get( mixinClass );
        if( model == null )
        {
            model = new MixinModel( mixinClass, instantiationClass( mixinClass ) );
            mixinModels.put( mixinClass, model );
        } else
        {
            // Reused
//            System.out.println( "Reused " + mixinClass );
        }

        return model;
    }

    public MethodConcernModel getConcernModel( Class concernClass )
    {
        MethodConcernModel model = concernModels.get( concernClass );
        if( model == null )
        {
            model = new MethodConcernModel( concernClass, instantiationClass( concernClass ) );
            if( !check.hasInvocationInjections( model ) )
                concernModels.put( concernClass, model );
        } else
        {
            // Reused
//            System.out.println( "Reused " + concernClass );
        }

        return model;
    }

    public MethodSideEffectModel getSideEffectModel( Class sideEffectClass )
    {
        MethodSideEffectModel model = sideEffectModels.get( sideEffectClass );
        if( model == null )
        {
            model = new MethodSideEffectModel( sideEffectClass, instantiationClass( sideEffectClass ) );
            if( !check.hasInvocationInjections( model ) )
                sideEffectModels.put( sideEffectClass, model );
        } else
        {
            // Reused
//            System.out.println( "Reused " + sideEffectClass );
        }

        return model;
    }

    private Class instantiationClass( Class fragmentClass )
    {
        Class instantiationClass = fragmentClass;
        if( !InvocationHandler.class.isAssignableFrom( fragmentClass ) )
        {
/*
            if( Modifier.isAbstract( fragmentClass.getModifiers() ) )
            {
*/
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
//            }
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

    class InvocationCheck
            extends DependencyVisitor
    {
        InvocationCheck()
        {
            super( new DependencyModel.ScopeSpecification( Invocation.class ) );
        }

        boolean hasInvocationInjections;

        @Override
        public void visitDependency( DependencyModel dependencyModel )
        {
            hasInvocationInjections = true;
        }

        public boolean hasInvocationInjections( MethodConcernModel model )
        {
            hasInvocationInjections = false;
            model.visitModel( this );
            return hasInvocationInjections;
        }

        public boolean hasInvocationInjections( MethodSideEffectModel model )
        {
            hasInvocationInjections = false;
            model.visitModel( this );
            return hasInvocationInjections;
        }
    }
}
