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

import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.raw.pbc.PBClientConfig;
import com.basho.riak.client.raw.pbc.PBClusterConfig;
import java.util.List;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;

/**
 * Riak Protobuf implementation of MapEntityStore.
 */
public class RiakProtobufMapEntityStoreMixin
    extends AbstractRiakMapEntityStore
{

    private static final int DEFAULT_CONNECTION_TIMEOUT = 1000;
    private static final int DEFAULT_IDLE_CONNECTION_TTL = 1000;
    private static final int DEFAULT_SOCKET_BUFFER_SIZE_KB = 16;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8087;
    @This
    private Configuration<RiakProtobufEntityStoreConfiguration> configuration;

    @Override
    public void activateService()
        throws Exception
    {
        configuration.refresh();
        RiakProtobufEntityStoreConfiguration config = configuration.get();

        int maxConnections = config.maxConnections().get() == null ? DEFAULT_MAX_CONNECTIONS : config.maxConnections().get();
        int connectionTimeout = config.connectionTimeout().get() == null ? DEFAULT_CONNECTION_TIMEOUT : config.connectionTimeout().get();
        int idleConnectionTTL = config.idleConnectionTTL().get() == null ? DEFAULT_IDLE_CONNECTION_TTL : config.idleConnectionTTL().get();
        int maxPoolSize = config.maxPoolSize().get();
        int initialPoolSize = config.initialPoolSize().get();
        int socketBufferSize = config.socketBufferSizeKb().get() == null ? DEFAULT_SOCKET_BUFFER_SIZE_KB : config.socketBufferSizeKb().get();
        List<String> hosts = config.hosts().get();
        if( hosts.isEmpty() )
        {
            hosts.add( DEFAULT_HOST );
        }
        bucketKey = config.bucket().get() == null ? DEFAULT_BUCKET_KEY : config.bucket().get();

        PBClusterConfig pbClusterConfig = new PBClusterConfig( maxConnections );
        for( String host : hosts )
        {
            String[] splitted = host.split( ":" );
            int port = DEFAULT_PORT;
            if( splitted.length > 1 )
            {
                host = splitted[0];
                port = Integer.valueOf( splitted[1] );
            }
            PBClientConfig clientConfig = new PBClientConfig.Builder().withConnectionTimeoutMillis( connectionTimeout ).
                withIdleConnectionTTLMillis( idleConnectionTTL ).
                withPoolSize( maxPoolSize ).
                withInitialPoolSize( initialPoolSize ).
                withSocketBufferSizeKb( socketBufferSize ).
                withHost( host ).withPort( port ).build();
            pbClusterConfig.addClient( clientConfig );
        }
        riakClient = RiakFactory.newClient( pbClusterConfig );

        if( !riakClient.listBuckets().contains( bucketKey ) )
        {
            riakClient.createBucket( bucketKey ).execute();
        }
    }

}
