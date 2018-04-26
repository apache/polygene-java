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
import com.basho.riak.client.api.commands.buckets.StoreBucketProperties;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.util.HostAndPort;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.common.InvalidApplicationException;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;

/**
 * Riak Protobuf implementation of MapEntityStore.
 */
public class RiakEntityStoreMixin implements ServiceActivation, MapEntityStore, RiakAccessors
{
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8087;

    @This
    private Configuration<RiakEntityStoreConfiguration> configuration;

    private RiakClient riakClient;
    private Namespace namespace;

    @Override
    public void activateService() throws Exception
    {
        // Load configuration
        configuration.refresh();
        RiakEntityStoreConfiguration config = configuration.get();
        String bucketName = config.bucket().get();
        List<String> hosts = config.hosts().get();

        // Setup Riak Cluster Client
        List<HostAndPort> hostsAndPorts = parseHosts( hosts );
        RiakNode.Builder nodeBuilder = new RiakNode.Builder();
        nodeBuilder = configureNodes( config, nodeBuilder );
        nodeBuilder = configureAuthentication( config, nodeBuilder );
        List<RiakNode> nodes = new ArrayList<>();
        for( HostAndPort host : hostsAndPorts )
        {
            nodes.add( nodeBuilder.withRemoteAddress( host ).build() );
        }
        RiakCluster.Builder clusterBuilder = RiakCluster.builder( nodes );
        clusterBuilder = configureCluster( config, clusterBuilder );

        // Start Riak Cluster
        RiakCluster cluster = clusterBuilder.build();
        cluster.start();
        namespace = new Namespace( bucketName );
        riakClient = new RiakClient( cluster );

        // Initialize Bucket
        riakClient.execute( new StoreBucketProperties.Builder( namespace ).build() );
    }

    private RiakNode.Builder configureNodes( RiakEntityStoreConfiguration config, RiakNode.Builder nodeBuilder )
    {
        Integer minConnections = config.minConnections().get();
        Integer maxConnections = config.maxConnections().get();
        Boolean blockOnMaxConnections = config.blockOnMaxConnections().get();
        Integer connectionTimeout = config.connectionTimeout().get();
        Integer idleTimeout = config.idleTimeout().get();
        if( minConnections != null )
        {
            nodeBuilder = nodeBuilder.withMinConnections( minConnections );
        }
        if( maxConnections != null )
        {
            nodeBuilder = nodeBuilder.withMaxConnections( maxConnections );
        }
        nodeBuilder = nodeBuilder.withBlockOnMaxConnections( blockOnMaxConnections );
        if( connectionTimeout != null )
        {
            nodeBuilder = nodeBuilder.withConnectionTimeout( connectionTimeout );
        }
        if( idleTimeout != null )
        {
            nodeBuilder = nodeBuilder.withIdleTimeout( idleTimeout );
        }
        return nodeBuilder;
    }

    private RiakNode.Builder configureAuthentication( RiakEntityStoreConfiguration config,
                                                      RiakNode.Builder nodeBuilder )
        throws IOException, GeneralSecurityException
    {
        String username = config.username().get();
        String password = config.password().get();
        String truststoreType = config.truststoreType().get();
        String truststorePath = config.truststorePath().get();
        String truststorePassword = config.truststorePassword().get();
        String keystoreType = config.keystoreType().get();
        String keystorePath = config.keystorePath().get();
        String keystorePassword = config.keystorePassword().get();
        String keyPassword = config.keyPassword().get();
        if( username != null )
        {
            // Eventually load BouncyCastle to support PKCS12
            if( "PKCS12".equals( keystoreType ) || "PKCS12".equals( truststoreType ) )
            {
                Provider bc = Security.getProvider( "BC" );
                if( bc == null )
                {
                    try
                    {
                        Class<?> bcType = Class.forName( "org.bouncycastle.jce.provider.BouncyCastleProvider" );
                        Security.addProvider( (Provider) bcType.newInstance() );
                    }
                    catch( Exception ex )
                    {
                        throw new InvalidApplicationException(
                            "Need to open a PKCS#12 but unable to register BouncyCastle, check your classpath", ex );
                    }
                }
            }
            KeyStore truststore = loadStore( truststoreType, truststorePath, truststorePassword );
            if( keystorePath != null )
            {
                KeyStore keyStore = loadStore( keystoreType, keystorePath, keystorePassword );
                nodeBuilder = nodeBuilder.withAuth( username, password, truststore, keyStore, keyPassword );
            }
            else
            {
                nodeBuilder = nodeBuilder.withAuth( username, password, truststore );
            }
        }
        return nodeBuilder;
    }

    private KeyStore loadStore( String type, String path, String password )
        throws IOException, GeneralSecurityException
    {
        try( InputStream keystoreInput = new FileInputStream( new File( path ) ) )
        {
            KeyStore keyStore = KeyStore.getInstance( type );
            keyStore.load( keystoreInput, password.toCharArray() );
            return keyStore;
        }
    }

