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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStoreListener;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * JAVADOC Add JavaDoc
 */
public class RdfEntityIndexerMixin
    implements EntityStoreListener, Initializable
{
    @Service private Repository repository;
    @Uses private EntityStateSerializer stateSerializer;
    @Uses private EntityTypeSerializer typeSerializer;

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

    public synchronized void notifyChanges( Iterable<EntityState> newStates, Iterable<EntityState> changedStates, Iterable<QualifiedIdentity> removedStates )
    {
        try
        {
            boolean abort = abortIfInternalConfigurationEntity( newStates );
            if( abort )
            {
                return;
            }
            final RepositoryConnection connection = repository.getConnection();
            connection.setAutoCommit( false );
            try
            {
                // Update index
                final Set<EntityType> entityTypes = new HashSet<EntityType>();
                for( EntityState entityState : newStates )
                {
                    if( entityState.entityType().queryable() )
                    {
                        entityTypes.add( entityState.entityType() );
                        indexEntityState( entityState, connection );
                    }
                }

                for( EntityState entityState : changedStates )
                {
                    removeEntityState( entityState.qualifiedIdentity(), connection );
                    if( entityState.entityType().queryable() )
                    {
                        indexEntityState( entityState, connection );
                    }
                }

                for( QualifiedIdentity entityId : removedStates )
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
        final QualifiedIdentity qualifiedIdentity = entityState.qualifiedIdentity();
        final URI entityURI = getValueFactory().createURI( qualifiedIdentity.toURI() );

        Graph graph = new GraphImpl();
        stateSerializer.serialize( entityState, false, graph );

        connection.add( graph, entityURI );
    }

    private void removeEntityState( final QualifiedIdentity qualifiedIdentity,
                                    final RepositoryConnection connection )
        throws RepositoryException
    {
        final URI entityURI = getValueFactory().createURI( qualifiedIdentity.toURI() );
        connection.clear( entityURI );
    }

    private void indexEntityType( final EntityType entityType,
                                  final RepositoryConnection connection )
        throws RepositoryException
    {
        final URI compositeURI = getValueFactory().createURI( entityType.toURI() );
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
        Iterator<EntityState> entityStateIterator = newStates.iterator();
        if( entityStateIterator.hasNext() )
        {
            String compositeTypeName = entityStateIterator.next().qualifiedIdentity().type();
            if( NativeConfiguration.class.getName().equals( compositeTypeName ) )
            {
                return true;
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
