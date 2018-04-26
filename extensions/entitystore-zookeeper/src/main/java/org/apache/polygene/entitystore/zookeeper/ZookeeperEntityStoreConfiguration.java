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
package org.apache.polygene.entitystore.zookeeper;

import java.util.List;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.property.Property;

/**
 * Configuration for ZookeeperEntityStoreService.
 */
// START SNIPPET: config
public interface ZookeeperEntityStoreConfiguration
{
    /**
     * List of Zookeeper hosts
     * <p>
     * Each entry must contain an IP address / hostname followed by a column and the host's port.
     * <p>
     * Defaulted to 127.0.0.1:2181 if empty.
     *
     * @return List of Zookeeper hosts
     */
    @UseDefaults
    Property<List<String>> hosts();

    /**
     * Path/Node in Zookeeper's namespace where Entities state will be stored.
     * <p>
     * Defaulted to "/polygene/store".
     *
     * @return The path of the the node where the entities will be stored.
     */
    @UseDefaults( "/polygene/store" )
    Property<String> storageNode();

    /** Timeout of Session in milliseconds
     */
    @UseDefaults( "10000" )
    Property<Integer> sessionTimeout();

    /**
     * ACL to be used for new znodes.
     * <p>
     * Each String is in the format of;  PERM, SCHEME, ID
     * </p>
     * <p>
     * where <strong>PERM</strong> is an integer by adding together
     * <ul>
     * <li>1 = <code>READ</code> </li>
     * <li>2 = <code>WRITE</code> </li>
     * <li>4 = <code>CREATE</code> </li>
     * <li>8 = <code>DELETE</code> </li>
     * <li>16 = <code>ADMIN</code> </li>
     * </ul>
     * or 31 for <code>ALL</code>, which is also the default value.
     *
     * </p>
     *<p>
     * <strong>SCHEME</strong> is the zookeeper ACL scheme, one of "world", "auth", ...(?)...
     * <br/>
     * Default: "world"
     *</p>
     * <p>
     *     ID is the identity within the SCHEME. For "world" SCHEME, "anyone" is wildcard as can be expected.
     *
     *     Default: "anyone"
     * </p>
     */
    @Optional
    Property<List<String>> acls();
}
// END SNIPPET: config
