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
package org.apache.polygene.entitystore.mongodb;

import java.util.List;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.property.Property;

// START SNIPPET: config
public interface MongoDBEntityStoreConfiguration
{

    @Optional
    Property<String> hostname();

    @Optional
    Property<Integer> port();

    @UseDefaults
    Property<List<String>> nodes();

    @UseDefaults
    Property<String> username();

    @UseDefaults
    Property<String> password();

    @Optional
    Property<String> database();

    @Optional
    Property<String> collection();

    @UseDefaults
    Property<WriteConcern> writeConcern();

    enum WriteConcern
    {
        /**
         *  Write operations that use this write concern will wait for acknowledgement,
         *  using the default write concern configured on the server.
         *  This is the default value.
         */
        ACKNOWLEDGED,
        /**
         * Write operations that use this write concern will wait for acknowledgement from a single member.
         */
        W1,
        /**
         * Write operations that use this write concern will wait for acknowledgement from two members.
         */
        W2,
        /**
         * Write operations that use this write concern will wait for acknowledgement from three members.
         */
        W3,
        /**
         * Write operations that use this write concern will return as soon as the message is written to the socket.
         * Exceptions are raised for network issues, but not server errors.
         */
        UNACKNOWLEDGED,
        /**
         * Write operations wait for the server to group commit to the journal file on disk.
         */
        JOURNALED,
        /**
         * Exceptions are raised for network issues, and server errors;
         * waits on a majority of servers for the write operation.
         */
        MAJORITY

    }
}
// END SNIPPET: config