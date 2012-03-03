/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.api.object;

import org.qi4j.api.common.ConstructionException;

/**
 * This factory creates and injects POJO's.
 */
public interface ObjectFactory
{
    /**
     * Create new objects of the given type.
     *
     * @param type an object class which will be instantiated.
     *
     * @return new objects.
     *
     * @throws ConstructionException Thrown if instantiation fails.
     * @throws NoSuchObjectException Thrown if {@code type} class is not an object.
     */
    <T> T newObject( Class<T> type, Object... uses )
        throws NoSuchObjectException, ConstructionException;

    /**
     * Inject an existing instance. Only fields and methods will be called.
     *
     * @param instance
     *
     * @throws ConstructionException
     */
    void injectTo( Object instance, Object... uses )
        throws ConstructionException;
}