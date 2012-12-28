/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.query.model;

import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

/**
 * Test traversal over internal interfaces.
 */
@Mixins( Pet.PetMixin.class )
public interface Pet
{
    void changeOwner( Person owner );

    interface PetState
    {
        @Optional
        Association<Person> owner();
    }

    class PetMixin
        implements Pet
    {
        @This
        PetState state;

        public void changeOwner( Person owner )
        {
            state.owner().set( owner );
        }
    }
}
