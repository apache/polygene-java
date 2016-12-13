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
package org.apache.polygene.entitystore.geode;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.configuration.ConfigurationComposite;
import org.apache.polygene.api.property.Property;

/**
 * Geode Configuration.
 */
// START SNIPPET: config
public interface GeodeConfiguration
        extends ConfigurationComposite
{
    /**
     * Geode Topology.
     * Defaults to {@literal EMBEDDED}, see {@link GeodeTopology}.
     *
     * @return Geode Topology
     */
    @UseDefaults
    Property<GeodeTopology> topology();

    /**
     * Geode Cache Name.
     *
     * @return Geode Cache Name
     */
    @UseDefaults( "polygene:cache" )
    Property<String> cacheName();

    /**
     * Cache properties path, loaded from the classpath.
     *
     * @return Cache properties path
     */
    @Optional
    Property<String> cachePropertiesPath();

    /**
     * Cache Region Shortcut.
     *
     * In {@literal EMBEDDED} {@link #topology()}, defaults to  {@literal LOCAL},
     * see {@link org.apache.geode.cache.RegionShortcut}.
     *
     * In {@literal CLIENT_SERVER} {@link #topology()}, defaults to {@literal PROXY},
     * see {@link org.apache.geode.cache.client.ClientRegionShortcut}.
     *
     * @return Cache Region Shortcut
     */
    @Optional
    Property<String> regionShortcut();

    /**
     * Geode Region Name.
     *
     * Region names may only be alphanumeric and may contain hyphens or underscores.
     *
     * @return Geode Region Name
     */
    @UseDefaults( "polygene-entitystore-region" )
    Property<String> regionName();
}
// END SNIPPET: config
