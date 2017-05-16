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

package org.apache.polygene.entitystore.jclouds;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.io.Payload;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.contains;

/**
 * JClouds implementation of MapEntityStore.
 */
// TODO Expose Location in configuration
//      To be done once JClouds 1.5 has stabilized their Location API.
//      A list of ISO-3166 country codes.
//      It defines where your entities are allowed to be stored.
//      @UseDefaults Property<List<String>> geopoliticalBoundaries(); ???
//      SEE  http://www.jclouds.org/documentation/reference/location-metadata-design
public class JCloudsMapEntityStoreMixin
    implements ServiceActivation, MapEntityStore
{

    private static final Logger LOGGER = LoggerFactory.getLogger( "org.apache.polygene.entitystore.jclouds" );

    private static final Map<String, ApiMetadata> allApis = Maps.uniqueIndex(
        Apis.viewableAs( BlobStoreContext.class ),
        Apis.idFunction()
    );

    private static final Map<String, ProviderMetadata> appProviders = Maps.uniqueIndex(
        Providers.viewableAs( BlobStoreContext.class ),
        Providers.idFunction()
    );

    private static final Set<String> allKeys = ImmutableSet.copyOf(
        Iterables.concat( appProviders.keySet(), allApis.keySet() )
    );

    @This
    private Configuration<JCloudsMapEntityStoreConfiguration> configuration;

    private BlobStoreContext storeContext;

    private String container;

    @Override
    public void activateService()
        throws Exception
    {
        configuration.refresh();
        String provider = configuration.get().provider().get();
        String identifier = configuration.get().identifier().get();
        String credentials = configuration.get().credential().get();
        String endpoint = configuration.get().endpoint().get();
        Map<String, String> properties = configuration.get().properties().get();
        container = configuration.get().container().get();
        if( provider != null )
        {
            checkArgument( contains( allKeys, provider ), "provider %s not in supported list: %s", provider, allKeys );
        }
        else
        {
            provider = "transient";
        }
        if( container == null )
        {
            container = "polygene-entities";
        }
        storeContext = ContextBuilder.newBuilder( provider )
                                     .endpoint( endpoint == null ? "" : endpoint )
                                     .credentials( identifier, credentials )
                                     .overrides( asProperties( properties ) )
                                     .buildView( BlobStoreContext.class );
        BlobStore blobStore = storeContext.getBlobStore();
        if( !blobStore.containerExists( container ) )
        {
            if( !blobStore.createContainerInLocation( null, container ) )
            {
                throw new EntityStoreException( "Unable to create JClouds Blob Container, cannot continue." );
            }
            else
            {
                LOGGER.debug( "Created new container: {}", container );
            }
        }
        LOGGER.info( "Activated using {} cloud provider [id:{}]", provider, identifier );
    }

    private Properties asProperties( Map<String, String> map )
    {
        Properties props = new Properties();
        for( Map.Entry<String, String> eachEntry : map.entrySet() )
        {
            props.put( eachEntry.getKey(), eachEntry.getValue() );
        }
        return props;
    }

    @Override
    public void passivateService()
        throws Exception
    {
        if( storeContext != null )
        {
            storeContext.close();
            storeContext = null;
            container = null;
        }
    }

    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        Blob blob = storeContext.getBlobStore().getBlob( container, entityReference.identity().toString() );
        if( blob == null )
        {
            throw new EntityNotFoundException( entityReference );
        }
        Payload payload = blob.getPayload();
        if( payload == null )
        {
            throw new EntityNotFoundException( entityReference );
        }
        try( InputStream input = payload.openStream() )
        {
            String state = new Scanner( input, StandardCharsets.UTF_8.name() ).useDelimiter( "\\Z" ).next();
            return new StringReader( state );
        }
        catch( IOException ex )
        {
            throw new EntityStoreException( "Unable to read entity state for: " + entityReference, ex );
        }
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws Exception
    {
        final BlobStore blobStore = storeContext.getBlobStore();
        changes.visitMap(
            new MapChanger()
            {
                @Override
                public Writer newEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter()
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            ByteSource payload = ByteSource.wrap( toString().getBytes( UTF_8 ) );
                            Blob blob = blobStore.blobBuilder( ref.identity().toString() )
                                                 .payload( payload )
                                                 .contentLength( payload.size() )
                                                 .build();
                            blobStore.putBlob( container, blob );
                        }
                    };
                }

                @Override
                public Writer updateEntity( MapChange mapChange )
                    throws IOException
                {
                    String identity = mapChange.reference().identity().toString();
                    if( !blobStore.blobExists( container, identity ) )
                    {
                        throw new EntityNotFoundException( mapChange.reference() );
                    }
                    return new StringWriter()
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            ByteSource payload = ByteSource.wrap( toString().getBytes( UTF_8 ) );
                            Blob blob = blobStore.blobBuilder( identity )
                                                 .payload( payload )
                                                 .contentLength( payload.size() )
                                                 .build();
                            blobStore.putBlob( container, blob );
                        }
                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                    throws EntityNotFoundException
                {
                    if( !blobStore.blobExists( container, ref.identity().toString() ) )
                    {
                        throw new EntityNotFoundException( ref );
                    }
                    blobStore.removeBlob( container, ref.identity().toString() );
                }
            }
        );
    }

    @Override
    public Stream<Reader> entityStates()
    {
        return storeContext
            .getBlobStore().list( container ).stream()
            .map( metadata ->
                  {
                      Payload payload = storeContext.getBlobStore()
                                                    .getBlob( container, metadata.getName() )
                                                    .getPayload();
                      if( payload == null )
                      {
                          EntityReference reference = EntityReference.parseEntityReference( metadata.getName() );
                          throw new EntityNotFoundException( reference );
                      }
                      try( InputStream input = payload.openStream() )
                      {
                          String state = new Scanner( input, UTF_8.name() ).useDelimiter( "\\Z" ).next();
                          return (Reader) new StringReader( state );
                      }
                      catch( IOException ex )
                      {
                          throw new EntityStoreException( ex );
                      }
                  } );
    }
}
