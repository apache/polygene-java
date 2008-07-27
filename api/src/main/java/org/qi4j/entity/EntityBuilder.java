/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity;

import org.qi4j.composite.ConstructionException;

/**
 * EntityBuilders are used to instantiate EntityComposites. They can be acquired from
 * {@link org.qi4j.entity.UnitOfWork#newEntityBuilder(Class)} and allows the client
 * to provide additional settings before instantiating the Composite.
 * <p/>
 * It extends Iterable which allows client code to iteratively create new instances. This
 * can be used to implement the prototype pattern.
 */
public interface EntityBuilder<T>
    extends Iterable<T>
{
    T stateOfComposite();

    <K> K stateFor( Class<K> mixinType );

    /**
     * Create a new Composite instance.
     *
     * @return a new Composite instance
     * @throws org.qi4j.composite.ConstructionException
     *          thrown if it was not possible to instantiate the Composite
     */
    T newInstance()
        throws ConstructionException;
}