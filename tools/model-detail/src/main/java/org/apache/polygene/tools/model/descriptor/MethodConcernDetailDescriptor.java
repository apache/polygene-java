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
import org.apache.polygene.api.concern.ConcernDescriptor;

public final class MethodConcernDetailDescriptor
    implements InjectableDetailDescriptor
{
    private final ConcernDescriptor descriptor;
    private MethodConcernsDetailDescriptor concerns;

    private final List<ConstructorDetailDescriptor> constructors;
    private final List<InjectedMethodDetailDescriptor> injectedMethods;
    private final List<InjectedFieldDetailDescriptor> injectedFields;

    MethodConcernDetailDescriptor( ConcernDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        descriptor = aDescriptor;

        constructors = new LinkedList<>();
        injectedMethods = new LinkedList<>();
        injectedFields = new LinkedList<>();
    }

    /**
     * @return Descriptor of this {@code MethodConcernDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final ConcernDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Method concerns that owns this {@code MethodConcernDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final MethodConcernsDetailDescriptor method()
    {
        return concerns;
    }

    /**
     * @return Constructors of this {@code MethodConcernDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<ConstructorDetailDescriptor> constructors()
    {
        return constructors;
    }

    /**
     * @return Injected methods of this {@code MethodConcernDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<InjectedMethodDetailDescriptor> injectedMethods()
    {
        return injectedMethods;
    }

    /**
     * @return Injected methods of this {@code MethodConcernDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<InjectedFieldDetailDescriptor> injectedFields()
    {
        return injectedFields;
    }

   @Override
   public String toString()
   {
      return descriptor.modifierClass().getName();
   }

   final void setConcerns( MethodConcernsDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );
        concerns = aDescriptor;
    }

    final void addConstructor( ConstructorDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMethodConcern( this );
        constructors.add( aDescriptor );
    }

    final void addInjectedMethod( InjectedMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMethodConcern( this );
        injectedMethods.add( aDescriptor );
    }

    final void addInjectedField( InjectedFieldDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        aDescriptor.setMethodConcern( this );
        injectedFields.add( aDescriptor );
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( "fragment", descriptor().modifierClass().getName() );
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