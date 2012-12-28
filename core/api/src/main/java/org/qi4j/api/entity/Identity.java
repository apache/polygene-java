/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.entity;

import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

/**
 * This interface provides the identity of the object which may be used
 * to store the state in a database. It is not the responsibility of the
 * framework to come up with a good identity string.
 */
@Mixins( Identity.IdentityMixin.class )
public interface Identity
{
    /**
     * Returns the client view of the identity.
     * <p/>
     * It is unique within the owning repository, but potentially not unique globally and between
     * types.
     *
     * @return The Identity of 'this' composite.
     */
    @Immutable
    Property<String> identity();

    /**
     * Default Identity implementation.
     */
    public class IdentityMixin
        implements Identity
    {
        @State
        private Property<String> identity;

        @Override
        public Property<String> identity()
        {
            return identity;
        }
    }
}
