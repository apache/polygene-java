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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.entity.EntityComposite;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.serialization.SerializedState;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public class RDFIndexerMixin
    implements Indexer
{

    @ThisCompositeAs RDFIndexerState state;

    public void index( final Map<EntityId, SerializedState> newEntities,
                       final Map<EntityId, SerializedState> updatedEntities,
                       final Iterable<EntityId> removedEntities )
    {
        System.out.println( "New: " + newEntities );
        System.out.println( "Updated: " + updatedEntities );
        System.out.println( "Removed: " + removedEntities );

        try
        {
            final RepositoryConnection connection = state.getRepository().getConnection();
            final ValueFactory valueFactory = state.getRepository().getValueFactory();
            try
            {
                for( Map.Entry<EntityId, SerializedState> entry : newEntities.entrySet() )
                {
                    indexCompositeType( entry.getKey().getCompositeType(), connection, valueFactory );
                    final URI entityTypeUri = valueFactory.createURI(
                        normalizeInnerClass(
                            "urn:" + entry.getKey().getCompositeType().getName()
                        )
                    );
                    final URI entityUri = valueFactory.createURI(
                        normalizeInnerClass(
                            "urn:" + entry.getKey().getCompositeType().getName() + "/" + entry.getKey().getIdentity()
                        )
                    );
                    connection.add( entityUri, RDF.TYPE, entityTypeUri );

                    // properties
                    for( Map.Entry<String, Serializable> property : entry.getValue().getProperties().entrySet() )
                    {
                        if( property.getValue() != null )
                        {
                            final URI propertyType = valueFactory.createURI(
                                normalizeInnerClass(
                                    "urn:" + property.getKey().replace( ":", "/" )
                                )
                            );
                            final Literal propertyValue = valueFactory.createLiteral( property.getValue().toString() );
                            connection.add( entityUri, propertyType, propertyValue );
                        }
                    }
                    // association
                    for( Map.Entry<String, EntityId> assoc : entry.getValue().getAssociations().entrySet() )
                    {
                        final URI assocType = valueFactory.createURI(
                            normalizeInnerClass(
                                "urn:" + assoc.getKey().replace( ":", "/" )
                            )
                        );
                        final URI assocRef = valueFactory.createURI(
                            normalizeInnerClass(
                                "urn:" + assoc.getValue().getCompositeType().getName() + "/" + assoc.getValue().getIdentity()
                            )
                        );
                        connection.add( entityUri, assocType, assocRef );
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
