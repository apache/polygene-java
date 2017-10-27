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
package org.apache.polygene.entitystore.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.awaitility.Awaitility;
import org.awaitility.Duration;

class RiakFixture
{
    private final RiakClient client;
    private final Namespace namespace;

    RiakFixture( RiakClient client, Namespace namespace )
    {
        this.client = client;
        this.namespace = namespace;
    }

    void waitUntilReady()
    {
        System.out.println( ">> Riak HealthCheck BEGIN" );
        Instant start = Instant.now();
        Awaitility.await()
                  .pollDelay( Duration.ZERO )
                  .pollInterval( Duration.ONE_SECOND )
                  .timeout( Duration.ONE_MINUTE )
                  .until( new HealthCheck() );
        System.out.println( ">> Riak HealthCheck END, took " + java.time.Duration.between( start, Instant.now() ) );
    }

    void deleteTestData()
    {
        // Riak doesn't expose bucket deletion in its API so we empty it
        if( namespace != null )
        {
            try
            {
                ListKeys listKeys = new ListKeys.Builder( namespace ).build();
                ListKeys.Response listKeysResponse = client.execute( listKeys );
                for( Location location : listKeysResponse )
                {
                    DeleteValue delete = new DeleteValue.Builder( location ).build();
                    client.execute( delete );
                }
            }
            catch(Exception e )
            {
                System.err.println("WARNING: Unable to clean up test data in RiakFixture" );
                e.printStackTrace();
            }
        }
    }

    private class HealthCheck implements Callable<Boolean>
    {
        @Override
        public Boolean call()
        {
            boolean inserted = false;
            boolean deleted = false;
            Location location = new Location( namespace, "HEALTH_CHECK_ID" );
            try
            {
                StoreValue store = new StoreValue.Builder( "DATA" ).withLocation( location ).build();
                client.execute( store );
                inserted = true;
            }
            catch( Throwable ex )
            {
                ex.printStackTrace();
                return false;
            }
            finally
            {
                if( inserted )
                {
                    try
                    {
                        DeleteValue delete = new DeleteValue.Builder( location ).build();
                        client.execute( delete );
                        deleted = true;
                    }
                    catch( Throwable ex )
                    {
                        ex.printStackTrace();
                    }
                }
            }
            return inserted && deleted;
        }
    }
}
