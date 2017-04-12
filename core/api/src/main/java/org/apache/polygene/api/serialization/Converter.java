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
 */
package org.apache.polygene.api.serialization;

/**
 * Converter for (de)serialization.
 *
 * Convert instances of {@code T} to String and the other way around.
 *
 * @param <T> the converted type
 */
public interface Converter<T>
{
    /**
     * @return the converted type
     */
    Class<T> type();

    /**
     * Convert.
     *
     * @param object the {@code T} to convert to String, never null
     * @return the String representation of the given object
     */
    String toString( T object );

    /**
     * Revert.
     *
     * @param string the String to convert back to {@code T}
     * @return the {@link T}
     */
    T fromString( String string );
}
