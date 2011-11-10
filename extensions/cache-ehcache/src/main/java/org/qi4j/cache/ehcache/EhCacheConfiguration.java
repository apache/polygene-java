package org.qi4j.cache.ehcache;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

import java.util.List;

public interface EhCacheConfiguration
    extends ConfigurationComposite
{

    @Optional @UseDefaults
    Property<Boolean> clearOnFlush();

    @Optional @UseDefaults
    Property<Integer> diskAccessStripes();

    @Optional @UseDefaults
    Property<Long> diskExpiryThreadIntervalSeconds();

    @Optional @UseDefaults
    Property<Boolean> diskPersistent();

    @Optional @UseDefaults
    Property<Integer> diskSpoolBufferSizeMB();

    @Optional @UseDefaults
    Property<String> diskStorePath();

    @Optional @UseDefaults
    Property<Boolean> eternal();

    @Optional @UseDefaults
    Property<Boolean> loggingEnabled();

    /**
     * Number of objects the ehCache should keep in memory.
     * Defaults to 1000
     *
     * @return The maximum number of elements to be kept in memory.
     */
    @Optional @UseDefaults
    Property<Integer> maxElementsInMemory();

    @Optional @UseDefaults
    Property<Integer> maxElementsOnDisk();

    @Optional @UseDefaults
    Property<String> memoryStoreEvictionPolicy();

    @Optional @UseDefaults
    Property<String> name();

    @Optional @UseDefaults
    Property<Boolean> overflowToDisk();

    @Optional @UseDefaults
    Property<String> transactionalMode();

    @Optional @UseDefaults
    Property<Long> timeToLiveSeconds();

    @Optional @UseDefaults
    Property<Long> timeToIdleSeconds();

    @Optional @UseDefaults
    Property<String> cacheManagerName();

    @Optional @UseDefaults
    Property<String> monitoring();

    @Optional @UseDefaults
    Property<Boolean> updateCheck();
}
