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
package org.apache.polygene.tools.model.descriptor;

import java.util.Objects;
import org.apache.polygene.api.composite.ConstructorDescriptor;

/**
 * Constructor Detail Descriptor.
 */
public final class ConstructorDetailDescriptor
{
    private final ConstructorDescriptor descriptor;
    private ActivatorDetailDescriptor activator;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private MethodConcernDetailDescriptor methodConcern;
    private MethodSideEffectDetailDescriptor methodSideEffect;
    private InjectedParametersDetailDescriptor parameters;

    ConstructorDetailDescriptor( ConstructorDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "ConstructorDescriptor" );
        this.descriptor = descriptor;
        this.parameters = null;
    }

    /**
     * @return Descriptor of this {@code ConstructorDetailDescriptor}. Never return {@code null}.
     */
    public final ConstructorDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructor parameters of this {@code ConstructorDetailDescriptor}. Never return {@code null}.
     */
    public final InjectedParametersDetailDescriptor parameters()
    {
        return parameters;
    }

    /**
     * @return Activator that own this {@code ConstructorDetailDescriptor}.
     */
    public final ActivatorDetailDescriptor activator()
    {
        return activator;
    }

    /**
     * @return Object that own this {@code ConstructorDetailDescriptor}.
     */
    public final ObjectDetailDescriptor object()
    {
        return object;
    }

    /**
     * @return Mixin that own this {@code ConstructorDetailDescriptor}.
     */
    public final MixinDetailDescriptor mixin()
    {
        return mixin;
    }

    /**
     * @return Method concern that own this {@code ConstructorDetailDescriptor}.
     */
    public final MethodConcernDetailDescriptor methodConcern()
    {
        return methodConcern;
    }

    /**
     * @return Method side effect that own this {@code ConstructorDetailDescriptor}.
     */
    public final MethodSideEffectDetailDescriptor methodSideEffect()
    {
        return methodSideEffect;
    }

    final void setActivator( ActivatorDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ActivatorDetailDescriptor" );
        activator = descriptor;
    }

    final void setObject( ObjectDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ObjectDetailDescriptor" );
        object = descriptor;
    }

    final void setMixin( MixinDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "MixinDetailDescriptor" );
        mixin = descriptor;
    }

    final void setMethodConcern( MethodConcernDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "MethodConcernDetailDescriptor" );
        methodConcern = descriptor;
    }

    final void setInjectedParameter( InjectedParametersDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "InjectedParametersDetailDescriptor" );
        descriptor.setConstructor( this );
        parameters = descriptor;
    }

    final void setMethodSideEffect( MethodSideEffectDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "MethodSideEffectDetailDescriptor" );
        methodSideEffect = descriptor;
    }

    @Override
    public final String toString()
    {
        return descriptor.constructor().getDeclaringClass().getSimpleName();
    }

}
