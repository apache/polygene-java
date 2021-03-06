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

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.composite.MethodDescriptor;

public final class CompositeMethodDetailDescriptor
{
    private final MethodDescriptor descriptor;

    private CompositeDetailDescriptor composite;
    private MethodConstraintsDetailDescriptor constraints;
    private MethodConcernsDetailDescriptor concerns;
    private MethodSideEffectsDetailDescriptor sideEffects;

    CompositeMethodDetailDescriptor( MethodDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );
        descriptor = aDescriptor;

        composite = null;
        constraints = null;
        sideEffects = null;
    }

    /**
     * @return Descriptor of this {@code CompositeMethodDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final MethodDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constraints of this {@code CompositeMethodDetailDescriptor}.
     * Returns {@code null} if this method does not have any constraints.
     * @since 0.5
     */
    public final MethodConstraintsDetailDescriptor constraints()
    {
        return constraints;
    }

    /**
     * @return Concerns of this {@code CompositeMethodDetailDescriptor}. Returns {@code null} if this method does not
     * have any concerns.
     * @since 0.5
     */
    public final MethodConcernsDetailDescriptor concerns()
    {
        return concerns;
    }

    /**
     * @return Side-effects of this {@code CompositeMethodDetailDescriptor}. Returns {@code null}
     * if this method does not have any side effects.
     * @since 0.5
     */
    public final MethodSideEffectsDetailDescriptor sideEffects()
    {
        return sideEffects;
    }

    /**
     * @return Composite that owns this {@code CompositeMethodDetailDescriptor}.
     * @since 0.5
     */
    public final CompositeDetailDescriptor composite()
    {
        return composite;
    }

    final void setComposite( CompositeDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );
        composite = aDescriptor;
    }

    final void setConstraints( MethodConstraintsDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMethod( this );
        constraints = aDescriptor;
    }

    public void setConcerns( MethodConcernsDetailDescriptor aDescriptor )
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMethod( this );
        concerns = aDescriptor;
    }

    final void setSideEffects( MethodSideEffectsDetailDescriptor aDescriptor )
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMethod( this );
        sideEffects = aDescriptor;
    }

    @Override
    public final String toString()
    {
        return descriptor.method().getName();
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( "name", descriptor().method().getName() );
        builder.add( "type", descriptor().method().getReturnType().getName() );
        {
            JsonArrayBuilder paramsBuilder = Json.createArrayBuilder();
            Arrays.stream( descriptor().method().getParameters() )
                  .map( Parameter::toString )
                  .forEach( paramsBuilder::add );
//                  .forEach( param -> paramsBuilder.add( param.getName() + " : " + param.getType() ) );
            builder.add( "parameters", paramsBuilder );
        }
        builder.add( "constraints", constraints().toJson() );
        builder.add( "concerns", concerns().toJson() );
        builder.add( "sideeffects", sideEffects().toJson() );

        return builder;
    }
}