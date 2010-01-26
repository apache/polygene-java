/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.composite;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.common.ConstructionException;

/**
 * JAVADOC
 */
public abstract class AbstractModifierDeclaration
    implements Serializable
{
    private final Class modifierClass;
    private final Class declaredIn;

    private AppliesToFilter appliesToFilter;

    public AbstractModifierDeclaration( Class modifierClass, Class declaredIn )
    {
        this.modifierClass = modifierClass;
        this.declaredIn = declaredIn;
        createAppliesToFilter( modifierClass );
    }

    public Class type()
    {
        return modifierClass;
    }

    public Type declaredIn()
    {
        return declaredIn;
    }

    public boolean appliesTo( Method method, Class<?> compositeType )
    {
        return appliesToFilter.appliesTo( method, modifierClass, compositeType, modifierClass );
    }

    private void createAppliesToFilter( Class<?> modifierClass )
    {
        if( !InvocationHandler.class.isAssignableFrom( modifierClass ) )
        {
            appliesToFilter = new TypedModifierAppliesToFilter();

            if( Modifier.isAbstract( modifierClass.getModifiers() ) )
            {
                appliesToFilter = new AndAppliesToFilter( appliesToFilter, new ImplementsMethodAppliesToFilter() );
            }
        }

        AppliesTo appliesTo = modifierClass.getAnnotation( AppliesTo.class );
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

                if( appliesToFilter == null )
                {
                    appliesToFilter = filter;
                }
                else
                {
                    appliesToFilter = new AndAppliesToFilter( appliesToFilter, filter );
                }
            }
        }

        if( appliesToFilter == null )
        {
            appliesToFilter = AppliesToFilter.ALWAYS;
        }
    }

    @Override
    public String toString()
    {
        return modifierClass.getName() + ( declaredIn == null ? "" : " declared in " + declaredIn );
    }
}