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
package org.qi4j.entity.index.rdf.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStoreListener;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * TODO Add JavaDoc
 */
public class RdfEntityIndexerMixin
    implements EntityStoreListener, Initializable
{
    @Service private Repository repository;
    @Uses private EntityStateSerializer serializer;

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
        serializer.serialize( entityState, false, graph );

        connection.add( graph, entityURI );
    }
//
//    private void indexProperties( final EntityState entityState,
//                                  final RepositoryConnection connection,
//                                  final URI entityURI,
//                                  final EntityType state )
//        throws RepositoryException
//    {
//        final Iterable<PropertyType> properties = state.properties();
//        for( PropertyType property : properties )
//        {
//            if( property.queryable() )
//            {
//                final Object propValue = entityState.getProperty( property.qualifiedName() );
//                if( propValue != null )
//                {
//                    final URI propURI = getValueFactory().createURI( property.uri() );
//                    connection.add( entityURI, propURI, getValueFactory().createLiteral( propValue.toString() ), entityURI );
//                }
//            }
//        }
//    }
//
//    private void indexAssociations( final EntityState entityState,
//                                    final RepositoryConnection connection,
//                                    final URI entityURI,
//                                    final EntityType state )
//        throws RepositoryException
//    {
//        final Iterable<AssociationType> associations = state.associations();
//        for( AssociationType association : associations )
//        {
//            if( association.queryable() )
//            {
//                final QualifiedIdentity assocEntityId = entityState.getAssociation( association.qualifiedName() );
//                if( assocEntityId != null )
//                {
//                    final URI assocURI = getValueFactory().createURI( association.uri() );
//                    if (assocEntityId instanceof QualifierQualifiedIdentity )
//                    {
//                        QualifierQualifiedIdentity arqi = (QualifierQualifiedIdentity) assocEntityId;
//                        BNode qualifier = indexQualifier( connection, arqi, entityURI );
//                        connection.add( entityURI, assocURI, qualifier );
//
//                    } else
//                    {
//                        final URI assocEntityURI = getValueFactory().createURI( assocEntityId.toURI() );
//                        connection.add( entityURI, assocURI, assocEntityURI, entityURI );
//                    }
//                }
//            }
//        }
//    }
//
//    private void indexManyAssociations( final EntityState entityState,
//                                        final RepositoryConnection connection,
//                                        final URI entityURI,
//                                        final EntityType state )
//        throws RepositoryException
//    {
//        final Iterable<ManyAssociationType> manyAssociations = state.manyAssociations();
//        for( ManyAssociationType manyAssociation : manyAssociations )
//        {
//            if( manyAssociation.queryable() )
//            {
//                final String associationQualifiedName = manyAssociation.qualifiedName();
//                final Collection<QualifiedIdentity> assocEntityIds =
//                    entityState.getManyAssociation( associationQualifiedName );
//
//                if( assocEntityIds != null && !assocEntityIds.isEmpty() )
//                {
//    //                indexManyAssociationItemOrginal( connection, entityURI, manyAssociation, assocEntityIds );
//                    indexManyAssociationItem( connection, entityURI, manyAssociation, assocEntityIds );
//                }
//            }
//        }
//    }
//
//    private void indexManyAssociationItemOrginal( final RepositoryConnection connection,
//                                                  final URI entityURI,
//                                                  final ManyAssociationType manyAssociation,
//                                                  final Collection<QualifiedIdentity> assocEntityIds )
//        throws RepositoryException
//    {
//        final URI assocURI = getValueFactory().createURI( manyAssociation.uri() );
//        BNode prevAssocEntityBNode = null;
//
//        for( QualifiedIdentity assocEntityId : assocEntityIds )
//        {
//            final URI assocEntityURI = getValueFactory().createURI( assocEntityId.toURI() );
//            final BNode assocEntityBNode = getValueFactory().createBNode();
//            if( prevAssocEntityBNode == null )
//            {
//                connection.add( entityURI, assocURI, assocEntityBNode, entityURI );
//            }
//            else
//            {
//                connection.add( prevAssocEntityBNode, RDF.REST, assocEntityBNode, entityURI );
//            }
//            connection.add( assocEntityBNode, RDF.FIRST, assocEntityURI, entityURI );
//            prevAssocEntityBNode = assocEntityBNode;
//        }
//    }

//    private void indexManyAssociationItem( final RepositoryConnection connection,
//                                           final URI entityURI,
//                                           final ManyAssociationType manyAssociation,
//                                           final Collection<QualifiedIdentity> assocEntityIds )
//        throws RepositoryException
//    {
//        final URI assocURI = getValueFactory().createURI( manyAssociation.uri() );
//
//        final ManyAssociationType.ManyAssociationTypeEnum typeEnum = manyAssociation.associationType();
//        final BNode collectionNode = getValueFactory().createBNode();
//
//        Statement collectionTypeStatement;
//        if( typeEnum == LIST )
//        {
//            collectionTypeStatement = getValueFactory().createStatement( collectionNode, RDF.TYPE, RDF.LIST );
//        }
//        else
//        {
//            collectionTypeStatement = getValueFactory().createStatement( collectionNode, RDF.TYPE, RDF.BAG );
//        }
//        connection.add( entityURI, assocURI, collectionNode, entityURI );
//        connection.add( collectionTypeStatement, entityURI );
//
//        for( QualifiedIdentity assocEntityId : assocEntityIds )
//        {
//            if (assocEntityId instanceof QualifierQualifiedIdentity )
//            {
//                QualifierQualifiedIdentity arqi = (QualifierQualifiedIdentity) assocEntityId;
//                BNode qualifier = indexQualifier( connection, arqi, entityURI );
//                connection.add( collectionNode, RDF.LI, qualifier, entityURI );
//            } else
//            {
//                final URI assocEntityURI = getValueFactory().createURI( assocEntityId.toURI() );
//                connection.add( collectionNode, RDF.LI, assocEntityURI, entityURI );
//            }
//        }
//    }

//    private BNode indexQualifier( RepositoryConnection connection, QualifierQualifiedIdentity arqi, URI entityURI )
//        throws RepositoryException
//    {
//        final BNode qualifier = getValueFactory().createBNode();
//        connection.add( qualifier, RDF.TYPE, Qi4jRdf.TYPE_QUALIFIER, entityURI );
//
//        final URI assocEntityURI = getValueFactory().createURI( arqi.toURI() );
//        connection.add( qualifier, Qi4jEntity.ENTITY, assocEntityURI, entityURI );
//        final URI assocRoleURI = getValueFactory().createURI( arqi.role().toURI() );
//        connection.add( qualifier, Qi4jEntity.QUALIFIER, assocRoleURI, entityURI );
//        return qualifier;
//    }

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

        Iterable<Statement> statements = serializer.serialize( entityType );
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
