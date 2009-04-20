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

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParserFactory;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.library.rdf.entity.EntityStateParser;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entity.helpers.EntityStoreEvents;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvent;
import org.restlet.Uniform;
import org.restlet.data.*;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

import java.io.*;
import java.util.*;

/**
 * EntityStore implementation that uses REST to access EntityState from a server.
 */
public class RESTEntityStoreServiceMixin
        implements EntityStore, EntityStoreEvents, Activatable
{
    @Uses
    EntityStateParser parser;
    @This
    Configuration<RESTEntityStoreConfiguration> config;

    @Service
    private Uniform client;
    private Reference entityStoreUrl;

    protected String uuid;
    private int count;

    public void activate() throws Exception
    {
        uuid = UUID.randomUUID().toString() + "-";
        entityStoreUrl = new Reference(config.configuration().storeUrl().get());
    }

    public void passivate() throws Exception
    {
    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, MetaInfo unitOfWorkMetaInfo )
    {
        return new DefaultEntityStoreUnitOfWork( this, newUnitOfWorkId(), usecase, unitOfWorkMetaInfo );
    }

    public EntityState newEntityState( DefaultEntityStoreUnitOfWork unitOfWork, EntityReference identity, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        return new DefaultEntityState(unitOfWork, identity);
    }

    public EntityState getEntityState( DefaultEntityStoreUnitOfWork unitOfWork, EntityReference identity, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        try
        {
            Reference ref = entityStoreUrl.clone().addSegment(identity.identity());
            Request request = new Request(Method.GET, ref);
            request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JAVA_OBJECT));
            Response response = client.handle(request);
            if (response.getStatus().isSuccess())
            {
                if (response.isEntityAvailable())
                {
                    Representation entity = response.getEntity();
                    return parseEntityState(unitOfWork, identity, ref, response, entity);
                }
            } else if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND))
            {
                throw new EntityNotFoundException(identity);
            }
        }
        catch (EntityStoreException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new EntityStoreException(e);
        }
        throw new EntityStoreException();
    }

    private EntityState parseEntityState(DefaultEntityStoreUnitOfWork uow, EntityReference anReference, Reference ref, Response response, Representation entity)
            throws IOException, RDFParseException, RDFHandlerException, ClassNotFoundException
    {
        if (entity.getMediaType().equals(MediaType.APPLICATION_RDF_XML))
        {
            Reader reader = entity.getReader();
            RDFParser rdfParser = new RDFXMLParserFactory().getParser();
            Collection<Statement> statements = new ArrayList<Statement>();
            StatementCollector statementCollector = new StatementCollector(statements);
            rdfParser.setRDFHandler(statementCollector);
            rdfParser.parse(reader, ref.toString());

            long modified = response.getEntity().getModificationDate().getTime();
            String version = response.getEntity().getTag().getName();
            DefaultEntityState entityState = new DefaultEntityState(uow,version, modified,
                    anReference, EntityStatus.LOADED,
                    new HashSet<EntityTypeReference>(),
                    new HashMap<StateName, String>(),
                    new HashMap<StateName, EntityReference>(),
                    new HashMap<StateName, List<EntityReference>>());
            parser.parse(statements, entityState);
            return entityState;
        } else
        {
            ObjectInputStream oin = new ObjectInputStream(entity.getStream());
            SerializableState serializableState = (SerializableState) oin.readUnshared();
            oin.close();

            EntityReference identity = serializableState.identity();
            Set<EntityTypeReference> entityTypeReferences = serializableState.entityTypeReferences();

            return new DefaultEntityState( uow,
                                           serializableState.version(),
                                           serializableState.lastModified(),
                                           identity,
                                           EntityStatus.LOADED,
                                           entityTypeReferences,
                                           serializableState.properties(),
                                           serializableState.associations(),
                                           serializableState.manyAssociations() );
        }
    }

    public StateCommitter apply( final String unitOfWorkIdentity, final Iterable<UnitOfWorkEvent> events, final Usecase usecase, final MetaInfo metaInfo ) throws EntityStoreException
    {
        Reference ref = entityStoreUrl.clone();

        Response response = client.post(ref, new OutputRepresentation(MediaType.APPLICATION_JAVA_OBJECT)
        {
            public void write(OutputStream outputStream) throws IOException
            {
                ObjectOutputStream oout = new ObjectOutputStream(outputStream);
                oout.writeUTF(unitOfWorkIdentity);
                oout.writeUnshared( usecase );
                oout.writeUnshared( metaInfo );
                oout.writeUnshared(events);
                oout.close();
            }
        });

        if (response.getStatus() == Status.CLIENT_ERROR_CONFLICT)
        {
            // TODO Figure out which ones were changed
            Collection<EntityReference> modifiedReferences = new ArrayList<EntityReference>();
            throw new ConcurrentEntityStateModificationException(modifiedReferences);
        } else if (!response.getStatus().isSuccess())
        {
            throw new EntityStoreException(response.getStatus().toString());
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


    public EntityStoreUnitOfWork visitEntityStates(EntityStateVisitor visitor)
    {
        // TODO Iterate over all EntityStates
        return null;
    }

    private String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

}
