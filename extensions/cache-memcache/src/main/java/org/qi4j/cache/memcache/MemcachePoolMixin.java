/*
 * Copyright 2014 Paul Merlin.
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
package org.qi4j.cache.memcache;

import java.util.HashMap;
import java.util.Map;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.spi.cache.Cache;

import static org.qi4j.api.util.NullArgumentException.validateNotEmpty;

/**
 * Memcache CachePool Mixin.
 */
public class MemcachePoolMixin
    implements MemcachePoolService
{
    private final Map<String, MemcacheImpl<?>> caches = new HashMap<>();
    @This @Optional
    private Configuration<MemcacheConfiguration> configuration;
    private MemcachedClient client;
    private int expiration;

    @Override
    public void activateService()
        throws Exception
    {
        if( configuration != null )
        {
            MemcacheConfiguration config = configuration.get();
            expiration = ( config.expiration().get() == null )
                         ? 3600
                         : config.expiration().get();
            String addresses = ( config.addresses().get() == null )
                               ? "localhost:11211"
                               : config.addresses().get();
            Protocol protocol = ( config.protocol().get() == null )
                                ? Protocol.TEXT
                                : Protocol.valueOf( config.protocol().get().toUpperCase() );
            String username = config.username().get();
            String password = config.password().get();
            String authMech = config.authMechanism().get() == null
                              ? "PLAIN"
                              : config.authMechanism().get();

            ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
            builder.setProtocol( protocol );
            if( username != null && !username.isEmpty() )
            {
                builder.setAuthDescriptor(
                    new AuthDescriptor(
                        new String[]
                        {
                            authMech
                        },
                        new PlainCallbackHandler( username, password )
                    )
                );
            }

            client = new MemcachedClient( builder.build(), AddrUtil.getAddresses( addresses ) );
        }
    }

    @Override
    public void passivateService()
        throws Exception
    {
        if( client != null )
        {
            client.shutdown();
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Cache<T> fetchCache( String cacheId, Class<T> valueType )
    {
        validateNotEmpty( "cacheId", cacheId );
        synchronized( caches )
        {
            MemcacheImpl<?> cache = caches.get( cacheId );
            if( cache == null )
            {
                cache = new MemcacheImpl<>( client, cacheId, valueType, expiration );
                caches.put( cacheId, cache );
            }
            cache.incRefCount();
            return (Cache<T>) cache;
        }
    }

    @Override
    public void returnCache( Cache<?> cache )
    {
        MemcacheImpl<?> memcache = (MemcacheImpl<?>) cache;
        memcache.decRefCount();
        synchronized( caches )
        {
            if( memcache.isNotUsed() )
            {
                caches.remove( memcache.cacheId() );
            }
        }
    }
}
