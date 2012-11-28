/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entitystore.jclouds;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.blobstore.BlobMap;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.InputStreamMap;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.io.Input;
import org.qi4j.io.Inputs;
import org.qi4j.io.Output;
import org.qi4j.io.Outputs;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger( "org.qi4j.entitystore.jclouds" );

    private static final Map<String, ApiMetadata> allApis = Maps.uniqueIndex( Apis.viewableAs( BlobStoreContext.class ), Apis
        .idFunction() );

    private static final Map<String, ProviderMetadata> appProviders = Maps.uniqueIndex( Providers.viewableAs( BlobStoreContext.class ), Providers
        .idFunction() );

    private static final Set<String> allKeys = ImmutableSet.copyOf( Iterables.concat( appProviders.keySet(), allApis.keySet() ) );

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
            container = "qi4j-entities";
        }
        storeContext = ContextBuilder.newBuilder( provider ).
            credentials( identifier, credentials ).
            overrides( asProperties( properties ) ).
            buildView( BlobStoreContext.class );
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
        InputStreamMap isMap = storeContext.createInputStreamMap( container );
        InputStream input = isMap.get( entityReference.identity() );

        if( input == null )
        {
            throw new EntityNotFoundException( entityReference );
        }
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Inputs.byteBuffer( input, 4096 ).transferTo( Outputs.byteBuffer( baos ) );
            return new StringReader( baos.toString( "UTF-8" ) );
        }
        catch( IOException ex )
        {
            throw new EntityStoreException( "Unable to read entity state for: " + entityReference, ex );
        }
        finally
        {
            try
            {
                input.close();
            }
            catch( IOException ignored )
            {
            }
        }
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        final BlobMap blobMap = storeContext.createBlobMap( container );

        changes.visitMap( new MapChanger()
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
                        Blob blob = blobMap.blobBuilder().payload( toString() ).build();
                        blobMap.put( ref.identity(), blob );
                    }
                };
            }

            @Override
            public Writer updateEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                throws IOException
            {
                if( !blobMap.containsKey( ref.identity() ) )
                {
                    throw new EntityNotFoundException( ref );
                }
                return new StringWriter()
                {

                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        Blob blob = blobMap.blobBuilder().payload( toString() ).build();
                        blobMap.put( ref.identity(), blob );
                    }
                };
            }

            @Override
            public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                throws EntityNotFoundException
            {
                if( !blobMap.containsKey( ref.identity() ) )
                {
                    throw new EntityNotFoundException( ref );
                }
                blobMap.remove( ref.identity() );
            }
        } );
    }

    @Override
    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {

            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super Reader, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {

                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super Reader, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        InputStreamMap isMap = storeContext.createInputStreamMap( container );
                        for( InputStream eachInput : isMap.values() )
                        {
                            try
                            {
                                receiver.receive( new InputStreamReader( eachInput, "UTF-8" ) );
                            }
                            finally
                            {
                                try
                                {
                                    eachInput.close();
                                }
                                catch( IOException ignored )
                                {
                                }
                            }
                        }
                    }
                } );
            }
        };
    }
}
