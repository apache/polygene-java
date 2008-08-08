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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParserFactory;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.This;
import org.qi4j.library.rdf.entity.EntityParser;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;
import org.qi4j.spi.entity.AbstractEntityStoreMixin;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;

/**
 * TODO
 */
public class RESTEntityStoreServiceMixin
    extends AbstractEntityStoreMixin
    implements Activatable
{
    @Service EntityParser parser;
    @This Configuration<RESTEntityStoreConfiguration> config;

    private Client client;
    private Reference baseRef;

    public void activate() throws Exception
    {
        client = new Client( Protocol.HTTP );
        baseRef = new Reference( config.configuration().host().get() + "/entity/" );
    }

    public void passivate() throws Exception
    {
    }

    public EntityState newEntityState( QualifiedIdentity anIdentity ) throws EntityStoreException
    {
        return new DefaultEntityState( anIdentity, getEntityType( anIdentity ) );
    }

    public EntityState getEntityState( QualifiedIdentity anIdentity ) throws EntityStoreException
    {
        EntityType entityType = getEntityType( anIdentity );

        try
        {
            String uri = anIdentity.type() + "/" + anIdentity.identity() + ".rdf";
            Reference ref = new Reference( baseRef.toString() + uri );
            Response response = client.get( ref );
            if( response.getStatus().isSuccess() )
            {
                if( response.isEntityAvailable() )
                {
                    Reader reader = response.getEntity().getReader();
                    RDFParser rdfParser = new RDFXMLParserFactory().getParser();
                    Collection<Statement> statements = new ArrayList<Statement>();
                    StatementCollector statementCollector = new StatementCollector( statements );
                    rdfParser.setRDFHandler( statementCollector );
                    rdfParser.parse( reader, uri );

                    long modified = response.getEntity().getModificationDate().getTime();
                    long version = Long.parseLong( response.getEntity().getTag().getName() );
                    EntityState entityState = new DefaultEntityState( version, modified,
                                                                      anIdentity, EntityStatus.LOADED,
                                                                      entityType,
                                                                      new HashMap<String, Object>(),
                                                                      new HashMap<String, QualifiedIdentity>(),
                                                                      DefaultEntityState.newManyCollections( entityType ) );
                    parser.parse( statements, entityState );
                    return entityState;
                }
            }
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
        throw new EntityStoreException();
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException
    {
        return null;
    }

    public Iterator<EntityState> iterator()
    {
        return null;
    }
}
