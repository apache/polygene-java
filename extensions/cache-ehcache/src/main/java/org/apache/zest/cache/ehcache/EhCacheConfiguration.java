/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.cache.ehcache;

import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.configuration.ConfigurationComposite;
import org.apache.zest.api.property.Property;

// START SNIPPET: config
public interface EhCacheConfiguration
    extends ConfigurationComposite
{

    @Optional @UseDefaults
    Property<Boolean> clearOnFlush();

    @Optional @UseDefaults
    Property<Integer> diskAccessStripes();

    @Optional @UseDefaults
    Property<Long> diskExpiryThreadIntervalSeconds();

    /**
     * Cache Persistence Strategy.
     *
     * Can be:
     * <ul>
     *   <li>LOCALTEMPSWAP: Standard open source (non fault-tolerant) on-disk persistence.</li>
     *   <li>LOCALRESTARTABLE: Enterprise fault tolerant persistence.</li>
     *   <li>NONE: No persistence.</li>
     *   <li>DISTRIBUTED: Terracotta clustered persistence (requires a Terracotta clustered cache).</li>
     * </ul>
     * Defaults to NONE.
     */
    @Optional
    Property<Strategy> persistenceStrategy();

    @Optional @UseDefaults
    Property<String> diskStorePath();

    @Optional @UseDefaults
    Property<Integer> diskSpoolBufferSizeMB();

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
// END SNIPPET: config
