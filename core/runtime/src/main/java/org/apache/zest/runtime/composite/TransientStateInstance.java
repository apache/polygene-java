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
package org.apache.zest.runtime.composite;

import java.lang.reflect.AccessibleObject;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.StateHolder;

/**
 * TODO
 */
public final class TransientStateInstance
    implements StateHolder
{
    private final Map<AccessibleObject, Property<?>> properties;

    public TransientStateInstance( Map<AccessibleObject, Property<?>> properties
    )
    {
        this.properties = properties;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Property<T> propertyFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        Property<T> property = (Property<T>) properties.get( accessor );

        if( property == null )
        {
            throw new IllegalArgumentException( "No such property:" + accessor );
        }

        return property;
    }

    @Override
    public Stream<Property<?>> properties()
    {
        return properties.values().stream();
    }
}
