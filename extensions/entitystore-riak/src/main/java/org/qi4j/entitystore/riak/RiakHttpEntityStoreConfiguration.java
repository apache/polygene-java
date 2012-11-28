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
 * Configuration for RiakHttpEntityStoreService.
 */
// START SNIPPET: config
public interface RiakHttpEntityStoreConfiguration
        extends ConfigurationComposite
{

    /**
     * List of Riak URLs.
     *
     * Defaulted to http://127.0.0.1:8098/riak if empty.
     */
    @UseDefaults
    Property<List<String>> urls();

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
     * The connection, socket read and pooled connection acquisition timeout in milliseconds.
     *
     * Defaulted to 0 (infinite).
     */
    @UseDefaults
    Property<Integer> timeout();

}
// END SNIPPET: config