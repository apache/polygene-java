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

import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.CompositeObject;

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
     * @throws ObjectInstantiationException thrown if instantiation fails
     */
    <T extends Composite> T newInstance( Class<T> aCompositeClass )
        throws ObjectInstantiationException;

    /**
     * Create a new object that implements the given interface.
     * <p/>
     * The new object wraps another object which provides mixin mixins
     * that should be reused for this new object.
     *
     * @param aCompositeClass an interface that describes the object to be created
     * @param anObject        an existing object whose mixins should be reused
     * @return a new proxy object implementing the interface
     */
    <T> T cast( Class<T> aCompositeClass, Object anObject );

    /**
     * Check if an object can implement a given composite class.
     * <p/>
     * This includes checking if generic mixins exist that can satisfy
     * the requirements.
     *
     * @param aCompositeClass
     * @param anObject
     * @return
     */
    boolean isInstance( Class aCompositeClass, Object anObject );

    <T> T getThat( T proxy );

    CompositeModel getCompositeModel( Class<? extends Composite> aCompositeClass );

    CompositeObject getCompositeObject( Composite aComposite);
}
