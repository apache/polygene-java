/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2008 Niclas Hedhman.
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

import org.openrdf.query.QueryLanguage;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.index.rdf.RdfFactory;
import org.qi4j.index.rdf.RdfQueryParser;
import org.qi4j.index.rdf.callback.CollectingQualifiedIdentityResultCallback;
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

    @Service private RdfFactory factory;
    @This TupleQueryExecutor tupleExecutor;

    public Iterable<EntityReference> findEntities( String resultType,
                                                   BooleanExpression whereClause,
                                                   OrderBy[] orderBySegments,
                                                   Integer firstResult,
                                                   Integer maxResults )
        throws EntityFinderException
    {
        CollectingQualifiedIdentityResultCallback collectingCallback = new CollectingQualifiedIdentityResultCallback();
        RdfQueryParser rdfQueryParser = factory.newQueryParser( language );
        String query = rdfQueryParser.getQuery( resultType, whereClause, orderBySegments, firstResult, maxResults );
        tupleExecutor.performTupleQuery( language, query, collectingCallback );
        return collectingCallback.getEntities();
    }

    public EntityReference findEntity( String resultType, BooleanExpression whereClause )
        throws EntityFinderException
    {
        final SingleQualifiedIdentityResultCallback singleCallback = new SingleQualifiedIdentityResultCallback();
        RdfQueryParser rdfQueryParser = factory.newQueryParser( language );
        String query = rdfQueryParser.getQuery( resultType, whereClause, null, null, null );
        tupleExecutor.performTupleQuery( language, query, singleCallback );
        return singleCallback.getQualifiedIdentity();
    }

    public long countEntities( String resultType, BooleanExpression whereClause ) throws EntityFinderException
    {
        RdfQueryParser rdfQueryParser = factory.newQueryParser( language );
        String query = rdfQueryParser.getQuery( resultType, whereClause, null, null, null );
        return tupleExecutor.performTupleQuery( language, query, null );
    }


}