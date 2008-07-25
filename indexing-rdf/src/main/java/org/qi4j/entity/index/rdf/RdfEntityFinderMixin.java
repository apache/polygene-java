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

import java.util.ArrayList;
import java.util.Collection;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.injection.scope.This;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;

/**
 * TODO Add JavaDoc
 */
public class RdfEntityFinderMixin
    implements EntityFinder
{

    @This RdfQueryContext queryContext;

    public Iterable<QualifiedIdentity> findEntities(
        final Class resultType,
        final BooleanExpression whereClause,
        final OrderBy[] orderBySegments,
        final Integer firstResult,
        final Integer maxResults )
        throws EntityFinderException
    {
        final Collection<QualifiedIdentity> entities = new ArrayList<QualifiedIdentity>();
        try
        {
            final RepositoryConnection connection = queryContext.getRepository().getConnection();

            // TODO shall we support different implementation as SERQL?
            final RdfQueryParser parser = new SparqlRdfQueryParser();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(
                parser.getQueryLanguage(),
                parser.getQuery( resultType, whereClause, orderBySegments, firstResult, maxResults )
            );
            final TupleQueryResult result = tupleQuery.evaluate();
            try
            {
                while( result.hasNext() )
                {
                    final BindingSet bindingSet = result.next();
                    final Value identifier = bindingSet.getValue( "identity" );
                    final Value entityClass = bindingSet.getValue( "entityType" );
                    //TODO Shall we throw an exception if there is no binding for identifier = query parser is not right
                    if( identifier != null )
                    {
                        System.out.println( entityClass.stringValue() + " -> " + identifier.stringValue() );
                        entities.add( new QualifiedIdentity( identifier.stringValue(), entityClass.stringValue() ) );
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
        return entities;
    }

    public QualifiedIdentity findEntity( Class resultType, BooleanExpression whereClause )
        throws EntityFinderException
    {
        try
        {
            final RepositoryConnection connection = queryContext.getRepository().getConnection();
            // TODO shall we support different implementation as SERQL?
            final RdfQueryParser parser = new SparqlRdfQueryParser();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(
                parser.getQueryLanguage(),
                parser.getQuery( resultType, whereClause, null, null, null )
            );
            final TupleQueryResult result = tupleQuery.evaluate();
            try
            {
                while( result.hasNext() )
                {
                    final BindingSet bindingSet = result.next();
                    final Value identifier = bindingSet.getValue( "identity" );
                    final Value entityClass = bindingSet.getValue( "entityType" );
                    //TODO Shall we throw an exception if there is no binding for identifier = query parser is not right
                    if( identifier != null )
                    {
                        System.out.println( entityClass.stringValue() + " -> " + identifier.stringValue() );
                        return new QualifiedIdentity( identifier.stringValue(), entityClass.stringValue() );
                    }
                }

                return null;
            }
            finally
            {
                result.close();
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
    }

    public long countEntities( Class resultType, BooleanExpression whereClause )
        throws EntityFinderException
    {
        long entityCount = 0;
        try
        {
            final RepositoryConnection connection = queryContext.getRepository().getConnection();
            // TODO shall we support different implementation as SERQL?
            final RdfQueryParser parser = new SparqlRdfQueryParser();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(
                parser.getQueryLanguage(),
                parser.getQuery( resultType, whereClause, null, null, null )
            );
            final TupleQueryResult result = tupleQuery.evaluate();
            try
            {
                while( result.hasNext() )
                {
                    result.next();
                    entityCount++;
                }
                return entityCount;
            }
            finally
            {
                result.close();
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
    }
}