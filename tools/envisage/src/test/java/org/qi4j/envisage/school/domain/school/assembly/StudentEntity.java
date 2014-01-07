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

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.envisage.school.domain.school.School;
import org.qi4j.envisage.school.domain.school.Student;
import org.qi4j.envisage.school.domain.school.Subject;

@Mixins( StudentEntity.StudentMixin.class )
public interface StudentEntity
    extends Student, EntityComposite
{
    class StudentMixin
        implements Student
    {
        @This
        private StudentState state;

        @Override
        public School school()
        {
            return state.school().get();
        }

        @Override
        public Iterable<Subject> subjects()
        {
            return state.subjects();
        }
    }

    static interface StudentState
    {
        ManyAssociation<Subject> subjects();

        Association<School> school();

        Property<String> schoolId();
    }

}
