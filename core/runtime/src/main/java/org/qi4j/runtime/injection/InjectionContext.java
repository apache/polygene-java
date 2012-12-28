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

package org.qi4j.runtime.injection;

import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * JAVADOC
 */
public final class InjectionContext
{
    private final ModuleInstance moduleInstance;
    private CompositeInstance compositeInstance;
    private UsesInstance uses;
    private StateHolder state;
    private Object next; // Only used for concerns and side-effects
    private ProxyReferenceInvocationHandler proxyHandler;
    private Object instance; // Only used for inner classes

    // For mixins

    public InjectionContext( CompositeInstance compositeInstance, UsesInstance uses, StateHolder state )
    {
        this.moduleInstance = (ModuleInstance) compositeInstance.module();
        this.compositeInstance = compositeInstance;
        this.uses = uses;
        this.state = state;
    }

    // For concerns and side-effects
    public InjectionContext( ModuleInstance moduleInstance, Object next, ProxyReferenceInvocationHandler proxyHandler )
    {
        this.moduleInstance = moduleInstance;
        this.next = next;
        this.proxyHandler = proxyHandler;
    }

    public InjectionContext( ModuleInstance moduleInstance, UsesInstance uses )
    {
        this.moduleInstance = moduleInstance;
        this.uses = uses;
    }

    // For inner classes
    public InjectionContext( ModuleInstance moduleInstance, UsesInstance uses, Object instance )
    {
        this.moduleInstance = moduleInstance;
        this.uses = uses;
        this.instance = instance;
    }

    public ModuleInstance module()
    {
        return moduleInstance;
    }

    public CompositeInstance compositeInstance()
    {
        return compositeInstance;
    }

    public UsesInstance uses()
    {
        return uses;
    }

    public StateHolder state()
    {
        return state;
    }

    public Object next()
    {
        return next;
    }

    public Object instance()
    {
        return instance;
    }

    public ProxyReferenceInvocationHandler proxyHandler()
    {
        return proxyHandler;
    }

    public void setUses( UsesInstance uses )
    {
        this.uses = uses;
    }

    @Override
    public String toString()
    {
        return "InjectionContext{" +
               "compositeInstance=" + compositeInstance +
               ", module=" + moduleInstance +
               ", uses=" + uses +
               ", state=" + state +
               ", next=" + next +
               ", proxyHandler=" + proxyHandler +
               '}';
    }
}
