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
package org.qi4j.api;

/**
 * TODO for Rickard; Explanation needed on how to use for Templating, Strategy and Builder patterns.
 */
public interface CompositeBuilder<T extends Composite>
{
    T newInstance();

    /**
     * TODO: Docs.
     * 
     * @param mixinType
     * @param mixin
     * @return Chaining pattern and returning the builder itself.
     */
    <M> CompositeBuilder<T> set( Class<M> mixinType, M mixin );

    <M> M get( Class<M> mixinType );

//    /**
//     * Clones the values in the <code>from</code> object into this builder.
//     * <p/>
//     * This method will clone all the mixin objects via the clone() method (if <code>Cloneable</code>),
//     * and otherwise try to instantiate the mixin implementation class (if no argument
//     * constructor) and copy the member fields across.
//     *
//     * @param from The object that is to be cloned.
//     */
//    void clone( Composite from );

    /**
     * Adapts the mixin object to be used for any mixin references missing in the builder, and can
     * be provided by the mixin object.
     *
     * @param mixin The object to use as a mixin.
     * @return Chaining pattern and returning the builder itself.
     */
    CompositeBuilder<T> adapt( Object mixin );
}
