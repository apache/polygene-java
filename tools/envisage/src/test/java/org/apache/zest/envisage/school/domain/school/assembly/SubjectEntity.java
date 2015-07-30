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
package org.apache.zest.envisage.school.domain.school.assembly;

import org.apache.zest.api.association.Association;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.envisage.school.domain.school.School;
import org.apache.zest.envisage.school.domain.school.Subject;

@Mixins( SubjectEntity.SubjectMixin.class )
public interface SubjectEntity
    extends Subject, EntityComposite
{
    public class SubjectMixin
        implements Subject
    {
        @This
        private SubjectState state;

        @Override
        public String name()
        {
            return state.name().get();
        }

        @Override
        public String description()
        {
            return state.description().get();
        }

        @Override
        public School school()
        {
            return state.school().get();
        }
    }

    static interface SubjectState
    {
        Property<String> name();

        Property<String> description();

        Property<String> schoolId();

        Association<School> school();
    }

}
