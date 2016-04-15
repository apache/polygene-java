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
package org.apache.zest.tools.model.descriptor;

import org.apache.zest.api.composite.InjectedFieldDescriptor;

import static org.apache.zest.api.util.NullArgumentException.validateNotNull;

public final class InjectedFieldDetailDescriptor
{
    private final InjectedFieldDescriptor descriptor;
    private ActivatorDetailDescriptor activator;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private MethodConcernDetailDescriptor methodConcern;
    private MethodSideEffectDetailDescriptor methodSideEffect;

    InjectedFieldDetailDescriptor( InjectedFieldDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "InjectedFieldDescriptor", descriptor );
        this.descriptor = descriptor;
    }

    /**
     * @return Descriptor of this {@code InjectedFieldDetailDescriptor}. Never returns {@code null}.
     */
    public final InjectedFieldDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Activator that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final ActivatorDetailDescriptor activator()
    {
        return activator;
    }

    /**
     * @return Object that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final ObjectDetailDescriptor object()
    {
        return object;
    }

    /**
     * @return Mixin that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final MixinDetailDescriptor mixin()
    {
        return mixin;
    }

    /**
     * @return Method concern that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final MethodConcernDetailDescriptor methodConcern()
    {
        return methodConcern;
    }

    /**
     * @return Method side effect that own this {@code InjectedFieldDetailDescriptor}.
     */
    public final MethodSideEffectDetailDescriptor methodSideEffect()
    {
        return methodSideEffect;
    }

    final void setActivator( ActivatorDetailDescriptor descriptor )
    {
        validateNotNull( "ActivatorDetailDescriptor", descriptor );
        activator = descriptor;
    }

    final void setObject( ObjectDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "ObjectDetailDescriptor", descriptor );
        object = descriptor;
    }

    final void setMixin( MixinDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "MixinDetailDescriptor", descriptor );
        mixin = descriptor;
    }

    final void setMethodConcern( MethodConcernDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "MethodConcernDetailDescriptor", descriptor );
        methodConcern = descriptor;
    }

    final void setMethodSideEffect( MethodSideEffectDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "MethodSideEffectDetailDescriptor", descriptor );
        methodSideEffect = descriptor;
    }

    @Override
    public final String toString()
    {
        return descriptor.field().getName();
    }

}
