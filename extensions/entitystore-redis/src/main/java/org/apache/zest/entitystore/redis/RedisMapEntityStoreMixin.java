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
package org.apache.zest.entitystore.redis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.stream.Stream;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.spi.entitystore.EntityAlreadyExistsException;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.helpers.MapEntityStore;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * Redis implementation of MapEntityStore.
 */
public class RedisMapEntityStoreMixin
    implements ServiceActivation, RedisAccessors, MapEntityStore
{
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String NIL = "nil";
    @This
    private Configuration<RedisEntityStoreConfiguration> configuration;
    private JedisPool pool;

    @Override
    public void activateService()
        throws Exception
    {
        configuration.refresh();
        RedisEntityStoreConfiguration config = configuration.get();

        String host = config.host().get() == null ? DEFAULT_HOST : config.host().get();
        int port = config.port().get() == null ? Protocol.DEFAULT_PORT : config.port().get();
        int timeout = config.timeout().get() == null ? Protocol.DEFAULT_TIMEOUT : config.timeout().get();
        String password = config.password().get();
        int database = config.database().get() == null ? Protocol.DEFAULT_DATABASE : config.database().get();

        pool = new JedisPool( new JedisPoolConfig(), host, port, timeout, password, database );
    }

    @Override
    public void passivateService()
        throws Exception
    {
        pool.destroy();
        pool = null;
    }

    @Override
    public JedisPool jedisPool()
    {
        return pool;
    }

    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        try( Jedis jedis = pool.getResource() )
        {
            String jsonState = jedis.get( entityReference.identity().toString() );
            if( notFound( jsonState ) )
            {
                throw new EntityNotFoundException( entityReference );
            }
            return new StringReader( jsonState );
        }
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        try( Jedis jedis = pool.getResource() )
        {
            changes.visitMap( new MapChanger()
            {
                @Override
                public Writer newEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            String statusCode = jedis.set( ref.identity().toString(), toString(), "NX" );
                            if( !"OK".equals( statusCode ) )
                            {
                                throw new EntityAlreadyExistsException( ref );
                            }
                        }
                    };
                }

                @Override
                public Writer updateEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            String statusCode = jedis.set( ref.identity().toString(), toString(), "XX" );
                            if( !"OK".equals( statusCode ) )
                            {
                                throw new EntityNotFoundException( ref );
                            }
                        }
                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                    throws EntityNotFoundException
                {
                    String jsonState = jedis.get( ref.identity().toString() );
                    if( notFound( jsonState ) )
                    {
                        throw new EntityNotFoundException( ref );
                    }
                    jedis.del( ref.identity().toString() );
                }
            } );
        }
    }

    @Override
    public Stream<Reader> entityStates()
    {
        Jedis jedis = pool.getResource();
        return jedis.keys( "*" ).stream()
                    .map( key -> (Reader) new StringReader( jedis.get( key ) ) )
                    .onClose( jedis::close );
    }

    private static boolean notFound( String jsonState )
    {
        return jsonState == null || NIL.equals( jsonState );
    }
}
