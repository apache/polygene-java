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
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.MixinTypeModel;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.AssociationBinding;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.query.EntityIndexer;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public class RDFEntityIndexerMixin
    implements EntityIndexer
{

    @ThisCompositeAs RDFQueryContext queryContext;

    public void index( final Iterable<EntityState> newStates,
                       final Iterable<EntityState> changedStates,
                       final Iterable<EntityId> removedStates,
                       final ModuleBinding moduleBinding )
    {
        System.out.println( "New: " + newStates );
        System.out.println( "Updated: " + changedStates );
        System.out.println( "Removed: " + removedStates );

        try
        {
            final RepositoryConnection connection = queryContext.getRepository().getConnection();
            final ValueFactory valueFactory = queryContext.getRepository().getValueFactory();
            try
            {
                final Set<String> entityTypes = new HashSet<String>();
                for( EntityState entityState : newStates )
                {
                    entityTypes.add( entityState.getIdentity().getCompositeType() );
                    indexEntityState( entityState, moduleBinding, connection, valueFactory );
                }
                for( EntityState entityState : changedStates )
                {
                    entityTypes.add( entityState.getIdentity().getCompositeType() );
                    removeEntityState( entityState.getIdentity(), moduleBinding, connection, valueFactory );
                    indexEntityState( entityState, moduleBinding, connection, valueFactory );
                }
                for( EntityId entityId : removedStates )
                {
                    removeEntityState( entityId, moduleBinding, connection, valueFactory );
                }
                for( String entityType : entityTypes )
                {
                    indexEntityType( entityType, moduleBinding, connection, valueFactory );
                }
            }
            finally
            {
                if( connection != null )
                {
                    connection.commit();
                }
            }
        }
        catch( RepositoryException e )
        {
            e.printStackTrace();
        }
    }

    private static void indexEntityState( final EntityState entityState,
                                          final ModuleBinding moduleBinding,
                                          final RepositoryConnection connection,
                                          final ValueFactory valueFactory )
        throws RepositoryException
    {
        final Class compositeClass = moduleBinding.lookupClass( entityState.getIdentity().getCompositeType() );
        final CompositeBinding compositeBinding = moduleBinding.getCompositeBinding( compositeClass );
        final CompositeModel compositeModel = compositeBinding.getCompositeResolution().getCompositeModel();
        final URI compositeURI = valueFactory.createURI( compositeModel.toURI() );
        final URI entityURI = valueFactory.createURI( compositeModel.toURI()
                                                      + "/" + entityState.getIdentity().getIdentity() );
        connection.add( entityURI, RDF.TYPE, compositeURI );
        // index properties
        for( String propName : entityState.getPropertyNames() )
        {
            final Object propValue = entityState.getProperty( propName );
            if( propValue != null )
            {
                final PropertyBinding propBinding = compositeBinding.getPropertyBinding( propName );
                final PropertyModel propModel = propBinding.getPropertyResolution().getPropertyModel();
                final URI propURI = valueFactory.createURI( propModel.toURI() );
                connection.add( entityURI, propURI, valueFactory.createLiteral( propValue.toString() ) );
            }
        }
        // index associations
        for( String assocName : entityState.getAssociationNames() )
        {
            final EntityId assocEntityId = entityState.getAssociation( assocName );
            if( assocEntityId != null )
            {
                final AssociationBinding assocBinding = compositeBinding.getAssociationBinding( assocName );
                final AssociationModel assocModel = assocBinding.getAssociationResolution().getAssociationModel();
                final URI assocURI = valueFactory.createURI( assocModel.toURI() );

                final Class assocCompositeClass = moduleBinding.lookupClass( assocEntityId.getCompositeType() );
                final CompositeBinding assocCompositeBinding = moduleBinding.getCompositeBinding( assocCompositeClass );
                final CompositeModel assocCompositeModel = assocCompositeBinding.getCompositeResolution().getCompositeModel();
                final URI assocEntityURI = valueFactory.createURI( assocCompositeModel.toURI()
                                                                   + "/" + assocEntityId.getIdentity() );
                connection.add( entityURI, assocURI, assocEntityURI );
            }
        }
        // index many associations
        for( String qualifiedName : entityState.getManyAssociationNames() )
        {
            final Collection<EntityId> assocEntityIds = entityState.getManyAssociation( qualifiedName );
            if( assocEntityIds != null )
            {
                final AssociationBinding assocBinding = compositeBinding.getAssociationBinding( qualifiedName );
                final AssociationModel assocModel = assocBinding.getAssociationResolution().getAssociationModel();
                final URI assocURI = valueFactory.createURI( assocModel.toURI() );
                
                for( EntityId assocEntityId : assocEntityIds )
                {
                    final Class assocCompositeClass = moduleBinding.lookupClass( assocEntityId.getCompositeType() );
                    final CompositeBinding assocCompositeBinding = moduleBinding.getCompositeBinding( assocCompositeClass );
                    final CompositeModel assocCompositeModel = assocCompositeBinding.getCompositeResolution().getCompositeModel();
                    final URI assocEntityURI = valueFactory.createURI( assocCompositeModel.toURI()
                                                                       + "/" + assocEntityId.getIdentity() );
                }
            }
        }
    }

    private static void removeEntityState( final EntityId entityId,
                                           final ModuleBinding moduleBinding,
                                           final RepositoryConnection connection,
                                           final ValueFactory valueFactory )
        throws RepositoryException
    {
        final Class compositeClass = moduleBinding.lookupClass( entityId.getCompositeType() );
        final CompositeBinding compositeBinding = moduleBinding.getCompositeBinding( compositeClass );
        final CompositeModel compositeModel = compositeBinding.getCompositeResolution().getCompositeModel();
        final URI entityURI = valueFactory.createURI( compositeModel.toURI() + "/" + entityId.getIdentity() );
        connection.remove( entityURI, null, null );
    }

    private static void indexEntityType( final String entityType,
                                         final ModuleBinding moduleBinding,
                                         final RepositoryConnection connection,
                                         final ValueFactory valueFactory )
        throws RepositoryException
    {

        final Class compositeClass = moduleBinding.lookupClass( entityType );
        final CompositeBinding compositeBinding = moduleBinding.getCompositeBinding( compositeClass );
        final CompositeModel compositeModel = compositeBinding.getCompositeResolution().getCompositeModel();
        final URI compositeURI = valueFactory.createURI( compositeModel.toURI() );
        // remove composite type if already present
        connection.remove( compositeURI, null, null );
        // first add the composite type as rdfs:Class
        connection.add( compositeURI, RDF.TYPE, RDFS.CLASS );
        // add all subclasses as rdfs:subClassOf
        for( MixinTypeModel mixinTypeModel : compositeModel.getMixinTypeModels() )
        {
            connection.add( compositeURI, RDFS.SUBCLASSOF, valueFactory.createURI( mixinTypeModel.toURI() ) );
        }
    }


}
