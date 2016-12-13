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

package org.apache.polygene.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.common.AppliesToFilter;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.constraint.Constraint;
import org.apache.polygene.runtime.composite.ConcernModel;
import org.apache.polygene.runtime.composite.ConstraintDeclaration;
import org.apache.polygene.runtime.composite.FragmentClassLoader;
import org.apache.polygene.runtime.composite.MixinModel;
import org.apache.polygene.runtime.composite.SideEffectModel;

/**
 * This helper is used when building the application model. It keeps track
 * of already created classloaders and various models
 */
@SuppressWarnings("WeakerAccess")
public class AssemblyHelper
{
    private Map<Class, Class> instantiationClasses = new HashMap<>();
    private Map<Class, ConstraintDeclaration> constraintDeclarations = new HashMap<>();
    private Map<ClassLoader, FragmentClassLoader> modifierClassLoaders = new HashMap<>();
    private Map<Class<?>, AppliesToFilter> appliesToInstances = new HashMap<>();

    protected MixinModel getMixinModel(Class mixinClass)
    {
        return new MixinModel( mixinClass, instantiationClass( mixinClass ) );
    }

    protected ConcernModel getConcernModel(Class concernClass)
    {
        return new ConcernModel( concernClass, instantiationClass( concernClass ) );
    }

    protected SideEffectModel getSideEffectModel(Class sideEffectClass)
    {
        return new SideEffectModel( sideEffectClass, instantiationClass( sideEffectClass ) );
    }

    protected Class instantiationClass(Class fragmentClass)
    {
        Class instantiationClass = fragmentClass;
        if( !InvocationHandler.class.isAssignableFrom( fragmentClass ) )
        {
            instantiationClass = instantiationClasses.get( fragmentClass );

            if( instantiationClass == null )
            {
                try
                {
                    FragmentClassLoader fragmentLoader = getModifierClassLoader( fragmentClass.getClassLoader() );
                    instantiationClass = fragmentLoader.loadFragmentClass( fragmentClass );
                    instantiationClasses.put( fragmentClass, instantiationClass );
                }
                catch( ClassNotFoundException | VerifyError e )
                {
                    throw new ConstructionException( "Could not generate mixin subclass " + fragmentClass.getName(), e );
                }
            }
        }
        return instantiationClass;
    }

    protected FragmentClassLoader getModifierClassLoader( ClassLoader classLoader )
    {
        FragmentClassLoader cl = modifierClassLoaders.get( classLoader );
        if( cl == null )
        {
            cl = instantiateFragmentClassLoader( classLoader );
            modifierClassLoaders.put( classLoader, cl );
        }
        return cl;
    }

    protected FragmentClassLoader instantiateFragmentClassLoader( ClassLoader classLoader )
    {
        return new FragmentClassLoader( classLoader );
    }

    public boolean appliesTo( Class<?> fragmentClass, Method method, Iterable<Class<?>> types, Class<?> mixinClass )
    {
        AppliesToFilter appliesToFilter = appliesToInstances.get( fragmentClass );
        if( appliesToFilter == null )
        {
            appliesToFilter = createAppliesToFilter( fragmentClass );
            appliesToInstances.put( fragmentClass, appliesToFilter );
        }
        for( Class<?> compositeType : types )
        {
            if( appliesToFilter.appliesTo( method, mixinClass, compositeType, fragmentClass ) )
            {
                return true;
            }
        }
        return false;
    }

    protected AppliesToFilter createAppliesToFilter( Class<?> fragmentClass )
    {
        AppliesToFilter result = null;
        if( !InvocationHandler.class.isAssignableFrom( fragmentClass ) )
        {
            result = new TypedFragmentAppliesToFilter();
            if( Modifier.isAbstract( fragmentClass.getModifiers() ) )
            {
                result = new AndAppliesToFilter( result, new ImplementsMethodAppliesToFilter() );
            }
        }
        result = applyAppliesTo( result, fragmentClass );
        if( result == null )
        {
            return AppliesToFilter.ALWAYS;
        }
        return result;
    }

    protected AppliesToFilter applyAppliesTo( AppliesToFilter existing, Class<?> modifierClass )
    {
        AppliesTo appliesTo = modifierClass.getAnnotation( AppliesTo.class );
        if( appliesTo != null )
        {
            // Use "or" for all filters specified in the annotation
            AppliesToFilter appliesToAnnotation = null;
            for( Class<?> appliesToClass : appliesTo.value() )
            {
                AppliesToFilter filter;
                if( AppliesToFilter.class.isAssignableFrom( appliesToClass ) )
                {
                    try
                    {
                        @SuppressWarnings("unchecked")
                        Constructor<AppliesToFilter> cons = (Constructor<AppliesToFilter>) appliesToClass.getDeclaredConstructor();
                        cons.setAccessible(true);
                        filter = cons.newInstance();
                    }
                    catch( Exception e )
                    {
                        throw new ConstructionException( e );
                    }
                }
                else if( Annotation.class.isAssignableFrom( appliesToClass ) )
                {
                    filter = new AnnotationAppliesToFilter( appliesToClass );
                }
                else // Type check
                {
                    filter = new TypeCheckAppliesToFilter( appliesToClass );
                }

                if( appliesToAnnotation == null )
                {
                    appliesToAnnotation = filter;
                }
                else
                {
                    appliesToAnnotation = new OrAppliesToFilter( appliesToAnnotation, filter );
                }
            }
            // Add to the rest of the rules using "and"
            if( existing == null )
            {
                return appliesToAnnotation;
            }
            else
            {
                return new AndAppliesToFilter( existing, appliesToAnnotation );
            }
        }
        return existing;
    }

    public boolean appliesTo( Class<? extends Constraint<?, ?>> constraint,
                              Class<? extends Annotation> annotationType,
                              Type valueType
    )
    {
        ConstraintDeclaration constraintDeclaration = constraintDeclarations.get( constraint );
        if( constraintDeclaration == null )
        {
            constraintDeclaration = new ConstraintDeclaration( constraint );
            constraintDeclarations.put( constraint, constraintDeclaration );
        }

        return constraintDeclaration.appliesTo( annotationType, valueType );
    }
}
