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
import org.apache.polygene.api.constraint.ConstraintDescriptor;

public final class MethodConstraintDetailDescriptor
{
    private final ConstraintDescriptor descriptor;
    private MethodConstraintsDetailDescriptor constraints;

    MethodConstraintDetailDescriptor( ConstraintDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );

        descriptor = aDescriptor;
    }

    /**
     * @return Descriptor of this {@code MethodConstraintDetailDescriptor}. Never returns {@code null}.
     *
     * @since 0.5
     */
    public final ConstraintDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constraints that own this {@code MethodConstraintDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final MethodConstraintsDetailDescriptor constraints()
    {
        return constraints;
    }

   @Override
   public String toString()
   {
      return descriptor.annotation().toString();
   }

   final void setConstraints( MethodConstraintsDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        Objects.requireNonNull( aDescriptor, "aDescriptor" );
        constraints = aDescriptor;
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        return builder;
    }
}