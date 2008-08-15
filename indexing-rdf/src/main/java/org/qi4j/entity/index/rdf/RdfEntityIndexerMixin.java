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
package org.qi4j.entity.index.rdf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.composite.Composite;
import org.qi4j.composite.ConstructionException;
import org.qi4j.composite.Initializable;
import org.qi4j.injection.scope.Service;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.spi.entity.AssociationType;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationType;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityIndexer;
import org.qi4j.util.ClassUtil;

/**
 * TODO Add JavaDoc
 */
public class RdfEntityIndexerMixin
    implements EntityIndexer, Initializable
{
    private @Service Repository repository;

    private Set<EntityType> indexedEntityTypes = new HashSet<EntityType>();
    private ValueFactory valueFactory;

    public void initialize() throws ConstructionException
    {
        valueFactory = repository.getValueFactory();
    }

    public void index( final Iterable<EntityState> newStates,
                       final Iterable<EntityState> changedStates,
                       final Iterable<QualifiedIdentity> removedStates )
    {
        try
        {
            boolean abort = abortIfInternalConfigurationEntity( newStates );
            if( abort )
            {
                return;
            }
            final RepositoryConnection connection = repository.getConnection();
            try
            {
                // Update index
                final Set<EntityType> entityTypes = new HashSet<EntityType>();
                for( EntityState entityState : newStates )
                {
                    entityTypes.add( entityState.entityType() );
                    indexEntityState( entityState, connection );
                }
                for( EntityState entityState : changedStates )
                {
                    removeEntityState( entityState.qualifiedIdentity(), connection );
                    indexEntityState( entityState, connection );
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
        final URI compositeURI = valueFactory.createURI( ClassUtil.toURI( entityState.qualifiedIdentity().type() ) );
        final URI compositeClassURI = valueFactory.createURI( ClassUtil.toURI( Composite.class ) + ":entityType" );
        final URI entityURI = valueFactory.createURI( entityState.qualifiedIdentity().toURI() );
        connection.add( entityURI, RDF.TYPE, compositeURI, entityURI );
        connection.add( entityURI, compositeClassURI, valueFactory.createLiteral( entityState.qualifiedIdentity().type() ), entityURI );

        // index properties
        final EntityType state = entityState.entityType();
        for( PropertyType property : state.properties() )
        {
            final Object propValue = entityState.getProperty( property.qualifiedName() );
            if( propValue != null )
            {
                final URI propURI = valueFactory.createURI( property.toURI() );
                connection.add( entityURI, propURI, valueFactory.createLiteral( propValue.toString() ), entityURI );
            }
        }

        // index associations
        for( AssociationType association : state.associations() )
        {
            final QualifiedIdentity assocEntityId = entityState.getAssociation( association.qualifiedName() );
            if( assocEntityId != null )
            {
                final URI assocURI = valueFactory.createURI( association.toURI() );

                final URI assocEntityURI = valueFactory.createURI( assocEntityId.toURI() );
                connection.add( entityURI, assocURI, assocEntityURI, entityURI );
            }
        }

        // index many associations
        for( ManyAssociationType manyAssociation : state.manyAssociations() )
        {
            final Collection<QualifiedIdentity> assocEntityIds = entityState.getManyAssociation( manyAssociation.qualifiedName() );
            if( assocEntityIds != null )
            {
                final URI assocURI = valueFactory.createURI( manyAssociation.toURI() );
                BNode prevAssocEntityBNode = null;

                for( QualifiedIdentity assocEntityId : assocEntityIds )
                {
                    final URI assocEntityURI = valueFactory.createURI( assocEntityId.toURI() );
                    final BNode assocEntityBNode = valueFactory.createBNode();
                    if( prevAssocEntityBNode == null )
                    {
                        connection.add( entityURI, assocURI, assocEntityBNode, entityURI );
                    }
                    else
                    {
                        connection.add( prevAssocEntityBNode, RDF.REST, assocEntityBNode, entityURI );
                    }
                    connection.add( assocEntityBNode, RDF.FIRST, assocEntityURI, entityURI );
                    prevAssocEntityBNode = assocEntityBNode;
                }
            }
        }
    }

    private void removeEntityState( final QualifiedIdentity qualifiedIdentity,
                                    final RepositoryConnection connection )
        throws RepositoryException
    {
        final URI entityURI = valueFactory.createURI( qualifiedIdentity.toURI() );
        connection.clear( entityURI );
    }

    private void indexEntityType( final EntityType entityType,
                                  final RepositoryConnection connection )
        throws RepositoryException
    {
        final URI compositeURI = valueFactory.createURI( entityType.toURI() );
        // remove composite type if already present
        connection.clear( compositeURI );
        // first add the composite type as rdfs:Class
        connection.add( compositeURI, RDF.TYPE, RDFS.CLASS, compositeURI );

        // add all subclasses as rdfs:subClassOf
        Iterable<String> mixinTypeNames = entityType.mixinTypes();
        for( String mixinType : mixinTypeNames )
        {
            connection.add( compositeURI, RDFS.SUBCLASSOF, valueFactory.createURI( ClassUtil.toURI( mixinType ) ), compositeURI );
        }
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

}
