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

package org.apache.polygene.envisage.school.domain.school.assembly;

import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.envisage.school.domain.school.School;
import org.apache.polygene.envisage.school.domain.school.SchoolRepository;

import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;

@Mixins( SchoolRepositoryService.SchoolRepositoryMixin.class )
public interface SchoolRepositoryService
    extends SchoolRepository
{

    class SchoolRepositoryMixin
        implements SchoolRepository
    {
        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private QueryBuilderFactory qbf;

        @Override
        public Query<School> findAll()
        {
            return uowf.currentUnitOfWork().newQuery( qbf.newQueryBuilder( School.class ) );
        }

        @Override
        public School findSchoolByName( String schoolName )
        {
            QueryBuilder<School> builder = qbf.newQueryBuilder( School.class );
            SchoolEntity.SchoolState template = templateFor( SchoolEntity.SchoolState.class );
            builder.where( eq( template.name(), schoolName ) );
            Query<School> query = uowf.currentUnitOfWork().newQuery( builder );
            return query.find();
        }
    }

}
