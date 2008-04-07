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
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.entity.EntityComposite;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.spi.query.EntityIndexer;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public class RDFEntityIndexerMixin
    implements EntityIndexer
{

    @ThisCompositeAs RDFQueryContext state;

    public void index( Iterable<EntityState> newStates, Iterable<EntityState> changedStates, Iterable<EntityId> removedStates, ModuleBinding moduleBinding )
    {
        System.out.println( "New: " + newStates );
        System.out.println( "Updated: " + changedStates );
        System.out.println( "Removed: " + removedStates );

        try
        {
            final RepositoryConnection connection = state.getRepository().getConnection();
            final ValueFactory valueFactory = state.getRepository().getValueFactory();
            try
            {
                for( EntityState entry : newStates )
                {
                    Class compositeType = moduleBinding.lookupClass( entry.getIdentity().getCompositeType() );
                    indexCompositeType( compositeType, connection, valueFactory );
                    final URI entityTypeUri = valueFactory.createURI(
                        normalizeInnerClass(
                            "urn:" + entry.getIdentity().getCompositeType()
                        )
                    );
                    final URI entityUri = valueFactory.createURI(
                        normalizeInnerClass(
                            "urn:" + entry.getIdentity().getCompositeType() + "/" + entry.getIdentity().getIdentity()
                        )
                    );
                    connection.add( entityUri, RDF.TYPE, entityTypeUri );

                    // properties
                    for( String property : entry.getPropertyNames() )
                    {
                        Object propertyValue = entry.getProperty( property );
                        if( propertyValue != null )
                        {
                            final URI propertyType = valueFactory.createURI(
                                normalizeInnerClass(
                                    "urn:" + property.replace( ":", "/" )
                                )
                            );
                            final Literal propertyLiteral = valueFactory.createLiteral( propertyValue.toString() );
                            connection.add( entityUri, propertyType, propertyLiteral );
                        }
                    }
                    // association
                    for( String assoc : entry.getAssociationNames() )
                    {
                        EntityId entityId = entry.getAssociation( assoc );
                        if( entityId != null )
                        {
                            final URI assocType = valueFactory.createURI(
                                normalizeInnerClass(
                                    "urn:" + assoc.replace( ":", "/" )
                                )
                            );
                            final URI assocRef = valueFactory.createURI(
                                normalizeInnerClass(
                                    "urn:" + entityId.getCompositeType() + "/" + entityId.getIdentity()
                                )
                            );
                            connection.add( entityUri, assocType, assocRef );
                        }
                    }
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

    private static void indexCompositeType( final Class<? extends EntityComposite> compositeType,
                                            final RepositoryConnection connection,
                                            final ValueFactory valueFactory )
        throws RepositoryException
    {
        final URI compositeTypeURI = valueFactory.createURI( "urn:" + compositeType.getName() );
        connection.add( compositeTypeURI, RDF.TYPE, RDFS.CLASS );
        for( Class subType : extractSubTypes( compositeType ) )
        {
            connection.add( compositeTypeURI, RDFS.SUBCLASSOF, valueFactory.createURI( "urn:" + subType.getName() ) );
        }
    }

    private static Collection<Class> extractSubTypes( final Class clazz )
    {
        final Collection<Class> subTypes = new HashSet<Class>();
        for( Class subType : clazz.getInterfaces() )
        {
            subTypes.add( subType );
            subTypes.addAll( extractSubTypes( subType ) );
        }
        return subTypes;
    }

    private String normalizeInnerClass( String className )
    {
        return className.replace( '$', '.' );
    }

}
