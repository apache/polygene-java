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
 */
package org.apache.zest.entitystore.riak;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.configuration.ConfigurationComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.library.constraints.annotation.OneOf;

import java.util.List;

/**
 * Configuration for RiakEntityStoreService.
 */
// START SNIPPET: config
public interface RiakEntityStoreConfiguration extends ConfigurationComposite
{
    /**
     * List of Riak Protocol Buffer hosts.
     *
     * Each entry can contain either an IP address / hostname
     * or an IP address / hostname followed by a column and the host's port.
     *
     * Defaulted to 127.0.0.1 if empty.
     *
     * @return List of Riak nodes
     */
    @UseDefaults
    Property<List<String>> hosts();

    /**
     * User name to use for authentication.
     *
     * @return Authentication user name
     */
    @Optional
    Property<String> username();

    /**
     * Password to use for authentication.
     *
     * @return Authentication password
     */
    @Optional
    Property<String> password();

    /**
     * Type of the keystore used for server certificate authentication.
     *
     * @return Type of the keystore used for server certificate authentication
     */
    @Optional
    @OneOf( { "PKCS12", "JCEKS", "JKS" } )
    Property<String> truststoreType();

    /**
     * Path of the keystore used for server certificate authentication.
     *
     * @return Path of the keystore used for server certificate authentication
     */
    @Optional
    Property<String> truststorePath();

    /**
     * Password of the keystore used for server certificate authentication.
     *
     * @return Password of the keystore used for server certificate authentication
     */
    @Optional
    Property<String> truststorePassword();

    /**
     * Type of the keystore used for client certificate authentication.
     *
     * @return Type of the keystore used for client certificate authentication
     */
    @Optional
    @OneOf( { "PKCS12", "JCEKS", "JKS" } )
    Property<String> keystoreType();

    /**
     * Path of the keystore used for client certificate authentication.
     *
     * @return Path of the keystore used for client certificate authentication
     */
    @Optional
    Property<String> keystorePath();

    /**
     * Password of the keystore used for client certificate authentication.
     *
     * @return Password of the keystore used for client certificate authentication
     */
    @Optional
    Property<String> keystorePassword();

    /**
     * Password of the key used for client certificate authentication.
     *
     * @return Password of the key used for client certificate authentication
     */
    @Optional
    Property<String> keyPassword();

    /**
     * Riak Bucket where Entities state will be stored.
     *
     * Defaulted to "zest:entities".
     *
     * @return Riak bucket name
     */
    @UseDefaults( "zest:entities" )
    Property<String> bucket();

    /**
     * Cluster execution attempts.
     *
     * @return Cluster execution attempts
     */
    @Optional
    Property<Integer> clusterExecutionAttempts();

    /**
     * Minimum connections per node.
     *
     * @return Minimum connections per node
     */
    @Optional
    Property<Integer> minConnections();

    /**
     * Maximum connections per node.
     *
     * @return Maximum connections per node
     */
    @Optional
    Property<Integer> maxConnections();

    /**
     * Block on maximum connections.
     *
     * @return Block on maximum connections
     */
    @UseDefaults
    Property<Boolean> blockOnMaxConnections();

    /**
     * Connection timeout.
     *
     * @return Connection timeout
     */
    @Optional
    Property<Integer> connectionTimeout();

    /**
     * Idle timeout.
     *
     * @return idle timeout
     */
    @Optional
    Property<Integer> idleTimeout();
}
// END SNIPPET: config
