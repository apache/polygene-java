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
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParserFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.rdf.entity.EntityStateParser;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.Wrapper;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;

/**
 * EntityStore implementation that uses REST to access EntityState from a server.
 */
public class RESTEntityStoreServiceMixin
    extends EntityTypeRegistryMixin
    implements Activatable
{
    @Uses EntityStateParser parser;
    @This Configuration<RESTEntityStoreConfiguration> config;
    @Uses ServiceDescriptor descriptor;

    @Service private Wrapper<Client> client;
    private Reference baseRef;

    public void activate() throws Exception
    {
        baseRef = new Reference( config.configuration().host().get() + "/qi4j/entity/" );
    }

    public void passivate() throws Exception
    {
    }

    public EntityState newEntityState( QualifiedIdentity anIdentity ) throws EntityStoreException
    {
        return new DefaultEntityState( anIdentity, getEntityType( anIdentity.type() ) );
    }

    public EntityState getEntityState( QualifiedIdentity anIdentity ) throws EntityStoreException
    {
        EntityType entityType = getEntityType( anIdentity.type() );

        try
        {
            String uri = anIdentity.type() + "/" + anIdentity.identity();
            Reference ref = new Reference( baseRef.toString() + uri );
            Request request = new Request( Method.GET, ref );
            request.getClientInfo().getAcceptedMediaTypes().add( new Preference<MediaType>( MediaType.APPLICATION_JAVA_OBJECT ) );
            Response response = client.get().handle( request );
            if( response.getStatus().isSuccess() )
            {
                if( response.isEntityAvailable() )
                {
                    Representation entity = response.getEntity();
                    if( entity.getMediaType().equals( MediaType.APPLICATION_RDF_XML ) )
                    {
                        Reader reader = entity.getReader();
                        RDFParser rdfParser = new RDFXMLParserFactory().getParser();
                        Collection<Statement> statements = new ArrayList<Statement>();
                        StatementCollector statementCollector = new StatementCollector( statements );
                        rdfParser.setRDFHandler( statementCollector );
                        rdfParser.parse( reader, uri );

                        long modified = response.getEntity().getModificationDate().getTime();
                        long version = Long.parseLong( response.getEntity().getTag().getName() );
                        DefaultEntityState entityState = new DefaultEntityState( version, modified,
                                                                                 anIdentity, EntityStatus.LOADED,
                                                                                 entityType,
                                                                                 new HashMap<QualifiedName, Object>(),
                                                                                 new HashMap<QualifiedName, QualifiedIdentity>(),
                                                                                 DefaultEntityState.newManyCollections( entityType ) );
                        parser.parse( statements, entityState );
                        entityState.clearModified();
                        return entityState;
                    }
                    else
                    {
                        ObjectInputStream oin = new ObjectInputStream( entity.getStream() );
                        EntityState state = (EntityState) oin.readObject();
                        oin.close();
                        return state;
                    }
                }
            }
            else if( response.getStatus().equals( Status.CLIENT_ERROR_NOT_FOUND ) )
            {
                throw new EntityNotFoundException( descriptor.identity(), anIdentity );
            }
        }
        catch( EntityStoreException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
        throw new EntityStoreException();
    }

    public StateCommitter prepare( final Iterable<EntityState> newStates, final Iterable<EntityState> loadedStates, final Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException
    {
        Reference ref = new Reference( baseRef.toString() );

        Response response = client.get().post( ref, new OutputRepresentation( MediaType.APPLICATION_JAVA_OBJECT )
        {
            public void write( OutputStream outputStream ) throws IOException
            {
                ObjectOutputStream oout = new ObjectOutputStream( outputStream );
                oout.writeObject( newStates );
                oout.writeObject( loadedStates );
                oout.writeObject( removedStates );
                oout.close();
            }
        } );

        if( response.getStatus() == Status.CLIENT_ERROR_CONFLICT )
        {
            // TODO Figure out which ones were changed
            Collection<QualifiedIdentity> modifiedIdentities = new ArrayList<QualifiedIdentity>();
            for( EntityState loadedState : loadedStates )
            {
                modifiedIdentities.add( loadedState.qualifiedIdentity() );
            }
            throw new ConcurrentEntityStateModificationException( descriptor.identity(), modifiedIdentities );
        }
        else if( !response.getStatus().isSuccess() )
        {
            throw new EntityStoreException( response.getStatus().toString() );
        }

        return new StateCommitter()
        {
            public void commit()
            {
            }

            public void cancel()
            {
            }
        };
    }

    public Iterator<EntityState> iterator()
    {
        return null;
    }
}
