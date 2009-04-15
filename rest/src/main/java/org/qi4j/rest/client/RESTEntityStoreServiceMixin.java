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
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.library.rdf.entity.EntityStateParser;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.restlet.Uniform;
import org.restlet.data.*;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * EntityStore implementation that uses REST to access EntityState from a server.
 */
public class RESTEntityStoreServiceMixin
        implements EntityStore, Activatable
{
    @Uses
    EntityStateParser parser;
    @This
    Configuration<RESTEntityStoreConfiguration> config;

    @Service
    private Uniform client;
    private Reference entityStoreUrl;

    public void activate() throws Exception
    {
        entityStoreUrl = new Reference(config.configuration().storeUrl().get());
    }

    public void passivate() throws Exception
    {
    }

    public EntityState newEntityState(EntityReference anReference) throws EntityStoreException
    {
        return new DefaultEntityState(anReference);
    }

    public EntityState getEntityState(EntityReference anReference) throws EntityStoreException
    {
        try
        {
            Reference ref = entityStoreUrl.clone().addSegment(anReference.identity());
            Request request = new Request(Method.GET, ref);
            request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JAVA_OBJECT));
            Response response = client.handle(request);
            if (response.getStatus().isSuccess())
            {
                if (response.isEntityAvailable())
                {
                    Representation entity = response.getEntity();
                    return parseEntityState(anReference, ref, response, entity);
                }
            } else if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND))
            {
                throw new EntityNotFoundException(anReference);
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

    private EntityState parseEntityState(EntityReference anReference, Reference ref, Response response, Representation entity)
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
            long version = Long.parseLong(response.getEntity().getTag().getName());
            DefaultEntityState entityState = new DefaultEntityState(version, modified,
                    anReference, EntityStatus.LOADED,
                    new HashSet<EntityTypeReference>(),
                    new HashMap<StateName, String>(),
                    new HashMap<StateName, EntityReference>(),
                    new HashMap<StateName, ManyAssociationState>());
            parser.parse(statements, entityState);
            entityState.clearModified();
            return entityState;
        } else
        {
            ObjectInputStream oin = new ObjectInputStream(entity.getStream());
            EntityState state = (EntityState) oin.readObject();
            oin.close();
            return state;
        }
    }

    public StateCommitter prepare(final Iterable<EntityState> newStates, final Iterable<EntityState> loadedStates, final Iterable<EntityReference> removedStates) throws EntityStoreException
    {
        Reference ref = entityStoreUrl.clone();

        Response response = client.post(ref, new OutputRepresentation(MediaType.APPLICATION_JAVA_OBJECT)
        {
            public void write(OutputStream outputStream) throws IOException
            {
                ObjectOutputStream oout = new ObjectOutputStream(outputStream);
                oout.writeObject(newStates);
                oout.writeObject(loadedStates);
                oout.writeObject(removedStates);
                oout.close();
            }
        });

        if (response.getStatus() == Status.CLIENT_ERROR_CONFLICT)
        {
            // TODO Figure out which ones were changed
            Collection<EntityReference> modifiedReferences = new ArrayList<EntityReference>();
            for (EntityState loadedState : loadedStates)
            {
                modifiedReferences.add(loadedState.identity());
            }
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

    public void visitEntityStates(EntityStateVisitor visitor)
    {
        // TODO Iterate over all EntityStates
    }
}
