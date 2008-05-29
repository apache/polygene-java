/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;

/**
 * TODO
 */
public final class ConcernDeclaration
{
    private Class concernClass;
    private Type declaredIn;

    private AppliesToFilter appliesToFilter;

    public ConcernDeclaration( Class concernClass, Type declaredIn )
    {
        this.concernClass = concernClass;
        this.declaredIn = declaredIn;

        createAppliesToFilter( concernClass );
    }

    public Class concernClass()
    {
        return concernClass;
    }

    public Type declaredIn()
    {
        return declaredIn;
    }

    public boolean appliesTo( Method method, Class<?> compositeType )
    {
        return appliesToFilter.appliesTo( method, concernClass, compositeType, concernClass );
    }

    @Override public String toString()
    {
        return "Concern " + concernClass.getName() + " declared in " + declaredIn;
    }

    private void createAppliesToFilter( Class concernClass )
    {
        if( !InvocationHandler.class.isAssignableFrom( concernClass ) )
        {
            appliesToFilter = new TypedConcernAppliesToFilter();
        }

        AppliesTo appliesTo = (AppliesTo) concernClass.getAnnotation( AppliesTo.class );
        if( appliesTo != null )
        {
            for( Class<?> appliesToClass : appliesTo.value() )
            {
                AppliesToFilter filter;
                if( AppliesToFilter.class.isAssignableFrom( appliesToClass ) )
                {
                    try
                    {
                        filter = (AppliesToFilter) appliesToClass.newInstance();
                    }
                    catch( Exception e )
                    {
                        throw new org.qi4j.composite.InstantiationException( e );
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

                if( appliesToFilter == null )
                {
                    appliesToFilter = filter;
                }
                else
                {
                    appliesToFilter = new ChainedAppliesToFilter( appliesToFilter, filter );
                }
            }

        }

        if( appliesToFilter == null )
        {
            appliesToFilter = new AlwaysAppliesToFilter();
        }
    }

    private class ChainedAppliesToFilter
        implements AppliesToFilter
    {
        private AppliesToFilter left;
        private AppliesToFilter right;

        private ChainedAppliesToFilter( AppliesToFilter left, AppliesToFilter right )
        {
            this.left = left;
            this.right = right;
        }

        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            return left.appliesTo( method, mixin, compositeType, fragmentClass ) &&
                   right.appliesTo( method, mixin, compositeType, fragmentClass );
        }
    }

    private class TypeCheckAppliesToFilter
        implements AppliesToFilter
    {
        private Class type;

        private TypeCheckAppliesToFilter( Class type )
        {
            this.type = type;
        }

        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            return type.isAssignableFrom( compositeType );
        }
    }

    private class AnnotationAppliesToFilter
        implements AppliesToFilter
    {
        private Class annotationType;

        private AnnotationAppliesToFilter( Class type )
        {
            this.annotationType = type;
        }

        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            return method.getAnnotation( annotationType ) != null;
        }
    }

    private class TypedConcernAppliesToFilter
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            return method.getDeclaringClass().isAssignableFrom( fragmentClass );
        }
    }

    private class AlwaysAppliesToFilter
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            return true;
        }
    }
}