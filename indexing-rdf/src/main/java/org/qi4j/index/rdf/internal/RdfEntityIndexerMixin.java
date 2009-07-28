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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.EntityTypeRegistry;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.unitofwork.UnitOfWorkEventListener;
import org.qi4j.spi.unitofwork.event.AddEntityTypeEvent;
import org.qi4j.spi.unitofwork.event.EntityEvent;
import org.qi4j.spi.unitofwork.event.RemoveEntityEvent;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvent;

/**
 * JAVADOC Add JavaDoc
 */
public class RdfEntityIndexerMixin
    implements UnitOfWorkEventListener, Initializable
{
    @Service
    private EntityStore entityStore;

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

    public void notifyEvents( Iterable<UnitOfWorkEvent> events )
    {
        try
        {
            final RepositoryConnection connection = repository.getConnection();
            connection.setAutoCommit( false );
            try
            {
                // Figure out what to update
                Set<EntityReference> updatedEntities = new HashSet<EntityReference>();
                Set<EntityReference> removedEntities = new HashSet<EntityReference>();
                final Set<EntityType> entityTypes = new HashSet<EntityType>();
                for( UnitOfWorkEvent event : events )
                {
                    if( event instanceof RemoveEntityEvent )
                    {
                        RemoveEntityEvent removeEvent = (RemoveEntityEvent) event;
                        removedEntities.add( removeEvent.identity() );
                    }
                    else if( event instanceof AddEntityTypeEvent )
                    {
                        AddEntityTypeEvent addEvent = (AddEntityTypeEvent) event;
                        try
                        {
                            EntityType entityType = entityTypeRegistry.getEntityType( addEvent.entityType() );

                            if( entityType.queryable() )
                            {
                                entityTypes.add( entityType );
                            }
                        }
                        catch( UnknownEntityTypeException e )
                        {
                            // Skip this one
                        }
                    }
                    else if( event instanceof EntityEvent )
                    {
                        EntityEvent entityEvent = (EntityEvent) event;
                        updatedEntities.add( entityEvent.identity() );
                    }
                }

                // Update entities
                EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( UsecaseBuilder.newUsecase( "Update index" ), new MetaInfo() );

                for( EntityReference entityReference : updatedEntities )
                {
                    EntityState entityState = null;
                    try
                    {
                        entityState = uow.getEntityState( entityReference );
                    }
                    catch( EntityNotFoundException e )
                    {
                        // Skip this one
                        continue;
                    }

                    for( EntityTypeReference entityTypeReference : entityState.entityTypeReferences() )
                    {
                        try
                        {
                            removeEntityState( entityState.identity(), connection ); // TODO Fix so that new entities are not removed first
                            indexEntityState( entityState, connection );
                        }
                        catch( UnknownEntityTypeException e )
                        {
                            // No EntityType registered - ignore
                            Logger.getLogger( getClass().getName() ).warning( "Could not get EntityType for " + entityTypeReference.toString() );
                        }
                    }
                }

                for( EntityReference entityId : removedEntities )
                {
                    removeEntityState( entityId, connection );
                }

                // Index new types
                for( EntityType entityType : entityTypes )
                {
                    if( !indexedEntityTypes.contains( entityType ) )
                    {
                        indexEntityType( entityType, connection );
                        indexedEntityTypes.add( entityType );
                    }
                }
            }
            finally
            {
                if( connection != null )
                {
                    connection.commit();
                    connection.close();
                }
            }
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            //TODO What shall we do with the exception?
        }
    }

    private void indexEntityState( final EntityState entityState,
                                   final RepositoryConnection connection )
        throws RepositoryException
    {
        final URI entityURI = stateSerializer.createEntityURI( getValueFactory(), entityState.identity() );

        Graph graph = new GraphImpl();
        stateSerializer.serialize( entityState, false, graph );

        connection.add( graph, entityURI );
    }

    private void removeEntityState( final EntityReference identity,
                                    final RepositoryConnection connection )
        throws RepositoryException
    {
        connection.clear( stateSerializer.createEntityURI( getValueFactory(), identity ) );
    }

    private void indexEntityType( final EntityType entityType,
                                  final RepositoryConnection connection )
        throws RepositoryException
    {
        final URI compositeURI = getValueFactory().createURI( entityType.uri() );
        // remove composite type if already present
        connection.clear( compositeURI );

        Iterable<Statement> statements = typeSerializer.serialize( entityType );
        connection.add( statements, compositeURI );

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

    private boolean abortIfInternalConfigurationEntity( Iterable<EntityState> newStates )
    {
        for( EntityState newState : newStates )
        {
            for( EntityTypeReference type : newState.entityTypeReferences() )
            {
                if( type.type().equals( TypeName.nameOf( NativeConfiguration.class ) ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    private ValueFactory getValueFactory()
    {
        if( valueFactory == null )
        {
            valueFactory = repository.getValueFactory();
        }

        return valueFactory;
    }
}
