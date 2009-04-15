/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.rdf.internal;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.spi.entity.*;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * JAVADOC Add JavaDoc
 */
public class RdfEntityIndexerMixin
        implements EntityStoreListener, Initializable
{
    @Service
    private EntityTypeRegistry entityTypeRegistry;
    @Service
    private Repository repository;
    @Uses
    private EntityStateSerializer stateSerializer;
    @Uses
    private EntityTypeSerializer typeSerializer;

    private Set<EntityType> indexedEntityTypes;
    private ValueFactory valueFactory;

    public RdfEntityIndexerMixin()
    {
        indexedEntityTypes = new HashSet<EntityType>();
    }

    public void initialize()
            throws ConstructionException
    {
    }

    public synchronized void notifyChanges(Iterable<EntityState> newStates, Iterable<EntityState> changedStates, Iterable<EntityReference> removedStates)
    {
        try
        {
            boolean abort = abortIfInternalConfigurationEntity(newStates);
            if (abort)
            {
                return;
            }
            final RepositoryConnection connection = repository.getConnection();
            connection.setAutoCommit(false);
            try
            {
                // Update index
                final Set<EntityType> entityTypes = new HashSet<EntityType>();
                for (EntityState entityState : newStates)
                {
                    for (EntityTypeReference entityTypeReference : entityState.entityTypeReferences())
                    {
                        try
                        {
                            EntityType entityType = entityTypeRegistry.getEntityType(entityTypeReference);

                            if (entityType.queryable())
                            {
                                entityTypes.add(entityType);
                                indexEntityState(entityState, connection);
                            }
                        } catch (UnknownEntityTypeException e)
                        {
                            // No EntityType registered - ignore
                            Logger.getLogger(getClass().getName()).warning("Could not get EntityType for " + entityTypeReference.toString());
                        }
                    }
                }

                for (EntityState entityState : changedStates)
                {
                    removeEntityState(entityState.identity(), connection);
                    indexEntityState(entityState, connection);
                }

                for (EntityReference entityId : removedStates)
                {
                    removeEntityState(entityId, connection);
                }

                // Index new types
                for (EntityType entityType : entityTypes)
                {
                    if (!indexedEntityTypes.contains(entityType))
                    {
                        indexEntityType(entityType, connection);
                        indexedEntityTypes.add(entityType);
                    }
                }
            }
            finally
            {
                if (connection != null)
                {
                    connection.commit();
                    connection.close();
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            //TODO What shall we do with the exception?
        }
    }

    private void indexEntityState(final EntityState entityState,
                                  final RepositoryConnection connection)
            throws RepositoryException
    {
        final URI entityURI = stateSerializer.createEntityURI(getValueFactory(), entityState.identity());

        Graph graph = new GraphImpl();
        stateSerializer.serialize(entityState, false, graph);

        connection.add(graph, entityURI);
    }

    private void removeEntityState(final EntityReference identity,
                                   final RepositoryConnection connection)
            throws RepositoryException
    {
        connection.clear(stateSerializer.createEntityURI(getValueFactory(), identity));
    }

    private void indexEntityType(final EntityType entityType,
                                 final RepositoryConnection connection)
            throws RepositoryException
    {
        final URI compositeURI = getValueFactory().createURI(entityType.uri());
        // remove composite type if already present
        connection.clear(compositeURI);

        Iterable<Statement> statements = typeSerializer.serialize(entityType);
        connection.add(statements, compositeURI);

/*
        // first add the composite type as rdfs:Class
        connection.add( compositeURI, RDF.TYPE, RDFS.CLASS, compositeURI );

        // add all subclasses as rdfs:subClassOf
        Iterable<String> mixinTypeNames = entityType.mixinTypes();
        for( String mixinType : mixinTypeNames )
        {
            URI mixinURI = getValueFactory().createURI( ClassUtil.toURI( mixinType ) );
            connection.add( compositeURI, RDFS.SUBCLASSOF, mixinURI, compositeURI );
        }
*/
    }

    private boolean abortIfInternalConfigurationEntity(Iterable<EntityState> newStates)
    {
        for (EntityState newState : newStates)
        {
            for (EntityTypeReference type : newState.entityTypeReferences())
            {
                if (type.type().equals(TypeName.nameOf(NativeConfiguration.class)))
                    return true;
            }
        }

        return false;
    }

    private ValueFactory getValueFactory()
    {
        if (valueFactory == null)
        {
            valueFactory = repository.getValueFactory();
        }

        return valueFactory;
    }
}
