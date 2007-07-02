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

/**
 * This factory creates proxies that implement the given
 * composite interfaces.
 */
public interface CompositeFactory
{
    /**
     * Create a new object that implements the given interface.
     *
     * @param aCompositeClass an interface that describes the object to be created
     * @return a new proxy object implementing the interface
     * @throws CompositeInstantiationException
     *          thrown if instantiation fails
     */
    <T extends Composite> T newInstance( Class<T> aCompositeClass )
        throws CompositeInstantiationException;

    /**
     * Create a new object that implements the given interface.
     * <p/>
     *
     * @param aCompositeClass an interface that describes the object to be created
     * @param anObject        an existing object whose mixins should be reused
     * @return a new proxy object implementing the interface
     */
    <T extends Composite> T cast( Class<T> aCompositeClass, Composite anObject );

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

    /**
     * Dereference the Proxy Reference to the Composite.
     * <p/>
     * <ul>
     * <li>If the the compositeReference is the Composite itself, it will be returned.</li>
     * <li>If the compositeReference was obtained from a @Uses demarced field, this method will return the
     * full composite object.</li>
     * <li>Otherwise, it returns null.</li>
     * </ul>
     *
     * @param compositeReference
     * @return See above.
     */
    <T> T dereference( T compositeReference );

    CompositeModel getCompositeModel( Class<? extends Composite> aCompositeClass );

    CompositeModel getCompositeModel( Composite aComposite );

    <T extends Composite> CompositeBuilder<T> newCompositeBuilder( Class<T> compositeType );
}
