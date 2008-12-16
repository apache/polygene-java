/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.library.swing.visualizer.school.domain.model.school.assembler;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.swing.visualizer.school.domain.model.person.Person;
import org.qi4j.library.swing.visualizer.school.domain.model.school.School;
import org.qi4j.library.swing.visualizer.school.domain.model.school.SchoolId;
import org.qi4j.library.swing.visualizer.school.domain.model.school.Student;
import org.qi4j.library.swing.visualizer.school.domain.model.school.Subject;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( SchoolEntity.SchoolMixin.class )
interface SchoolEntity extends School, EntityComposite
{
    class SchoolMixin
        implements School
    {
        private final SchoolId schoolId;

        @Structure private UnitOfWorkFactory uowf;
        @This private SchoolState state;

        public SchoolMixin( @This Identity identity )
        {
            String schoolIdString = identity.identity().get();
            schoolId = new SchoolId( schoolIdString );
        }

        public final SchoolId schoolId()
        {
            return schoolId;
        }

        public String name()
        {
            return state.name().get();
        }

        public final Query<Subject> availableSubjects()
        {
            UnitOfWork uow = uowf.nestedUnitOfWork();
            try
            {
                QueryBuilderFactory qbf = uow.queryBuilderFactory();
                QueryBuilder<Subject> builder = qbf.newQueryBuilder( Subject.class );
                SubjectState subject = templateFor( SubjectState.class );
                builder.where( eq( subject.schoolId(), schoolId.idString() ) );

                return builder.newQuery();
            }
            finally
            {
                uow.pause();
            }
        }

        public final Query<Student> students()
        {
            UnitOfWork uow = uowf.nestedUnitOfWork();
            try
            {
                QueryBuilderFactory qbf = uow.queryBuilderFactory();
                QueryBuilder<Student> builder = qbf.newQueryBuilder( Student.class );
                StudentState studentState = templateFor( StudentState.class );
                builder.where( eq( studentState.schoolId(), schoolId.idString() ) );

                return builder.newQuery();
            }
            finally
            {
                uow.pause();
            }
        }

        public final void enroll( Person person, Subject subject )
        {
            // TODO
            throw new UnsupportedOperationException();
        }
    }
}
