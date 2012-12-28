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

import java.lang.reflect.Method;

/**
 * Implementations of this interface can be specified in the &#64;AppliesTo.
 * <p>
 * AppliesTo filters are one of the driving technologies in Qi4j. They allow you to apply fragments (Mixins,
 * Concerns, SideEffects), often generic ones, depending on the context that they are evaluated under. This
 * mechanism is heavily used internally in Qi4j to achieve many other features.
 * </p>
 * <p>
 * The starting point is the basic use of AppliesToFilter, where the &#64;AppliesTo annotation is given an
 * AppliesToFilter implementation as an argument, for instance at a Mixin implementation;
 * <pre></code>
 * &#64;AppliesTo( MyAppliesToFilter.class )
 * public class SomeMixin
 *     implements InvocationHandler
 * {
 *
 * }
 *
 * public class MyAppliesToFilter
 *     implements AppliesToFilter
 * {
 *     public boolean appliesTo( Method method, Class&lt;?&gt; mixin, Class&lt;?&gt; compositeType, Class&lt;?&gt; fragmentClass )
 *     {
 *         return method.getName().startsWith( "my" );
 *     }
 * }
 * </code></pre>
 * In the case above, the generic mixin will only be applied to the methods that that is defined by the
 * AppliesToFilter. This is the primary way to define limits on the application of generic fragments, since
 * especially mixins are rarely applied to all methods.
 * </p>
 */
public interface AppliesToFilter
{
    /**
     * This is an internal AppliesToFilter which is assigned if no other AppliesToFilters are found for a given
     * fragment.
     * <p>
     * There is no reason for user code to use this AppliesToFilter directly, and should be perceived as an
     * internal class in Qi4j.
     * </p>
     */
    AppliesToFilter ALWAYS = new AppliesToFilter()
    {
        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            return true;
        }
    };

    /**
     * Check if the Fragment should be applied or not. Will be call when applied to Mixins, Concerns, SideEffects.
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
