/*
 * Copyright 2010 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.cache;

/**
 * Interface to interact with Cache implementations.
 * The Cache interface has the simple put/get/remove method to make implementations very easy and straight forward.
 * The key is always a String, since it is intended to be used for the EntityComposite's identity, and not totally
 * generic.
 *
 * @param <T> The Value type to be stored in the cache.
 */
public interface Cache<T>
{
    T get( String key );

    T remove( String key );

    void put( String key, T value );

    boolean exists( String key );
}
