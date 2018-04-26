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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static org.apache.zookeeper.CreateMode.PERSISTENT;
import static org.apache.zookeeper.ZooDefs.Ids.ANYONE_ID_UNSAFE;
import static org.apache.zookeeper.ZooDefs.Perms.ALL;

/**
 * Zookeeper implementation of MapEntityStore.
 */
public class ZookeeperEntityStoreMixin
    implements ServiceActivation, MapEntityStore
{
    private static final String DEFAULT_HOSTPORT = "localhost:2181";
    private static final byte[] EMPTY_DATA = new byte[ 0 ];

    @Service
    @Optional
    private Watcher watcher;

    @This
    private Configuration<ZookeeperEntityStoreConfiguration> configuration;
    private ZooKeeper zkClient;
    private String storageNode;
    private List<ACL> acl;

    @Override
    public void activateService()
        throws Exception
    {
        // Load configuration
        configuration.refresh();
        ZookeeperEntityStoreConfiguration config = configuration.get();
        setupStorageNode( config );

        List<String> hosts = config.hosts().get();
        if( hosts == null || hosts.size() == 0 )
        {
            hosts = Collections.singletonList( DEFAULT_HOSTPORT );
        }
        String hostPort = hosts.get( (int) ( Math.random() * hosts.size() ) );

        int sessionTimeout = config.sessionTimeout().get();

        zkClient = new ZooKeeper( hostPort, sessionTimeout, watcher == null ? new DummyWatcher() : watcher );
        createStorageNodeIfNotExists( config );
    }

    private void setupStorageNode( ZookeeperEntityStoreConfiguration config )
    {
        storageNode = config.storageNode().get();
        while( storageNode.startsWith( "//" ) )
        {
            storageNode = storageNode.substring( 1 );
        }
        while( storageNode.endsWith( "/" ) )
        {
            storageNode = storageNode.substring( 0, storageNode.length() - 1 );
        }
        if( !storageNode.startsWith( "/" ) )
        {
            storageNode = "/" + storageNode;
        }
    }

    private void createStorageNodeIfNotExists( ZookeeperEntityStoreConfiguration config )
        throws KeeperException, InterruptedException
    {
        acl = parseAcls( config.acls().get() );
        String nodeName = config.storageNode().get();
        String[] parts = nodeName.split( "/" );
        String current = "";
        for( String part : parts )
        {
            if( part.length() > 0 )
            {
                current = current + "/" + part;
                Stat stat = zkClient.exists( current, false );
                if( stat == null )
                {
                    zkClient.create( current, EMPTY_DATA, acl, PERSISTENT );
                }
            }
        }
    }

    private List<ACL> parseAcls( List<String> acls )
    {
        List<ACL> result = new ArrayList<>();
        if( acls == null || acls.size() == 0 )
        {
            result.add( new ACL( ALL, ANYONE_ID_UNSAFE ) );
            return result;
        }
        acls.forEach( s -> {
            String[] parts = s.split( "," );
            int perms = Integer.valueOf( parts[ 0 ] );
            String id = parts[ 2 ];
            String scheme = parts[ 1 ];
            result.add( new ACL( perms, new Id( scheme, id ) ) );
        } );
        return result;
    }

    @Override
    public void passivateService()
        throws Exception
    {
        zkClient.close();
    }

    @Override
    public Reader get( EntityReference reference )
    {
        try
        {
            Stat stat = new Stat();
            byte[] data = zkClient.getData( znode( reference ), false, stat );
            if( data == null || stat.getDataLength() == 0 )
            {
                throw new EntityNotFoundException( reference );
            }
            return new StringReader( new String( data ) );
        }
        catch( KeeperException.NoNodeException e )
        {
            throw new EntityNotFoundException( reference );
        }
        catch( InterruptedException | KeeperException e )
        {
            throw new EntityStoreException( "Unable to get Entity " + reference.identity(), e );
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
                        public void close()
                            throws IOException
                        {
                            try
                            {
                                super.close();
                                String znode = znode( ref );
                                byte[] data = toString().getBytes();
                                zkClient.create( znode, data, acl, PERSISTENT );
                            }
                            catch( InterruptedException | KeeperException e )
                            {
                                throw new EntityStoreException( "Unable to apply entity change: newEntity", e );
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
                        public void close()
                            throws IOException
                        {
                            try
                            {
                                super.close();
                                EntityReference ref = mapChange.reference();
                                String znode = znode( ref );
                                Stat stat = zkClient.exists( znode, false );
                                if( stat == null )
                                {
                                    throw new EntityNotFoundException( ref );
                                }
                                int version = stat.getVersion();
                                zkClient.setData( znode, toString().getBytes(), version );
                            }
                            catch( InterruptedException | KeeperException e )
                            {
                                throw new EntityStoreException( "Unable to apply entity change: updateEntity", e );
                            }
                        }
                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                {
                    try
                    {
                        String znode = znode( ref );
                        Stat stat = zkClient.exists( znode, false );
                        int version = stat.getVersion();
                        zkClient.delete( znode, version );
                    }
                    catch( InterruptedException | KeeperException e )
                    {
                        throw new EntityStoreException( "Unable to apply entity change: removeEntity", e );
                    }
                }
            } );
        }
        catch( Exception ex )
        {
            throw new EntityStoreException( "Unable to apply entity changes.", ex );
        }
    }

    private String znode( EntityReference ref )
    {
        return storageNode + '/' + ref.identity().toString();
    }

    @Override
    public Stream<Reader> entityStates()
    {
        try
        {
            List<String> children = zkClient.getChildren( storageNode, false );
            final ItemIterator iterator = new ItemIterator();
            for( String child : children )
            {
                Stat stat = new Stat();
                String path = storageNode + "/" + child;
                byte[] data = zkClient.getData( path, false, stat );
                if( data.length > 0 )
                {
                    String json = new String( data );
                    iterator.queue.offer( new StringReader( json ) );
                }
            }
            return StreamSupport.stream( Spliterators.spliterator( iterator, DISTINCT | NONNULL | IMMUTABLE, 1 ), false );
        }
        catch( KeeperException | InterruptedException e )
        {
            throw new EntityStoreException( "Unable to get entity states.", e );
        }
    }

    private class ItemIterator
        implements Iterator<Reader>
    {
        private final BlockingQueue<Reader> queue = new LinkedBlockingQueue<>();

        @Override
        public boolean hasNext()
        {
            return queue.size() > 0;
        }

        @Override
        public Reader next()
        {
            try
            {
                return queue.take();
            }
            catch( InterruptedException e )
            {
                throw new UndeclaredThrowableException( e );
            }
        }
    }

    private class DummyWatcher
        implements Watcher
    {
        @Override
        public void process( WatchedEvent event )
        {
            // ignore all
        }
    }
}
