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

package org.qi4j.runtime.composite.qi;

import org.qi4j.composite.State;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * TODO
 */
public final class InjectionContext
{
    private CompositeInstance compositeInstance;
    private UsesInstance uses;
    private State state;
    private ModuleInstance moduleInstance;
    private Object next;
    private ProxyReferenceInvocationHandler proxyHandler;

    // For mixins
    public InjectionContext( CompositeInstance compositeInstance, UsesInstance uses, State state )
    {
        this.compositeInstance = compositeInstance;
        this.moduleInstance = compositeInstance.moduleInstance();
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

    public ModuleInstance moduleInstance()
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

    public State state()
    {
        return state;
    }

    public Object next()
    {
        return next;
    }

    public ProxyReferenceInvocationHandler proxyHandler()
    {
        return proxyHandler;
    }
}
