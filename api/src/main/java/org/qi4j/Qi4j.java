/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j;

import org.qi4j.composite.Composite;

/**
 * Encapsulation of the Qi4j API. The Qi4j holds references
 * to all the API objects in a Qi4j runtime.
 */
public interface Qi4j
{
    Composite dereference( Composite composite );

    /**
     * Get the super Composite of the given Composite. If one Composite
     * type MyComposite is extended by CustomMyComposite interface,
     * the CustomMyComposite is considered to be the super Composite
     * of MyComposite. A Composite may only extend one other Composite,
     * but may extend any number of other interfaces which do not in turn
     * extend Composite.
     *
     * @param compositeClass the Composite type whose super Composite should be returned
     * @return the super Composite of the given Composite, or null if it does not have one
     */
    <S extends Composite, T extends S> Class<S> getSuperComposite( Class<T> compositeClass );

    Class<?> getConfigurationType( Composite serviceComposite );
}
