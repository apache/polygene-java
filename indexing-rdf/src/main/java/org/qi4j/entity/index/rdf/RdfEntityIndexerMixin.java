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
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.composite.Composite;
import org.qi4j.entity.index.rdf.natiive.NativeRdfConfiguration;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.MixinTypeModel;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.query.EntityIndexer;
import org.qi4j.structure.Module;

/**
 * TODO Add JavaDoc
 */
public class RdfEntityIndexerMixin
    implements EntityIndexer
{
    @Structure private Qi4jSPI spi;

    @This RdfQueryContext queryContext;

    public void index( final Iterable<EntityState> newStates,
                       final Iterable<EntityState> changedStates,
                       final Iterable<QualifiedIdentity> removedStates,
                       final Module moduleBinding )
    {
        try
        {
            boolean abort = abortIfInternalConfigurationEntity( newStates );
            if( abort )
            {
                return;
            }
            final RepositoryConnection connection = queryContext.getRepository().getConnection();
            final ValueFactory valueFactory = queryContext.getRepository().getValueFactory();
            try
            {
                final Set<String> entityTypes = new HashSet<String>();
                for( EntityState entityState : newStates )
                {
                    entityTypes.add( entityState.getIdentity().type() );
                    indexEntityState( entityState, moduleBinding, connection, valueFactory );
                }
                for( EntityState entityState : changedStates )
                {
                    entityTypes.add( entityState.getIdentity().type() );
                    removeEntityState( entityState.getIdentity(), moduleBinding, connection, valueFactory );
                    indexEntityState( entityState, moduleBinding, connection, valueFactory );
                }
                for( QualifiedIdentity entityId : removedStates )
                {
                    removeEntityState( entityId, moduleBinding, connection, valueFactory );
                }
                for( String entityType : entityTypes )
                {
                    indexEntityType( entityType, moduleBinding, connection, valueFactory );
                }
            }
            catch( ClassNotFoundException e )
            {
                e.printStackTrace();
                //TODO What shall we do with the exception?
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
            //TODO What shall we do with the exception?
        }
    }

    private void indexEntityState( final EntityState entityState,
                                   final Module module,
                                   final RepositoryConnection connection,
                                   final ValueFactory valueFactory )
        throws RepositoryException, ClassNotFoundException
    {
        final Class compositeClass = module.classLoader().loadClass( entityState.getIdentity().type() );
        final CompositeDescriptor compositeDescriptor = spi.getCompositeDescriptor( compositeClass, module );
        final URI compositeURI = valueFactory.createURI( compositeDescriptor.toURI() );
        final URI compositeClassURI = valueFactory.createURI( MixinTypeModel.toURI( Composite.class ) + ":entityType" );
        final URI entityURI = valueFactory.createURI( compositeDescriptor.toURI()
                                                      + "/" + entityState.getIdentity().identity() );
        connection.add( entityURI, RDF.TYPE, compositeURI );
        connection.add( entityURI, compositeClassURI, valueFactory.createLiteral( compositeClass.getName() ) );
        // index properties
        for( String propName : entityState.getPropertyNames() )
        {
            final Object propValue = entityState.getProperty( propName );
            if( propValue != null )
            {
                final PropertyDescriptor propertyDescriptor = compositeDescriptor.state().getPropertyByName( propName );
                final URI propURI = valueFactory.createURI( propertyDescriptor.toURI() );
                connection.add( entityURI, propURI, valueFactory.createLiteral( propValue.toString() ) );
            }
        }
        // index associations
        for( String assocName : entityState.getAssociationNames() )
        {
            final QualifiedIdentity assocEntityId = entityState.getAssociation( assocName );
            if( assocEntityId != null )
            {
                final AssociationDescriptor associationDescriptor = compositeDescriptor.state().getAssociationByName( assocName );
                final URI assocURI = valueFactory.createURI( associationDescriptor.toURI() );

                final Class assocCompositeClass = module.classLoader().loadClass( assocEntityId.type() );
                final CompositeDescriptor descriptor = spi.getCompositeDescriptor( assocCompositeClass, module );
                final URI assocEntityURI = valueFactory.createURI( descriptor.toURI()
                                                                   + "/" + assocEntityId.identity() );
                connection.add( entityURI, assocURI, assocEntityURI );
            }
        }
        // index many associations
        for( String qualifiedName : entityState.getManyAssociationNames() )
        {
            final Collection<QualifiedIdentity> assocEntityIds = entityState.getManyAssociation( qualifiedName );
            if( assocEntityIds != null )
            {
                final AssociationDescriptor associationDescriptor = compositeDescriptor.state().getAssociationByName( qualifiedName );
                final URI assocURI = valueFactory.createURI( associationDescriptor.toURI() );
                BNode prevAssocEntityBNode = null;

                for( QualifiedIdentity assocEntityId : assocEntityIds )
                {
                    final Class assocCompositeClass = module.classLoader().loadClass( assocEntityId.type() );
                    final CompositeDescriptor descriptor = spi.getCompositeDescriptor( assocCompositeClass, module );
                    final URI assocEntityURI = valueFactory.createURI( descriptor.toURI()
                                                                       + "/" + assocEntityId.identity() );
                    final BNode assocEntityBNode = valueFactory.createBNode();
                    if( prevAssocEntityBNode == null )
                    {
                        connection.add( entityURI, assocURI, assocEntityBNode );
                    }
                    else
                    {
                        connection.add( prevAssocEntityBNode, RDF.REST, assocEntityBNode );
                    }
                    connection.add( assocEntityBNode, RDF.FIRST, assocEntityURI );
                    prevAssocEntityBNode = assocEntityBNode;
                }
            }
        }
    }

    private void removeEntityState( final QualifiedIdentity entityId,
                                    final Module module,
                                    final RepositoryConnection connection,
                                    final ValueFactory valueFactory )
        throws RepositoryException, ClassNotFoundException
    {
        final Class compositeClass = module.classLoader().loadClass( entityId.type() );
        final CompositeDescriptor compositeDescriptor = spi.getCompositeDescriptor( compositeClass, module );
        final URI entityURI = valueFactory.createURI( compositeDescriptor.toURI() + "/" + entityId.identity() );
        connection.remove( entityURI, null, null );
    }

    private void indexEntityType( final String entityType,
                                  final Module module,
                                  final RepositoryConnection connection,
                                  final ValueFactory valueFactory )
        throws RepositoryException, ClassNotFoundException
    {

        final Class compositeClass = module.classLoader().loadClass( entityType );
        final CompositeDescriptor compositeDescriptor = spi.getCompositeDescriptor( compositeClass, module );
        final URI compositeURI = valueFactory.createURI( compositeDescriptor.toURI() );
        // remove composite type if already present
        connection.remove( compositeURI, null, null );
        // first add the composite type as rdfs:Class
        connection.add( compositeURI, RDF.TYPE, RDFS.CLASS );
        // add all subclasses as rdfs:subClassOf
/* TODO Fix this!
        for( MixinTypeModel mixinTypeModel : compositeDescriptor.type().getMixinTypeModels() )
        {
            connection.add( compositeURI, RDFS.SUBCLASSOF, valueFactory.createURI( mixinTypeModel.toURI() ) );
        }
*/
    }

    private boolean abortIfInternalConfigurationEntity( Iterable<EntityState> newStates )
    {
        Iterator<EntityState> entityStateIterator = newStates.iterator();
        if( entityStateIterator.hasNext() )
        {
            String compositeTypeName = entityStateIterator.next().getIdentity().type();
            if( NativeRdfConfiguration.class.getName().equals( compositeTypeName ) )
            {
                return true;
            }
        }
        return false;
    }


}
