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

import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.envisage.school.domain.school.School;
import org.apache.zest.envisage.school.domain.school.Student;
import org.apache.zest.envisage.school.domain.school.Subject;

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

        Property<Identity> schoolId();
    }

}
