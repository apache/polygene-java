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

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.entity.index.rdf.callback.CollectingQualifiedIdentityResultCallback;
import org.qi4j.entity.index.rdf.callback.QualifiedIdentityResultCallback;
import org.qi4j.entity.index.rdf.callback.SingleQualifiedIdentityResultCallback;
import org.qi4j.injection.scope.Service;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.util.ClassUtil;

/**
 * TODO Add JavaDoc
 * TODO shall we support different implementation as SERQL?
 */
public class RdfEntityFinderMixin
    implements EntityFinder
{
    @Service Repository repository;

    public Iterable<QualifiedIdentity> findEntities( String resultType, BooleanExpression whereClause,
                                                     OrderBy[] orderBySegments, Integer firstResult, Integer maxResults )
        throws EntityFinderException
    {
        CollectingQualifiedIdentityResultCallback collectingCallback = new CollectingQualifiedIdentityResultCallback();
        performTupleQuery( resultType, whereClause, orderBySegments, firstResult, maxResults, new SparqlRdfQueryParser(), collectingCallback );
        return collectingCallback.getEntities();
    }

    public QualifiedIdentity findEntity( String resultType, BooleanExpression whereClause )
        throws EntityFinderException
    {
        final SingleQualifiedIdentityResultCallback singleCallback = new SingleQualifiedIdentityResultCallback();
        performTupleQuery( resultType, whereClause, null, null, null, new SparqlRdfQueryParser(), singleCallback );
        return singleCallback.getQualifiedIdentity();
    }

    public long countEntities( String resultType, BooleanExpression whereClause ) throws EntityFinderException
    {
        return performTupleQuery( resultType, whereClause, null, null, null, new SparqlRdfQueryParser(), null );
    }


    private int performTupleQuery( String resultType,
                                   BooleanExpression whereClause,
                                   OrderBy[] orderBySegments,
                                   Integer firstResult,
                                   Integer maxResults,
                                   RdfQueryParser parser,
                                   QualifiedIdentityResultCallback qualifiedIdentityResultCallback )
        throws EntityFinderException
    {
        try
        {
            RepositoryConnection connection = repository.getConnection();

            QueryLanguage queryLanguage = parser.getQueryLanguage();
            String query = parser.getQuery( resultType, whereClause, orderBySegments, firstResult, maxResults );
            TupleQuery tupleQuery = connection.prepareTupleQuery( queryLanguage, query );
            tupleQuery.setIncludeInferred( false );

            TupleQueryResult result = tupleQuery.evaluate();
            try
            {
                int row = 0;
                while( result.hasNext() )
                {
                    BindingSet bindingSet = result.next();
                    if( qualifiedIdentityResultCallback != null )
                    {
                        if( !processRow( row, bindingSet, qualifiedIdentityResultCallback ) )
                        {
                            break;
                        }
                    }
                    row++;
                }
                return row;
            }
            finally
            {
                if( result != null )
                {
                    result.close();
                }
                if( connection != null )
                {
                    connection.close();
                }
            }
        }
        catch( RepositoryException e )
        {
            throw new EntityFinderException( e );
        }
        catch( MalformedQueryException e )
        {
            throw new EntityFinderException( e );
        }
        catch( QueryEvaluationException e )
        {
            throw new EntityFinderException( e );
        }
        catch( Exception e )
        {
            throw new EntityFinderException( e );
        }
    }

    private boolean processRow( int row, BindingSet bindingSet, QualifiedIdentityResultCallback qualifiedIdentityResultCallback )
    {
        final Value identifier = bindingSet.getValue( "identity" );

        //TODO Shall we throw an exception if there is no binding for identifier = query parser is not right
        if( identifier == null )
        {
            return true;
        }

        final Value entityClass = bindingSet.getValue( "entityType" );
        final String identity = identifier.stringValue();
        final String entityType = ClassUtil.toClassName( entityClass.stringValue());

        final QualifiedIdentity qualifiedIdentity = new QualifiedIdentity( identity, entityType );
        return qualifiedIdentityResultCallback.processRow( row, qualifiedIdentity );
    }
}