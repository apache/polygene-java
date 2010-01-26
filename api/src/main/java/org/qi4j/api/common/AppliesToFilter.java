/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.common;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Implementations of this interface can be specified in the AppliesTo.
 * An instance of the provided class will be used to test if the modifier or mixin
 * should be applied to the method or not.
 */
public interface AppliesToFilter
    extends Serializable
{
    AppliesToFilter ALWAYS = new AppliesToFilter()
    {
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            return true;
        }
    };

    /**
     * Check if the Fragment should be applied or not. Can be used
     * with Mixins, Concerns, SideEffects.
     *
     * @param method        method that is invoked
     * @param mixin         mixin implementation for the method
     * @param compositeType composite type
     * @param fragmentClass fragment that is being applies
     *
     * @return true if the filter passes, otherwise false
     */
    boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass );
}
