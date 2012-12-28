/*  Copyright 2008 Edward Yakop.
*   Copyright 2009 Niclas Hedhman.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.envisage.school.domain.school.assembly;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.envisage.school.domain.person.Person;
import org.qi4j.envisage.school.domain.school.School;
import org.qi4j.envisage.school.domain.school.Student;
import org.qi4j.envisage.school.domain.school.Subject;
import org.qi4j.envisage.school.infrastructure.mail.MailService;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

@Mixins( SchoolEntity.SchoolMixin.class )
public interface SchoolEntity
    extends School, EntityComposite
{
    class SchoolMixin
        implements School
    {
        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private QueryBuilderFactory qbf;
        @This
        private SchoolState state;
        @Service
        private MailService mailer;
        private String schoolId;

        public SchoolMixin( @This Identity identity )
        {
            schoolId = identity.identity().get();
        }

        public String name()
        {
            return state.name().get();
        }

        public Query<Subject> availableSubjects()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            try
            {
                QueryBuilder<Subject> builder = qbf.newQueryBuilder( Subject.class );
                SubjectEntity.SubjectState subject = templateFor( SubjectEntity.SubjectState.class );
                builder.where( eq( subject.schoolId(), schoolId ) );
                return uow.newQuery( builder );
            }
            finally
            {
                uow.pause();
            }
        }

        public Query<Student> students()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            try
            {
                QueryBuilder<Student> builder = qbf.newQueryBuilder( Student.class );
                StudentEntity.StudentState studentState = templateFor( StudentEntity.StudentState.class );
                builder.where( eq( studentState.schoolId(), schoolId ) );
                return uow.newQuery( builder );
            }
            finally
            {
                uow.pause();
            }
        }

        public void enroll( Person person, Subject subject )
        {
            // TODO
            throw new UnsupportedOperationException();
        }
    }

    static interface SchoolState
    {
        Property<String> name();
    }
}