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
package org.qi4j.api;

/**
 * This factory creates proxies that implement the given
 * composite interfaces.
 */
public interface ObjectFactory
{
    /**
     * Create a new object that implements the given interface.
     *
     * @param aCompositeClass an interface that describes the object to be created
     * @return a new proxy object implementing the interface
     */
    <T> T newInstance( Class<T> aCompositeClass );

    /**
     * Create a new object that implements the given interface.
     * <p/>
     * The new object wraps another object which provides mixin mixins
     * that should be reused for this new object.
     *
     * @param aCompositeClass an interface that describes the object to be created
     * @param anObject     an existing object whose mixins should be reused
     * @return a new proxy object implementing the interface
     */
    <T> T cast( Class<T> aCompositeClass, Object anObject );

    boolean isInstance( Class aCompositeClass, Object anObject );

    <T> T getThat( T proxy );

    Composite getComposite( Class aCompositeClass );
}
