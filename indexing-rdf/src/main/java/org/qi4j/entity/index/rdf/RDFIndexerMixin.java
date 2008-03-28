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
import java.util.Map;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.composite.scope.ThisCompositeAs;
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
                    final URI entityTypeUri = valueFactory.createURI(
                        normalizeInnerClass(
                            "urn:qi4j/" + entry.getKey().getCompositeType().getName()
                        )
                    );
                    final URI entityUri = valueFactory.createURI(
                        normalizeInnerClass(
                            "urn:" + entry.getKey().getCompositeType().getName() + "/" + entry.getKey().getIdentity()
                        )
                    );
                    connection.add( entityUri, RDF.TYPE, entityTypeUri );

                    // properties
                    // map between property type and associated blank node
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

    private String normalizeInnerClass( String className )
    {
        return className.replace( '$', '.' );
    }

}
