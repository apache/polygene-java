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
package org.qi4j.index.rdf.internal;

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
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.util.Classes;
import org.qi4j.index.rdf.RdfFactory;
import org.qi4j.index.rdf.RdfQueryParser;
import org.qi4j.index.rdf.callback.CollectingQualifiedIdentityResultCallback;
import org.qi4j.index.rdf.callback.QualifiedIdentityResultCallback;
import org.qi4j.index.rdf.callback.SingleQualifiedIdentityResultCallback;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;

/**
 * JAVADOC Add JavaDoc
 * JAVADOC shall we support different implementation as SERQL?
 */
public class RdfEntityFinderMixin
        implements EntityFinder
{
    private static final QueryLanguage language = QueryLanguage.SPARQL;

    @Service
    private Repository repository;
    @Service
    private RdfFactory factory;


    public Iterable<EntityReference> findEntities(String resultType, BooleanExpression whereClause,
                                                  OrderBy[] orderBySegments, Integer firstResult, Integer maxResults)
            throws EntityFinderException
    {
        CollectingQualifiedIdentityResultCallback collectingCallback = new CollectingQualifiedIdentityResultCallback();
        RdfQueryParser rdfQueryParser = factory.newQueryParser(language);
        String query = rdfQueryParser.getQuery(resultType, whereClause, orderBySegments, firstResult, maxResults);
        performTupleQuery(query, collectingCallback);
        return collectingCallback.getEntities();
    }

    public EntityReference findEntity(String resultType, BooleanExpression whereClause)
            throws EntityFinderException
    {
        final SingleQualifiedIdentityResultCallback singleCallback = new SingleQualifiedIdentityResultCallback();
        RdfQueryParser rdfQueryParser = factory.newQueryParser(language);
        String query = rdfQueryParser.getQuery(resultType, whereClause, null, null, null);
        performTupleQuery(query, singleCallback);
        return singleCallback.getQualifiedIdentity();
    }

    public long countEntities(String resultType, BooleanExpression whereClause) throws EntityFinderException
    {
        RdfQueryParser rdfQueryParser = factory.newQueryParser(language);
        String query = rdfQueryParser.getQuery(resultType, whereClause, null, null, null);
        return performTupleQuery(query, null);
    }


    private long performTupleQuery(String query,
                                  QualifiedIdentityResultCallback qualifiedIdentityResultCallback)
            throws EntityFinderException
    {
        try
        {
            RepositoryConnection connection = repository.getConnection();

            TupleQuery tupleQuery = connection.prepareTupleQuery(language, query);
            tupleQuery.setIncludeInferred(false);

            TupleQueryResult result = tupleQuery.evaluate();
            try
            {
                long row = 0;
                while (result.hasNext())
                {
                    if (handleCallbacks(qualifiedIdentityResultCallback, result, row))
                    {
                        break;
                    }
                    row++;
                }
                return row;
            }
            finally
            {
                if (result != null)
                {
                    result.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            }
        }
        catch (RepositoryException e)
        {
            throw new EntityFinderException(e);
        }
        catch (MalformedQueryException e)
        {
            throw new EntityFinderException(e);
        }
        catch (QueryEvaluationException e)
        {
            throw new EntityFinderException(e);
        }
        catch (Exception e)
        {
            throw new EntityFinderException(e);
        }
    }

    private boolean handleCallbacks(QualifiedIdentityResultCallback qualifiedIdentityResultCallback, TupleQueryResult result, long row)
            throws Exception
    {
        BindingSet bindingSet = result.next();
        if (qualifiedIdentityResultCallback != null)
        {
            if (!processRow(row, bindingSet, qualifiedIdentityResultCallback))
            {
                return true;
            }
        }
        return false;
    }

    private boolean processRow(long row, BindingSet bindingSet, QualifiedIdentityResultCallback qualifiedIdentityResultCallback)
    {
        final Value identifier = bindingSet.getValue("identity");

        //TODO Shall we throw an exception if there is no binding for identifier = query parser is not right
        if (identifier == null)
        {
            return true;
        }

        final Value entityClass = bindingSet.getValue("entityType");
        final String identity = identifier.stringValue();
        final String entityType = Classes.toClassName(entityClass.stringValue());

        final EntityReference entityReference = new EntityReference(identity);
        return qualifiedIdentityResultCallback.processRow(row, entityReference);
    }
}