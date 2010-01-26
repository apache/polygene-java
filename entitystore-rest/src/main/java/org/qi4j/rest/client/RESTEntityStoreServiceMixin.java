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
import java.util.UUID;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.library.rdf.entity.EntityStateParser;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

/**
 * EntityStore implementation that uses REST to access EntityState from a server.
 */
public class RESTEntityStoreServiceMixin
    implements EntityStore, EntityStoreSPI, Activatable
{
    @Uses
    private EntityStateParser parser;

    @This
    private Configuration<RESTEntityStoreConfiguration> config;

    @This
    EntityStoreSPI entityStoreSpi;

    @Service
    private Uniform client;
    private Reference entityStoreUrl;

    protected String uuid;
    private int count;

    public void activate()
        throws Exception
    {
        uuid = UUID.randomUUID().toString() + "-";
        entityStoreUrl = new Reference( config.configuration().storeUrl().get() );
    }

    public void passivate()
        throws Exception
    {
    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module );
    }

    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor, Module moduleInstance )
    {
        return null;
    }

    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity,
                                       EntityDescriptor entityType
    )
    {
        return null;
    }

    public EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        try
        {
            Reference ref = entityStoreUrl.clone().addSegment( identity.identity() );
            Request request = new Request( Method.GET, ref );
            request.getClientInfo()
                .getAcceptedMediaTypes()
                .add( new Preference<MediaType>( MediaType.APPLICATION_JAVA_OBJECT ) );
            Response response = new Response( request );
            client.handle( request, response );
            if( response.getStatus().isSuccess() )
            {
                if( response.isEntityAvailable() )
                {
                    Representation entity = response.getEntity();
                    return parseEntityState( unitOfWork, identity, ref, response, entity );
                }
            }
            else if( response.getStatus().equals( Status.CLIENT_ERROR_NOT_FOUND ) )
            {
                throw new EntityNotFoundException( identity );
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

    private EntityState parseEntityState( EntityStoreUnitOfWork uow,
                                          EntityReference anReference,
                                          Reference ref,
                                          Response response,
                                          Representation entity
    )
        throws IOException, RDFParseException, RDFHandlerException, ClassNotFoundException
    {
/*
        Reader reader = entity.getReader();
        RDFParser rdfParser = new RDFXMLParserFactory().getParser();
        Collection<Statement> statements = new ArrayList<Statement>();
        StatementCollector statementCollector = new StatementCollector( statements );
        rdfParser.setRDFHandler( statementCollector );
        rdfParser.parse( reader, ref.toString() );

        long modified = response.getEntity().getModificationDate().getTime();
        String version = response.getEntity().getTag().getName();
        EntityState entityState = new DefaultEntityState( uow, version, modified,
                                                                  anReference, EntityStatus.LOADED,
                                                                  null, // TODO
                                                                  new HashMap<QualifiedName, Object>(),
                                                                  new HashMap<QualifiedName, EntityReference>(),
                                                                  new HashMap<QualifiedName, List<EntityReference>>() );
        parser.parse( statements, entityState );
        return entityState;
*/
        return null;
    }

    public StateCommitter apply( Iterable<EntityState> state, String identity )
    {
/*
        Reference ref = entityStoreUrl.clone();

        Response response = client.post( ref, new OutputRepresentation( MediaType.APPLICATION_JAVA_OBJECT )
        {
            public void write( OutputStream outputStream ) throws IOException
            {
                ObjectOutputStream oout = new ObjectOutputStream( outputStream );
                oout.writeUTF( unitOfWorkIdentity );
                oout.writeUnshared( usecase );
                oout.writeUnshared( metaInfo );
                oout.writeUnshared( events );
                oout.close();
            }
        } );

        if( response.getStatus() == Status.CLIENT_ERROR_CONFLICT )
        {
            // TODO Figure out which ones were changed
            Collection<EntityReference> modifiedReferences = new ArrayList<EntityReference>();
            throw new ConcurrentEntityStateModificationException( modifiedReferences );
        }
        else if( !response.getStatus().isSuccess() )
        {
            throw new EntityStoreException( response.getStatus().toString() );
        }

*/
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

    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor )
    {
        // TODO Iterate over all EntityStates
        return null;
    }

    private String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }
}
