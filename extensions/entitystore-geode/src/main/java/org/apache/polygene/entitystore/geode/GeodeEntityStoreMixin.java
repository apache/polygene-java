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
package org.apache.polygene.entitystore.geode;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.stream.Stream;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;

/**
 * Geode EntityStore Mixin.
 */
public class GeodeEntityStoreMixin
        implements ServiceActivation, MapEntityStore
{
    @This
    private Configuration<GeodeEntityStoreConfiguration> config;

    private AutoCloseable closeable;
    private Region<String, String> region;

    @Override
    public void activateService()
            throws Exception
    {
        config.refresh();
        GeodeEntityStoreConfiguration configuration = config.get();
        switch( configuration.topology().get() )
        {
            case EMBEDDED:
                activateEmbedded( configuration );
                break;
            case CLIENT_SERVER:
                activateClientServer( configuration );
                break;
            default:
                throw new IllegalStateException( "Invalid/Unsupported Geode Topology: "
                                                 + configuration.topology().get() );
        }
    }

    private void activateEmbedded( GeodeEntityStoreConfiguration configuration )
            throws IOException
    {
        Properties cacheProperties = buildCacheProperties( configuration );
        String regionShortcutName = configuration.regionShortcut().get();
        RegionShortcut regionShortcut = regionShortcutName == null
                                        ? RegionShortcut.LOCAL
                                        : RegionShortcut.valueOf( regionShortcutName );
        String regionName = configuration.regionName().get();

        CacheFactory cacheFactory = new CacheFactory( cacheProperties );
        Cache cache = cacheFactory.create();
        RegionFactory<String, String> regionFactory = cache.createRegionFactory( regionShortcut );
        region = regionFactory.create( regionName );
        closeable = cache;
    }

    private void activateClientServer( GeodeEntityStoreConfiguration configuration )
            throws IOException
    {
        Properties cacheProperties = buildCacheProperties( configuration );
        String regionShortcutName = configuration.regionShortcut().get();
        ClientRegionShortcut regionShortcut = regionShortcutName == null
                                              ? ClientRegionShortcut.PROXY
                                              : ClientRegionShortcut.valueOf( regionShortcutName );
        String regionName = configuration.regionName().get();

        ClientCacheFactory cacheFactory = new ClientCacheFactory( cacheProperties );
        ClientCache cache = cacheFactory.create();
        ClientRegionFactory<String, String> regionFactory = cache.createClientRegionFactory( regionShortcut );
        region = regionFactory.create( regionName );
        closeable = cache;
    }

    private Properties buildCacheProperties( GeodeEntityStoreConfiguration config )
            throws IOException
    {
        Properties properties = new Properties();
        String cachePropertiesPath = config.cachePropertiesPath().get();
        if( cachePropertiesPath != null )
        {
            try( InputStream input = getClass().getResourceAsStream( cachePropertiesPath ) )
            {
                if( input == null )
                {
                    throw new IllegalStateException( "Geode Cache Properties could not be found: "
                                                     + cachePropertiesPath );
                }
                properties.load( input );
            }
        }
        properties.setProperty( "name", config.cacheName().get() );
        return properties;
    }

    @Override
    public void passivateService()
            throws Exception
    {
        region = null;
        if( closeable != null )
        {
            closeable.close();
            closeable = null;
        }
    }

    @Override
    public Reader get( EntityReference entityReference ) throws EntityStoreException
    {
        String serializedState = region.get( entityReference.identity().toString() );
        if( serializedState == null )
        {
            throw new EntityNotFoundException( entityReference );
        }
        return new StringReader( serializedState );
    }

    @Override
    public void applyChanges( MapChanges changes ) throws Exception
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
                        region.put( ref.identity().toString(), toString() );
                    }
                };
            }

            @Override
            public Writer updateEntity( MapChange mapChange )
                    throws IOException
            {
                return newEntity( mapChange.reference(), mapChange.descriptor() );
            }

            @Override
            public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                    throws EntityNotFoundException
            {
                region.remove( ref.identity().toString() );
            }
        } );
    }

    @Override
    public Stream<Reader> entityStates()
    {
        return region.values().stream().map( StringReader::new );
    }
}
