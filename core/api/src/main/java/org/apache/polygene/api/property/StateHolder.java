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

package org.apache.polygene.api.property;

import java.lang.reflect.AccessibleObject;
import java.util.stream.Stream;

/**
 * This represents the state of a composite (properties).
 */
public interface StateHolder
{
    /**
     * Get a property for a specific accessor
     *
     * @param <T> Property type
     * @param accessor of the property
     *
     * @return the property
     *
     * @throws IllegalArgumentException if no property for given accessor exists
     */
    <T> Property<T> propertyFor( AccessibleObject accessor )
        throws IllegalArgumentException;

    Stream<? extends Property<?>> properties();
}
