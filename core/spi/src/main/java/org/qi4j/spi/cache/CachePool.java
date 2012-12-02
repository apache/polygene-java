package org.qi4j.spi.cache;

/**
 * A CachePool is a service that manages the Persistence Caches.
 * <p>
 * The CachePool is typically implemented as a Qi4j Extension, and is an optional extension in the persistence
 * subsystem of Qi4j. If a Cache Extension is not provided, caching will be turned off. However, since caching
 * operate on EntityStore level, and is an optional component at that, just because you have defined a Cache
 * Extension does not necessary mean that your system will use it. Check the EntityStore implementations for
 * details if they are Cache enabled. Most EntityStore implementations has this enabled, often via the MapEntityStore
 * and JSONMapEntityStore SPI.
 * </p>
 * <p>
 * NOTE: Make sure that there is a match between the fetchCache and returnCache methods, to ensure no memory leakage
 * occur. Also remember that if the reference count reaches zero, the CachePool will destroy the Cache as soon
 * as possible and a new fetchCache will return an empty one.
 * </p>
 */
public interface CachePool
{

    /**
     * Fetches a cache from the pool.
     * If the cache does not exist already, then a new Cache should be created and returned. For each fetchCache()
     * call, a reference count on the Cache must be increased.
     *
     * @param cacheId   The identity of the cache. If the same id is given as a previous fetch, the same cache will be
     *                  returned.
     * @param valueType
     * @param <T>
     *
     * @return The cache fetched from the pool.
     */
    <T> Cache<T> fetchCache( String cacheId, Class<T> valueType );

    /**
     * Returns the cache back to the pool.
     * The reference count for the cache must then be decreased and if the count reaches zero, the Cache should be
     * destroyed and cleared from memory.
     *
     * @param cache The cache to return to the pool.
     */
    void returnCache( Cache cache );
}