    private RiakCluster.Builder configureCluster( RiakEntityStoreConfiguration config,
                                                  RiakCluster.Builder clusterBuilder )
    {
        Integer clusterExecutionAttempts = config.clusterExecutionAttempts().get();
        if( clusterExecutionAttempts != null )
        {
            clusterBuilder = clusterBuilder.withExecutionAttempts( clusterExecutionAttempts );
        }
        return clusterBuilder;
    }

    @Override
    public void passivateService() throws Exception
    {
        riakClient.shutdown();
        riakClient = null;
        namespace = null;
    }

    @Override
    public RiakClient riakClient()
    {
        return riakClient;
    }

    @Override
    public Namespace riakNamespace()
    {
        return namespace;
    }

    @Override
    public Reader get( EntityReference entityReference )
    {
        try
        {
            Location location = new Location( namespace, entityReference.identity().toString() );
            FetchValue fetch = new FetchValue.Builder( location ).build();
            FetchValue.Response response = riakClient.execute( fetch );
            if( response.isNotFound() )
            {
                throw new EntityNotFoundException( entityReference );
            }
            String jsonState = response.getValue( String.class );
            return new StringReader( jsonState );
        }
        catch( InterruptedException | ExecutionException ex )
        {
            throw new EntityStoreException( "Unable to get Entity " + entityReference.identity(), ex );
        }
    }

    @Override
    public void applyChanges( MapChanges changes )
    {
        try
        {
            changes.visitMap( new MapChanger()
            {
                @Override
                public Writer newEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close() throws IOException
                        {
                            try
                            {
                                super.close();
                                StoreValue store = new StoreValue.Builder( toString() )
                                    .withLocation( new Location( namespace, ref.identity().toString() ) )
                                    .build();
                                riakClient.execute( store );
                            }
                            catch( InterruptedException | ExecutionException ex )
                            {
                                throw new EntityStoreException( "Unable to apply entity change: newEntity", ex );
                            }
                        }
                    };
                }

                @Override
                public Writer updateEntity( MapChange mapChange )
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close() throws IOException
                        {
                            try
                            {
                                super.close();
                                EntityReference reference = mapChange.reference();
                                String identity = reference.identity().toString();
                                Location location = new Location( namespace, identity );
                                FetchValue fetch = new FetchValue.Builder( location ).build();
                                FetchValue.Response response = riakClient.execute( fetch );
                                if( response.isNotFound() )
                                {
                                    throw new EntityNotFoundException( reference );
                                }
                                StoreValue store = new StoreValue.Builder( toString() )
                                    .withLocation( location )
                                    .build();
                                riakClient.execute( store );
                            }
                            catch( InterruptedException | ExecutionException ex )
                            {
                                throw new EntityStoreException( "Unable to apply entity change: updateEntity", ex );
                            }
                        }
                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                {
                    try
                    {
                        Location location = new Location( namespace, ref.identity().toString() );
                        FetchValue fetch = new FetchValue.Builder( location ).build();
                        FetchValue.Response response = riakClient.execute( fetch );
                        if( response.isNotFound() )
                        {
                            throw new EntityNotFoundException( ref );
                        }
                        DeleteValue delete = new DeleteValue.Builder( location ).build();
                        riakClient.execute( delete );
                    }
                    catch( InterruptedException | ExecutionException ex )
                    {
                        throw new EntityStoreException( "Unable to apply entity change: removeEntity", ex );
                    }
                }
            } );
        }
        catch( Exception ex )
        {
            throw new EntityStoreException( "Unable to apply entity changes.", ex );
        }
    }

    @Override
    public Stream<Reader> entityStates()
    {
        try
        {
            ListKeys listKeys = new ListKeys.Builder( namespace ).build();
            ListKeys.Response listKeysResponse = riakClient.execute( listKeys );
            return StreamSupport
                .stream( listKeysResponse.spliterator(), false )
                .map( location ->
                      {
                          try
                          {
                              FetchValue fetch = new FetchValue.Builder( location ).build();
                              FetchValue.Response response = riakClient.execute( fetch );
                              return response.getValue( String.class );
                          }
                          catch( InterruptedException | ExecutionException ex )
                          {
                              throw new EntityStoreException( "Unable to get entity states.", ex );
                          }
                      } )
                .filter( Objects::nonNull )
                .map( StringReader::new );
        }
        catch( InterruptedException | ExecutionException ex )
        {
            throw new EntityStoreException( "Unable to get entity states.", ex );
        }
    }

    private List<HostAndPort> parseHosts( List<String> hosts )
    {
        if( hosts.isEmpty() )
        {
            hosts.add( DEFAULT_HOST );
        }
        List<HostAndPort> addresses = new ArrayList<>( hosts.size() );
        for( String host : hosts )
        {
            String[] splitted = host.split( ":" );
            int port = DEFAULT_PORT;
            if( splitted.length > 1 )
            {
                host = splitted[ 0 ];
                port = Integer.valueOf( splitted[ 1 ] );
            }
            addresses.add( HostAndPort.fromParts( host, port ) );
        }
        return addresses;
    }
}
