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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParserFactory;
import org.qi4j.injection.scope.Service;
import org.qi4j.library.rdf.entity.EntityParser;
import org.qi4j.service.Activatable;
import org.qi4j.spi.entity.AbstractEntityStoreMixin;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;

/**
 * TODO
 */
public class RESTEntityStoreServiceMixin
    extends AbstractEntityStoreMixin
    implements Activatable
{
    @Service EntityParser parser;

    private AbderaClient client;

    public void activate() throws Exception
    {
        Abdera abdera = new Abdera();
        client = new AbderaClient( abdera );
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
            String uri = "http://localhost:8080/entity/" + anIdentity.type() + "_" + anIdentity.identity();
            ClientResponse response = client.get( uri );
            Document<Entry> feed = response.getDocument();
            Entry entry = feed.getRoot();
            String content = entry.getContent();
            RDFParser rdfParser = new RDFXMLParserFactory().getParser();
            Collection<Statement> statements = new ArrayList<Statement>();
            StatementCollector statementCollector = new StatementCollector( statements );
            rdfParser.setRDFHandler( statementCollector );
            rdfParser.parse( new StringReader( content ), uri );

            long modified = entry.getUpdated().getTime();
            EntityState entityState = new DefaultEntityState( 0, modified,
                                                              anIdentity, EntityStatus.LOADED,
                                                              entityType,
                                                              new HashMap<String, Object>(),
                                                              new HashMap<String, QualifiedIdentity>(),
                                                              DefaultEntityState.newManyCollections( entityType ) );
            parser.parse( statements, entityState );
            return entityState;
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
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
