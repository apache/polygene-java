/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.index.rdf.query;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.api.query.grammar.QuerySpecification;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.spi.query.EntityFinder;
import org.apache.polygene.spi.query.EntityFinderException;
import org.openrdf.query.QueryLanguage;

/**
 * JAVADOC Add JavaDoc
 */
@Mixins( { RdfQueryService.RdfEntityFinderMixin.class } )
public interface RdfQueryService
    extends EntityFinder, RdfQueryParserFactory, ServiceComposite
{
    /**
     * JAVADOC Add JavaDoc
     */
    public static class RdfEntityFinderMixin
        implements EntityFinder
    {

        private static final QueryLanguage language = QueryLanguage.SPARQL;

        @Service
        private RdfQueryParserFactory queryParserFactory;

        @This
        TupleQueryExecutor tupleExecutor;

        @Override
        public Stream<EntityReference> findEntities( Class<?> resultType,
                                                     Predicate<Composite> whereClause,
                                                     List<OrderBy> orderBySegments,
                                                     Integer firstResult,
                                                     Integer maxResults,
                                                     Map<String, Object> variables ) throws EntityFinderException
        {
            CollectingQualifiedIdentityResultCallback collectingCallback = new CollectingQualifiedIdentityResultCallback();

            if( QuerySpecification.isQueryLanguage( "SERQL", whereClause ))
            {
                String query = ((QuerySpecification)whereClause).query();
                tupleExecutor.performTupleQuery( QueryLanguage.SERQL, query, variables, collectingCallback );
                return collectingCallback.entities().stream();

            } else
            {
                RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
                String query = rdfQueryParser.constructQuery( resultType, whereClause, orderBySegments, firstResult, maxResults, variables );

                tupleExecutor.performTupleQuery( language, query, variables, collectingCallback );
                return collectingCallback.entities().stream();
            }
        }

        @Override
        public EntityReference findEntity( Class<?> resultType, Predicate<Composite> whereClause, Map<String, Object> variables )
            throws EntityFinderException
        {
            final SingleQualifiedIdentityResultCallback singleCallback = new SingleQualifiedIdentityResultCallback();

            if (QuerySpecification.isQueryLanguage( "SERQL", whereClause))
            {
                String query = ((QuerySpecification)whereClause).query();
                tupleExecutor.performTupleQuery( QueryLanguage.SERQL, query, variables, singleCallback );
                return singleCallback.qualifiedIdentity();
            } else
            {
                RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
                String query = rdfQueryParser.constructQuery( resultType, whereClause, null, null, null, variables );
                tupleExecutor.performTupleQuery( QueryLanguage.SPARQL, query, variables, singleCallback );
                return singleCallback.qualifiedIdentity();
            }
        }

        @Override
        public long countEntities( Class<?> resultType, Predicate<Composite> whereClause, Map<String, Object> variables )
            throws EntityFinderException
        {
            if (QuerySpecification.isQueryLanguage( "SERQL", whereClause ))
            {
                String query = ((QuerySpecification)whereClause).query();
                return tupleExecutor.performTupleQuery( QueryLanguage.SERQL, query, variables, null );

            } else
            {
                RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
                String query = rdfQueryParser.constructQuery( resultType, whereClause, null, null, null, variables );
                return tupleExecutor.performTupleQuery( language, query, variables, null );
            }
        }
    }
}
