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
