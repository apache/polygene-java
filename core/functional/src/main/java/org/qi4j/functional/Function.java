/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.functional;

/**
 * Generic function interface to map from one type to another.
 *
 * This can be used with the Iterables methods to transform lists of objects.
 *
 * @param <From>
 * @param <To>
 */
public interface Function<From, To>
{
    /**
     * Map a single item from one type to another
     *
     * @param from the input item
     *
     * @return the mapped item
     */
    To map( From from );
}
