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

package org.apache.zest.index.rdf.query;

import java.util.Map;
import java.util.function.Predicate;
import org.openrdf.query.QueryLanguage;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.query.grammar.OrderBy;
import org.apache.zest.api.query.grammar.QuerySpecification;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.spi.query.EntityFinder;
import org.apache.zest.spi.query.EntityFinderException;

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
        public Iterable<EntityReference> findEntities( Class<?> resultType,
                                                       Predicate<Composite> whereClause,
                                                       OrderBy[] orderBySegments,
                                                       Integer firstResult,
                                                       Integer maxResults,
                                                       Map<String, Object> variables
        )
            throws EntityFinderException
        {
            CollectingQualifiedIdentityResultCallback collectingCallback = new CollectingQualifiedIdentityResultCallback();

            if( QuerySpecification.isQueryLanguage( "SERQL", whereClause ))
            {
                String query = ((QuerySpecification)whereClause).query();
                tupleExecutor.performTupleQuery( QueryLanguage.SERQL, query, variables, collectingCallback );
                return collectingCallback.entities();

            } else
            {
                RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
                String query = rdfQueryParser.constructQuery( resultType, whereClause, orderBySegments, firstResult, maxResults, variables );

                tupleExecutor.performTupleQuery( language, query, variables, collectingCallback );
                return collectingCallback.entities();
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
