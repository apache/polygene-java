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
package org.qi4j.index.elasticsearch;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

// START SNIPPET: config
public interface ElasticSearchClusterConfiguration
        extends ElasticSearchConfiguration
{

    /**
     * Coma separated list of nodes host:port.
     * Defaults to '127.0.0.1:9300'.
     */
    @Optional Property<String> nodes();

    /**
     * Allows client to sniff the rest of the cluster, and add those into its list of machines to use.
     * In this case, note that the ip addresses used will be the ones that the other nodes were started
     * with (the “publish” address).
     * Defaults to FALSE.
     */
    @UseDefaults Property<Boolean> clusterSniff();

    /**
     * Set to true to ignore cluster name validation of connected nodes.
     * Defaults to FALSE.
     */
    @UseDefaults Property<Boolean> ignoreClusterName();

    /**
     * The time to wait for a ping response from a node.
     * Defaults to 5s.
     */
    @Optional Property<String> pingTimeout();

    /**
     * How often to sample / ping the nodes listed and connected.
     * Defaults to 5s.
     */
    @Optional Property<String> samplerInterval();


}
// END SNIPPET: config
