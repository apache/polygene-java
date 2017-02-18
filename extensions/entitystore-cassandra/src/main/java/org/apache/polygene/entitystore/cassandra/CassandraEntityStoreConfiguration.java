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
package org.apache.polygene.entitystore.cassandra;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.configuration.ConfigurationComposite;
import org.apache.polygene.api.property.Property;

// START SNIPPET: config
public interface CassandraEntityStoreConfiguration
    extends ConfigurationComposite
{
    /**
     * A comma or space separated list of <code>hostname:port</code> to the Cassandra cluster.
     * <p>
     *     A small list of hostnames should be given, as the client is capable to discover the topology by itself
     *     and only need one host that it can connect to.
     * </p>
     * <p>
     *     Also not that Cassandra refuse to bind to all interfaces on a host, so you need to know which hostname
     *     corresponds to the interface that Cassandra is bound to. This may not include 'localhost'.
     * </p>
     * @return A comma or space separated list of hostnames (and port) to use to connect to the Cassandra cluster.
     */
    @Optional
    Property<String> hostnames();

    /** The replication factor to be used, if a KEYSPACE is created.
     *
     * @return The replication factor to use in the keyspace if a keyspace is created. Default: 3
     */
    @Optional
    Property<Integer> replicationFactor();

    /**
     * Username for connecting the client to the Cassandra cluster.
     * <p>
     *     The Cassandra client uses the CQL interface.
     * </p>
     * @return The Username to use to connect to the Cassandra cluster. If empty, then no user name will be attempted.
     */
    @UseDefaults
    Property<String> username();

    /**
     * Password for connecting the client to the Cassandra cluster.
     * <p>
     *     The Cassandra client uses the CQL interface.
     * </p>
     * @return The password to use to connect to the Cassandra cluster. If empty, then no password will be attempted.
     */
    @UseDefaults
    Property<String> password();

    /**
     * The name of the KEYSPACE to be used.
     *
     * @return The name of the KEYSPACE to use If null, then the default <code>KEYSPACE polygene</code> will be used.
     */
    @Optional
    Property<String> keySpace();

    /**
     * The name of the entity TABLE to be used.
     * <p>
     * All entities are stored in the same table, with one entity per row. The required table schema is as follows;
     * </p>
     * <pre><code>
     *     CREATE TABLE entitystore (
     *         id text,
     *         version text,
     *         appversion text,
     *         storeversion text,
     *         modified timestamp,
     *         usecase text,
     *         props map<text,text>,
     *         assocs map<text,text>,
     *         manyassocs map<text,text>,
     *         namedassocs map<text,text>,
     *         PRIMARY KEY (id)
     * );
     *
     * </code></pre>
     *
     * @return the name of the Entity table. If it returns null the default name of <code>entitystore</code> will be used.
     */
    @Optional
    Property<String> entityTableName();

    /**
     * Defines whether a KEYSPACE and entity TABLE should be created if not already present in the Cassandra cluster.
     * <p>
     * The default keyspace that could be created is defined as follows;
     * </p>
     * <pre><code>
     *     CREATE KEYSPACE sensetif WITH replication = {'class':'SimpleStrategy', 'replication_factor' : $replicationFactor };
     * </code></pre>
     * <p>
     *     The <code>$replicationFactor</code> refers to the {@link CassandraEntityStoreConfiguration#replicationFactor()}
     *     configuration property above.
     * </p>
     *
     * @return true if the KEYSPACE and TABLE should be created, false if an Exception should be thrown if it is missing.
     */
    @UseDefaults
    Property<Boolean> createIfMissing();
}
// END SNIPPET: config