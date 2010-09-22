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
import java.lang.reflect.Type;
import org.qi4j.api.common.AppliesToFilter;

/**
 * JAVADOC
 */
public abstract class FragmentDeclaration
    implements Serializable
{
    private final Class fragmentClass;
    private final Class declaredIn;
    private final AppliesToFilter appliesToFilter;
    private final boolean generic;

    public FragmentDeclaration( Class fragmentClass, Class declaredIn )
    {
        this.fragmentClass = fragmentClass;
        this.declaredIn = declaredIn;
        appliesToFilter = new AppliesToFactory().createAppliesToFilter( fragmentClass );
        this.generic = InvocationHandler.class.isAssignableFrom( fragmentClass );
    }

    public Class type()
    {
        return fragmentClass;
    }

    public Type declaredIn()
    {
        return declaredIn;
    }

    public boolean isGeneric()
    {
        return generic;
    }

    public Class fragmentClass()
    {
        return fragmentClass;
    }

    public boolean appliesTo( Method method, Class<?> compositeType )
    {
        return appliesToFilter.appliesTo( method, fragmentClass, compositeType, fragmentClass );
    }

    @Override
    public String toString()
    {
        return fragmentClass.getName() + ( declaredIn == null ? "" : " declared in " + declaredIn );
    }
}