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
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.swing.visualizer.school.domain.model.school.School;
import org.qi4j.library.swing.visualizer.school.domain.model.school.Student;
import org.qi4j.library.swing.visualizer.school.domain.model.school.StudentId;
import org.qi4j.library.swing.visualizer.school.domain.model.school.Subject;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( StudentEntity.StudentMixin.class )
interface StudentEntity extends Student, EntityComposite
{
    class StudentMixin
        implements Student
    {
        @This private StudentState state;
        private final StudentId studentId;

        public StudentMixin( @This Identity identity )
        {
            String studentIdString = identity.identity().get();
            studentId = new StudentId( studentIdString );
        }

        public final StudentId studentId()
        {
            return studentId;
        }

        public final School school()
        {
            return state.school().get();
        }

        public final Iterable<Subject> subjects()
        {
            return state.subjects();
        }
    }
}
