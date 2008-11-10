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
package org.qi4j.library.swing.visualizer.school.domain.model.user.assembler;

import org.qi4j.composite.Mixins;
import org.qi4j.entity.Identity;
import org.qi4j.injection.scope.This;
import org.qi4j.library.swing.visualizer.school.domain.model.user.Student;
import org.qi4j.library.swing.visualizer.school.domain.model.user.StudentId;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( StudentEntity.StudentMixin.class )
interface StudentEntity extends Student, UserEntity
{
    abstract class StudentMixin
        implements Student
    {
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
    }
}
