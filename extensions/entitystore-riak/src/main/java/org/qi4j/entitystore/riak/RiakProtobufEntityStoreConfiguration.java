/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.entitystore.riak;

import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Configuration for RiakProtobufEntityStore service.
 */
// START SNIPPET: config
public interface RiakProtobufEntityStoreConfiguration
    extends ConfigurationComposite
{

    /**
     * List of Riak Protocol Buffer hosts.
     *
     * Each entry can contain either an IP address / hostname
     * or an IP address / hostname followed by a column and the host's port.
     *
     * Defaulted to 127.0.0.1 if empty.
     */
    @UseDefaults
    Property<List<String>> hosts();

    /**
     * Riak Bucket where Entities state will be stored.
     *
     * Defaulted to "qi4j:entities".
     */
    @Optional
    Property<String> bucket();

    /**
     * Maximum total connections.
     *
     * Defaulted to 50. Use 0 for infinite number of connections.
     */
    @Optional
    Property<Integer> maxConnections();

    /**
     * The connection timeout in milliseconds.
     *
     * Defaulted to 1000.
     */
    @Optional
    Property<Integer> connectionTimeout();

    /**
     * Idle connection time to live in milliseconds.
     *
     * Defaulted to 1000.
     */
    @Optional
    Property<Integer> idleConnectionTTL();

    /**
     * Max pool size.
     *
     * Defaulted to 0 (unlimited).
     */
    @UseDefaults
    Property<Integer> maxPoolSize();

    /**
     * Initial pool size.
     *
     * Defaulted to 0.
     */
    @UseDefaults
    Property<Integer> initialPoolSize();

    /**
     * Socket buffer size in KB.
     *
     * Defaulted to 16.
     */
    @Optional
    Property<Integer> socketBufferSizeKb();

}
// END SNIPPET: config