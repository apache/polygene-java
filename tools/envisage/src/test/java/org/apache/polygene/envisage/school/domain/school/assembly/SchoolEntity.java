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

import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.envisage.school.domain.person.Person;
import org.apache.polygene.envisage.school.domain.school.School;
import org.apache.polygene.envisage.school.domain.school.Student;
import org.apache.polygene.envisage.school.domain.school.Subject;
import org.apache.polygene.envisage.school.infrastructure.mail.MailService;

import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;

@Mixins( SchoolEntity.SchoolMixin.class )
public interface SchoolEntity
    extends School, HasIdentity
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
        private final Identity schoolId;

        public SchoolMixin( @This HasIdentity identity )
        {
            schoolId = identity.identity().get();
        }

        @Override
        public String name()
        {
            return state.name().get();
        }

        @Override
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

        @Override
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

        @Override
        public void enroll( Person person, Subject subject )
        {
            // TODO
            throw new UnsupportedOperationException();
        }
    }

    interface SchoolState
    {
        Property<String> name();
    }

}
