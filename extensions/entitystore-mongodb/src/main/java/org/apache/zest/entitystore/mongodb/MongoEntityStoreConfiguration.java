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
package org.apache.zest.entitystore.mongodb;

import com.mongodb.ServerAddress;
import java.util.List;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.configuration.ConfigurationComposite;
import org.apache.zest.api.property.Property;

// START SNIPPET: config
public interface MongoEntityStoreConfiguration
    extends ConfigurationComposite
{

    @Optional
    Property<String> hostname();

    @Optional
    Property<Integer> port();

    @UseDefaults
    Property<List<ServerAddress>> nodes();

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

        /** Exceptions are raised for network issues, but not server errors */
        NORMAL,
        /** Exceptions are raised for network issues, and server errors; waits on a server for the write operation */
        SAFE,
        /** Exceptions are raised for network issues, and server errors; waits on a majority of servers for the write operation */
        MAJORITY,
        /** Exceptions are raised for network issues, and server errors; the write operation waits for the server to flush the data to disk*/
        FSYNC_SAFE,
        /** Exceptions are raised for network issues, and server errors; the write operation waits for the server to group commit to the journal file on disk*/
        JOURNAL_SAFE,
        /** Exceptions are raised for network issues, and server errors; waits for at least 2 servers for the write operation*/
        REPLICAS_SAFE;

    }

}
// END SNIPPET: config