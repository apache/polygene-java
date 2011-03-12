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
package org.qi4j.index.rdf.query;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueryDescriptor;

/**
 * JAVADOC Add JavaDoc
 */
@Mixins( { RdfQueryService.RdfEntityFinderMixin.class, RdfQueryService.RdfNamedEntityFinderMixin.class } )
public interface RdfQueryService
    extends EntityFinder, NamedEntityFinder, RdfQueryParserFactory, ServiceComposite
{
    /**
     * JAVADOC Add JavaDoc
     * JAVADOC shall we support different implementation as SERQL?
     */
    public static class RdfEntityFinderMixin
        implements EntityFinder
    {

        private static final QueryLanguage language = QueryLanguage.SPARQL;

        @Service
        private RdfQueryParserFactory queryParserFactory;

        @This
        TupleQueryExecutor tupleExecutor;

        public Iterable<EntityReference> findEntities( Class<?> resultType,
                                                       BooleanExpression whereClause,
                                                       OrderBy[] orderBySegments,
                                                       Integer firstResult,
                                                       Integer maxResults
        )
            throws EntityFinderException
        {
            CollectingQualifiedIdentityResultCallback collectingCallback = new CollectingQualifiedIdentityResultCallback();
            RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
            String query = rdfQueryParser.getQuery( resultType, whereClause, orderBySegments, firstResult, maxResults );
            tupleExecutor.performTupleQuery( language, query, null, collectingCallback );
            return collectingCallback.getEntities();
        }

        public EntityReference findEntity( Class<?> resultType, BooleanExpression whereClause )
            throws EntityFinderException
        {
            final SingleQualifiedIdentityResultCallback singleCallback = new SingleQualifiedIdentityResultCallback();
            RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
            String query = rdfQueryParser.getQuery( resultType, whereClause, null, null, null );
            tupleExecutor.performTupleQuery( language, query, null, singleCallback );
            return singleCallback.getQualifiedIdentity();
        }

        public long countEntities( Class<?> resultType, BooleanExpression whereClause )
            throws EntityFinderException
        {
            RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
            String query = rdfQueryParser.getQuery( resultType, whereClause, null, null, null );
            return tupleExecutor.performTupleQuery( language, query, null, null );
        }
    }

    public static class RdfNamedEntityFinderMixin
        implements NamedEntityFinder
    {
        @Service
        private RdfQueryParserFactory queryParserFactory;

        @This
        private TupleQueryExecutor tupleExecutor;

        public Iterable<EntityReference> findEntities( NamedQueryDescriptor descriptor,
                                                       String resultType,
                                                       Map<String, Object> variables,
                                                       OrderBy[] orderBySegments,
                                                       Integer firstResult,
                                                       Integer maxResults
        )
            throws EntityFinderException
        {
            Map<String, Value> bindings = getBindings(variables);

            QueryLanguage queryLanguage = QueryLanguage.valueOf( descriptor.language() );
            String query = descriptor.compose( variables, orderBySegments, firstResult, maxResults );
            CollectingQualifiedIdentityResultCallback callback = new CollectingQualifiedIdentityResultCallback();
            tupleExecutor.performTupleQuery( queryLanguage, query, bindings, callback );
            return callback.getEntities();
        }

        public EntityReference findEntity( NamedQueryDescriptor descriptor,
                                           String resultType,
                                           Map<String, Object> variables
        )
            throws EntityFinderException
        {
            Map<String, Value> bindings = getBindings(variables);

            QueryLanguage queryLanguage = QueryLanguage.valueOf( descriptor.language() );
            String query = descriptor.compose( variables, null, null, 1 );
            SingleQualifiedIdentityResultCallback callback = new SingleQualifiedIdentityResultCallback();
            tupleExecutor.performTupleQuery( queryLanguage, query, bindings, callback );
            return callback.getQualifiedIdentity();
        }

        public long countEntities( NamedQueryDescriptor descriptor, String resultType, Map<String, Object> variables )
            throws EntityFinderException
        {
            Map<String, Value> bindings = getBindings(variables);

            QueryLanguage queryLanguage = QueryLanguage.valueOf( descriptor.language() );
            return tupleExecutor.performTupleQuery( queryLanguage, descriptor.compose( null, null, null, null ), bindings, null );
        }

        public String showQuery( NamedQueryDescriptor descriptor )
        {
            return descriptor.compose( null, null, null, null );
        }

        private Map<String, Value> getBindings(Map<String, Object> variables)
        {
            Map<String, Value> bindings = new HashMap<String, Value>();
            for (Map.Entry<String, Object> stringObjectEntry : variables.entrySet())
            {
                if (!stringObjectEntry.getValue().getClass().equals(Object.class))
                    bindings.put(stringObjectEntry.getKey(), ValueFactoryImpl.getInstance().createLiteral(stringObjectEntry.getValue().toString()));
            }
            return bindings;
        }
    }
}
