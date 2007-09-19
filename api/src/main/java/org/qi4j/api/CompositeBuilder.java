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

import org.qi4j.api.model.CompositeContext;

/**
 * TODO for Rickard; Explanation needed on how to use for Templating, Strategy and Builder patterns.
 */
public interface CompositeBuilder<T extends Composite>
{
    CompositeContext<T> getContext();

    /**
     * Adapts the mixin object to be used for any mixin references missing in the builder, and can
     * be provided by the mixin object.
     *
     * @param mixin The object to use as a mixin.
     */
    void adapt( Object mixin );

    <K, T extends K> void decorate( K object );

    <K, T extends K> void properties( Class<K> mixinType, PropertyValue... properties );

    T properties();

    T newInstance();
}
