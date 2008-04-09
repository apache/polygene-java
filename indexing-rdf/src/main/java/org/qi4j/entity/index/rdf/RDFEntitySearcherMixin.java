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
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.spi.query.EntitySearcher;
import org.qi4j.spi.query.SearchException;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public class RDFEntitySearcherMixin
    implements EntitySearcher
{

    @ThisCompositeAs RDFQueryContext queryContext;

    public Iterable<String> find( final Class entityType,
                                  final BooleanExpression whereClause )
        throws SearchException
    {
        final Collection<String> entities = new HashSet<String>();
        try
        {
            final RepositoryConnection connection = queryContext.getRepository().getConnection();
            // TODO shall we support different implementation as SERQL?
            final RDFQueryParser parser = new SPARQLRDFQueryParser();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(
                parser.getQueryLanguage(),
                parser.getQuery( entityType, whereClause )
            );
            final TupleQueryResult result = tupleQuery.evaluate();
            try
            {
                while( result.hasNext() )
                {
                    final BindingSet bindingSet = result.next();
                    final Value identifier = bindingSet.getValue( "identity" );
                    //TODO Shall we throw an exception if there is no binding for identifier = query parser is not right
                    if( identifier != null )
                    {
                        final String value = identifier.stringValue();
                        if( value != null )
                        {
                            System.out.println( bindingSet.getValue( "entity" ).stringValue() + " -> " + value );
                            entities.add( value );
                        }
                    }
                }
            }
            finally
            {
                result.close();
            }
        }
        catch( RepositoryException e )
        {
            throw new SearchException( e );
        }
        catch( MalformedQueryException e )
        {
            throw new SearchException( e );
        }
        catch( QueryEvaluationException e )
        {
            throw new SearchException( e );
        }
        return entities;
    }

}