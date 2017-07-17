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
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.composite.InjectedMethodDescriptor;


public final class InjectedMethodDetailDescriptor
{
    private final InjectedMethodDescriptor descriptor;
    private ActivatorDetailDescriptor activator;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private MethodSideEffectDetailDescriptor methodSideEffect;
    private MethodConcernDetailDescriptor methodConcern;
    private InjectedParametersDetailDescriptor parameters;

    InjectedMethodDetailDescriptor( InjectedMethodDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "InjectedMethodDescriptor" );
        this.descriptor = descriptor;
        this.parameters = null;
    }

    /**
     * @return Descriptor of this {@code InjectedMethodDetailDescriptor}. Never return {@code null}.
     */
    public final InjectedMethodDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Method parameters of this {@code InjectedMethodDetailDescriptor}. Never return {@code null}.
     */
    public final InjectedParametersDetailDescriptor parameters()
    {
        return parameters;
    }

    /**
     * @return Activator that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final ActivatorDetailDescriptor activator()
    {
        return activator;
    }

    /**
     * @return Object that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final ObjectDetailDescriptor object()
    {
        return object;
    }

    /**
     * @return Mixin that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final MixinDetailDescriptor mixin()
    {
        return mixin;
    }

    /**
     * @return Method side effect that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final MethodSideEffectDetailDescriptor methodSideEffect()
    {
        return methodSideEffect;
    }

    /**
     * @return Method concern that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final MethodConcernDetailDescriptor methodConcern()
    {
        return methodConcern;
    }

    final void setActivator( ActivatorDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ActivatorDetailDescriptor" );
        activator = descriptor;
    }

    final void setObject( ObjectDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "ObjectDetailDescriptor" );
        object = descriptor;
    }

    final void setMixin( MixinDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "MixinDetailDescriptor" );
        mixin = descriptor;
    }

    final void setInjectedParameter( InjectedParametersDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "InjectedParametersDetailDescriptor" );

        descriptor.setMethod( this );
        parameters = descriptor;
    }

    final void setMethodSideEffect( MethodSideEffectDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "MethodSideEffectDetailDescriptor" );
        methodSideEffect = descriptor;
    }

    final void setMethodConcern( MethodConcernDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( descriptor, "MethodConcernDetailDescriptor" );
        methodConcern = descriptor;
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( "name", descriptor().method().getName() );
        return builder;
    }
}
