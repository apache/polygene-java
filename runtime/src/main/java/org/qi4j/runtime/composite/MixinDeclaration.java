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
public final class MixinDeclaration
    implements Serializable
{
    private final Class mixinClass;
    private final Class declaredIn;

    private AppliesToFilter appliesToFilter;
    private final boolean generic;

    public MixinDeclaration( Class mixinClass, Class declaredIn )
    {
        this.mixinClass = mixinClass;
        this.generic = InvocationHandler.class.isAssignableFrom( mixinClass );
        this.declaredIn = declaredIn;

        if( !this.generic )
        {
            appliesToFilter = new TypedFragmentAppliesToFilter();

            if( Modifier.isAbstract( mixinClass.getModifiers() ) )
            {
                appliesToFilter = new AndAppliesToFilter( appliesToFilter, new ImplementsMethodAppliesToFilter() );
            }
        }

        AppliesTo appliesTo = (AppliesTo) mixinClass.getAnnotation( AppliesTo.class );
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
                        filter = (AppliesToFilter) appliesToClass.newInstance();
                    }
                    catch( Exception e )
                    {
                        throw new ConstructionException( e );
                    }
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
            if( appliesToFilter == null )
            {
                appliesToFilter = appliesToAnnotation;
            }
            else
            {
                appliesToFilter = new AndAppliesToFilter( appliesToFilter, appliesToAnnotation );
            }
        }

        if( appliesToFilter == null )
        {
            appliesToFilter = AppliesToFilter.ALWAYS;
        }
    }

    public Class mixinClass()
    {
        return mixinClass;
    }

    public Type declaredIn()
    {
        return declaredIn;
    }

    public boolean isGeneric()
    {
        return generic;
    }

    public boolean appliesTo( Method method, Class<?> compositeType )
    {
        return appliesToFilter.appliesTo( method, mixinClass, compositeType, mixinClass );
    }

    @Override
    public String toString()
    {
        return "Mixin " + mixinClass.getName() + " declared in " + declaredIn;
    }
}
