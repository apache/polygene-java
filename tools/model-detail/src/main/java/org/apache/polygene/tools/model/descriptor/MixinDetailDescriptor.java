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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.mixin.MixinDescriptor;

public final class MixinDetailDescriptor
    implements InjectableDetailDescriptor
{
    private final MixinDescriptor descriptor;
    private CompositeDetailDescriptor composite;
    private final List<ConstructorDetailDescriptor> constructors;
    private final List<InjectedMethodDetailDescriptor> injectedMethods;
    private final List<InjectedFieldDetailDescriptor> injectedFields;

    MixinDetailDescriptor( MixinDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );
        descriptor = aDescriptor;
        constructors = new LinkedList<>();
        injectedMethods = new LinkedList<>();
        injectedFields = new LinkedList<>();
    }

    /**
     * @return Descriptor of this {@code MixinDetailDescriptor}.
     *
     * @since 0.5
     */
    public final MixinDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructors of this {@code MixinDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<ConstructorDetailDescriptor> constructors()
    {
        return constructors;
    }

    /**
     * @return Injected methods of this {@code MixinDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<InjectedMethodDetailDescriptor> injectedMethods()
    {
        return injectedMethods;
    }

    /**
     * @return Injected fields of this {@code MixinDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<InjectedFieldDetailDescriptor> injectedFields()
    {
        return injectedFields;
    }

    /**
     * @return Composite that owns this {@code MixinDetailDescriptor}. Never return {@code null}.
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

    final void addConstructor( ConstructorDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMixin( this );
        constructors.add( aDescriptor );
    }

    final void addInjectedMethod( InjectedMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMixin( this );
        injectedMethods.add( aDescriptor );
    }

    final void addInjectedField( InjectedFieldDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMixin( this );
        injectedFields.add( aDescriptor );
    }

    @Override
    public String toString()
    {
        return descriptor.mixinClass().getSimpleName();
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( "mixin", descriptor().mixinClass().getName() );
        JsonObjectBuilder injectionBuilder = Json.createObjectBuilder();
        {
            JsonArrayBuilder constructorsBuilder = Json.createArrayBuilder();
            constructors().forEach( constructor -> constructorsBuilder.add( constructor.toJson() ) );
            builder.add( "constructors", constructorsBuilder );
        }
        builder.add( "injection", injectionBuilder );
        {
            JsonArrayBuilder injectedFieldsBuilder = Json.createArrayBuilder();
            injectedFields().forEach( field -> injectedFieldsBuilder.add( field.toJson() ) );
            injectionBuilder.add( "fields", injectedFieldsBuilder );
        }
        {
            JsonArrayBuilder injectedMethodsBuilder = Json.createArrayBuilder();
            injectedMethods().forEach( method -> injectedMethodsBuilder.add( method.toJson() ) );
            injectionBuilder.add( "methods", injectedMethodsBuilder );
        }

        return builder;
    }
}