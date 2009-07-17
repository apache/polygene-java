/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.library.rdf.entity.EntityTypeParser;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.EntityTypeRegistrationException;
import org.qi4j.spi.entity.EntityTypeRegistry;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

/**
 * EntityStore implementation that uses REST to access EntityState from a server.
 */
public class RESTEntityTypeRegistryMixin
    implements EntityTypeRegistry, Activatable
{
    protected Map<EntityTypeReference, EntityType> entityTypes = new ConcurrentHashMap<EntityTypeReference, EntityType>();

    @Uses
    EntityTypeParser parser;

    @Uses
    EntityTypeSerializer serializer;

    @This
    Configuration<RESTEntityTypeRegistryConfiguration> config;

    @Service
    private Uniform client;
    private Reference entityRegistryUrl;

    public void activate() throws Exception
    {
        entityRegistryUrl = new Reference( config.configuration().registryUrl().get() );
    }

    public void passivate() throws Exception
    {
    }

    public void registerEntityType( final EntityType entityType )
    {
        EntityTypeReference reference = entityType.reference();
        if( !entityTypes.containsKey( reference ) )
        {
            // Register with server
            Reference ref = entityRegistryUrl.clone().addSegment( entityType.version() );

            Response response = client.put( ref, new OutputRepresentation( MediaType.APPLICATION_JAVA_OBJECT )
            {
                public void write( OutputStream outputStream ) throws IOException
                {
                    ObjectOutputStream oout = new ObjectOutputStream( outputStream );
                    oout.writeUnshared( entityType );
                }
            } );


            if( response.getStatus().isSuccess() )
            {
                entityTypes.put( reference, entityType );
            }
            else
            {
                throw new EntityTypeRegistrationException( "Could not register EntityType" );
            }
        }
    }

    public EntityType getEntityType( EntityTypeReference type )
        throws UnknownEntityTypeException
    {
        EntityType entityType = entityTypes.get( type );
        if( entityType == null )
        {
            // Get type from the server
            Reference ref = entityRegistryUrl.clone().addSegment( type.version() );
            Request request = new Request( Method.GET, ref );
            request.getClientInfo().getAcceptedMediaTypes().add( new Preference<MediaType>( MediaType.APPLICATION_JAVA_OBJECT ) );
            Response response = client.handle( request );
            if( response.getStatus().isSuccess() )
            {
                if( response.isEntityAvailable() )
                {
                    try
                    {
                        Representation entity = response.getEntity();
                        ObjectInputStream oin = new ObjectInputStream( entity.getStream() );
                        entityType = (EntityType) oin.readUnshared();
                        entityTypes.put( type, entityType );
                    }
                    catch( Exception e )
                    {
                        throw (UnknownEntityTypeException) new UnknownEntityTypeException( type.toString() ).initCause( e );
                    }
                }
                else
                {
                    throw new UnknownEntityTypeException( type.toString() );
                }
            }
            else if( response.getStatus().equals( Status.CLIENT_ERROR_NOT_FOUND ) )
            {
                throw new UnknownEntityTypeException( type.toString() );
            }
        }
        return entityType;
    }

    public Iterable<EntityType> getEntityTypes()
    {
        // TODO Implement this
        return Collections.<EntityType>emptyList();
    }
}