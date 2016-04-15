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

package org.apache.zest.envisage.school.domain.school.assembly;

import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryBuilderFactory;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.envisage.school.domain.person.Person;
import org.apache.zest.envisage.school.domain.school.School;
import org.apache.zest.envisage.school.domain.school.Student;
import org.apache.zest.envisage.school.domain.school.Subject;
import org.apache.zest.envisage.school.infrastructure.mail.MailService;

import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

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
        private final String schoolId;

        public SchoolMixin( @This Identity identity )
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

    static interface SchoolState
    {
        Property<String> name();
    }

}
