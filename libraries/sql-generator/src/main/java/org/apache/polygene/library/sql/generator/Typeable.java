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
package org.apache.polygene.library.sql.generator;

import java.util.Map;

/**
 * <p>
 * The {@link Object#getClass()} method returns the actual class of the object. Sometimes, that is not what is wanted;
 * instead it would be good to know which API interface the object implements. Let's say we have a API interface
 * hierarchy, and we know that each object implements only one specific interface (it may implement more, but all others
 * must be then super-interface of the actual implemented one). Then we have a mapping from types of interfaces to some
 * other things, like algorithms or some other functionality. We can not use {@link Map#get(Object)} or
 * {@link Map#containsKey(Object)} methods for the result of {@link Object#getClass()}, since we would much rather use
 * API interfaces than actual implementing classes as keys. And because of that, each time, we need to iterate through
 * whole map, and check for each key that whether the specified object is instance of this key. In other words, quite
 * inefficient.
 * </p>
 * <p>
 * This is when this interface comes to aid. It provides a method returning an API interface/class, which is implemented
 * by the implementation. Then one can use the {@link Map#get(Object)} and {@link Map#containsKey(Object)} methods in
 * order to retrieve the object associated with specific API interface, without the need to iterate through whole map.
 * </p>
 *
 */
public interface Typeable<BaseType>
{

    /**
     * Returns the API interface/class, which is implemented by this object.
     *
     * @return The API interface/class, which is implemented by this object.
     */
    Class<? extends BaseType> getImplementedType();
}