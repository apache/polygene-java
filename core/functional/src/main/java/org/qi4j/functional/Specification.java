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
 * Generic specification interface.
 *
 * @param <T>
 */
// START SNIPPET: specification
public interface Specification<T>
{
// END SNIPPET: specification

    /**
     * Test whether an item matches the given specification
     *
     * @param item the item to be tested
     *
     * @return true if the item matches, false otherwise
     */
// START SNIPPET: specification
    boolean satisfiedBy( T item );
}
// END SNIPPET: specification
