/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
 * Copyright (c) 2009, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.envisage.school.domain.school.assembly;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.envisage.school.domain.school.School;
import org.qi4j.envisage.school.domain.school.SchoolRepository;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

@Mixins( SchoolRepositoryService.SchoolRepositoryMixin.class )
public interface SchoolRepositoryService
    extends SchoolRepository, ServiceComposite
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
