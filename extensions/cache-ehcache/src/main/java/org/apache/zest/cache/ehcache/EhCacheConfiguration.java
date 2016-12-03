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

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.configuration.ConfigurationComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.library.constraints.annotation.OneOf;

// START SNIPPET: config
public interface EhCacheConfiguration extends ConfigurationComposite {

    /**
     * Heap tier size.
     *
     * Default to 1MB, you may want to change this.
     *
     * @return Heap tier size
     */
    @UseDefaults( "1" )
    Property<Long> heapSize();

    /**
     * Heap tier size unit.
     *
     * @return Heap tier size unit
     */
    @OneOf( { "B", "KB", "MB", "GB", "TB", "PB" } )
    @UseDefaults( "MB" )
    Property<String> heapUnit();

    /**
     * Optional off-heap tier size.
     *
     * @return Optional off-heap tier size
     */
    @Optional
    Property<Long> offHeapSize();

    /**
     * Off-heap tier size unit.
     *
     * @return Off-heap tier size unit
     */
    @OneOf( { "B", "KB", "MB", "GB", "TB", "PB" } )
    @UseDefaults( "MB" )
    Property<String> offHeapUnit();

    /**
     * Optional disk tier size.
     *
     * @return Optional disk tier size
     */
    @Optional
    Property<Long> diskSize();

    /**
     * Disk tier size unit.
     *
     * @return Disk tier size unit
     */
    @OneOf( { "B", "KB", "MB", "GB", "TB", "PB" } )
    @UseDefaults( "MB" )
    Property<String> diskUnit();

    /**
     * If the disk tier is persistent or not.
     *
     * @return If the disk tier is persistent or not
     */
    @UseDefaults
    Property<Boolean> diskPersistent();

    /**
     * Maximum size of cached objects.
     *
     * @return Maximum size of cached objects
     */
    @Optional
    Property<Long> maxObjectSize();

    /**
     * Unit for maximum size of cached objects.
     *
     * @return Unit for maximum size of cached objects
     */
    @OneOf( { "B", "KB", "MB", "GB", "TB", "PB" } )
    @UseDefaults( "MB" )
    Property<String> maxObjectSizeUnit();

    /**
     * Maximum cached object graph depth.
     *
     * @return Maximum cached object graph depth
     */
    @Optional
    Property<Long> maxObjectGraphDepth();

    /**
     * Expiry policy.
     *
     * @return Expiry policy
     */
    @OneOf( { "NONE", "TIME_TO_IDLE", "TIME_TO_LIVE" } )
    @UseDefaults( "NONE" )
    Property<String> expiry();

    /**
     * Expiry length.
     *
     * @return Expiry length
     */
    @Optional
    Property<Long> expiryLength();

    /**
     * Expiry time unit.
     *
     * @return Expiry time unit
     */
    @OneOf( { "MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS" } )
    @UseDefaults( "SECONDS" )
    Property<String> expiryTimeUnit();
}
// END SNIPPET: config
