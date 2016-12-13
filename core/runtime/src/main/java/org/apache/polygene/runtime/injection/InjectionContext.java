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

package org.apache.polygene.runtime.injection;

import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.runtime.composite.ProxyReferenceInvocationHandler;
import org.apache.polygene.runtime.composite.UsesInstance;

/**
 * JAVADOC
 */
public final class InjectionContext
{
    private final ModuleDescriptor module;
    private CompositeInstance compositeInstance;
    private UsesInstance uses;
    private StateHolder state;
    private Object next; // Only used for concerns and side-effects
    private ProxyReferenceInvocationHandler proxyHandler;
    private Object instance; // Only used for inner classes

    // For mixins

    public InjectionContext( CompositeInstance compositeInstance, UsesInstance uses, StateHolder state )
    {
        this.module = compositeInstance.module();
        this.compositeInstance = compositeInstance;
        this.uses = uses;
        this.state = state;
    }

    // For concerns and side-effects
    public InjectionContext( ModuleDescriptor module, Object next, ProxyReferenceInvocationHandler proxyHandler )
    {
        this.module = module;
        this.next = next;
        this.proxyHandler = proxyHandler;
    }

    public InjectionContext( ModuleDescriptor module, UsesInstance uses )
    {
        this.module = module;
        this.uses = uses;
    }

    // For inner classes
    public InjectionContext( ModuleDescriptor module, UsesInstance uses, Object instance )
    {
        this.module = module;
        this.uses = uses;
        this.instance = instance;
    }

    public ModuleDescriptor module()
    {
        return module;
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
               ", module=" + module +
               ", uses=" + uses +
               ", state=" + state +
               ", next=" + next +
               ", proxyHandler=" + proxyHandler +
               '}';
    }
}
