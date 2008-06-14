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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;

/**
 * TODO
 */
public abstract class AbstractModifierDeclaration
{
    protected Class modifierClass;
    protected Type declaredIn;

    private AppliesToFilter appliesToFilter;

    public AbstractModifierDeclaration( Class modifierClass, Type declaredIn )
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
                    appliesToFilter = new AndAppliesToFilter( appliesToFilter, filter );
                }
            }

        }

        if( appliesToFilter == null )
        {
            appliesToFilter = AppliesToFilter.ALWAYS;
        }
    }
}