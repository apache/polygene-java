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
package org.apache.polygene.cache.memcache;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.property.Property;

/**
 * Memcache CachePool Configuration.
 */
// START SNIPPET: config
public interface MemcacheConfiguration
{
    /**
     * Cached items expiration in seconds.
     * Defaulted to 3600 seconds, one hour.
     * @return Cached items expiration configuration property
     */
    @Optional
    Property<Integer> expiration();

    /**
     * Memcached server addresses space separated.
     * Eg. {@literal "server1:11211 server2:11211"}.
     * Defaulted to {@literal "127.0.0.1:11211"}.
     * @return Memcached server addresses configuration property
     */
    @Optional
    Property<String> addresses();

    /**
     * Memcache Protocol.
     * Can be {@literal text} or {@literal binary}
     * Defaulted to {@literal text}.
     * @return Memcache Protocol configuration property
     */
    @Optional
    Property<String> protocol();

    /**
     * Username.
     * Authentication happens only if set.
     * @return Username configuration property
     */
    @Optional
    Property<String> username();

    /**
     * Password.
     * @return Password configuration property
     */
    @Optional
    Property<String> password();

    /**
     * SASL authentication mechanism.
     * Defaulted to PLAIN.
     * @return Authentication mechanism configuration property
     */
    @Optional
    Property<String> authMechanism();
}
// END SNIPPET: config
