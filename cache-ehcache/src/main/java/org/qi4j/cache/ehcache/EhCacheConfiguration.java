package org.qi4j.cache.ehcache;

import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

public interface EhCacheConfiguration
    extends ConfigurationComposite
{

    @Optional
    Property<Boolean> clearOnFlush();

    @Optional
    Property<Integer> diskAccessStripes();

    @Optional
    Property<Long> diskExpiryThreadIntervalSeconds();

    @Optional
    Property<Boolean> diskPersistent();

    @Optional
    Property<Integer> diskSpoolBufferSizeMB();

    @Optional
    Property<String> diskStorePath();

    @Optional
    Property<Boolean> eternal();

    @Optional
    Property<Boolean> loggingEnabled();

    /**
     * Number of objects the ehCache should keep in memory.
     * Defaults to 1000
     *
     * @return The maximum number of elements to be kept in memory.
     */
    @Optional
    Property<Integer> maxElementsInMemory();

    @Optional
    Property<Integer> maxElementsOnDisk();

    @Optional
    Property<String> memoryStoreEvictionPolicy();

    @Optional
    Property<String> name();

    @Optional
    Property<Boolean> overflowToDisk();

    @Optional
    Property<String> transactionalMode();

    @Optional
    Property<Long> timeToLiveSeconds();

    @Optional
    Property<Long> timeToIdleSeconds();

    @Optional
    Property<String> cacheManagerName();

    @Optional
    Property<String> monitoring();

    @Optional
    Property<Boolean> updateCheck();

    @Optional
    Property<List<String>> terracottaConfigURLs();
}
