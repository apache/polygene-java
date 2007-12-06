/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.test.model1;

import org.qi4j.composite.scope.Adapt;
import org.qi4j.composite.scope.Decorate;

/**
 * Object with adapt and decorate injections
 */
public class Object2
{
    @Adapt
    Object3 adapted;

    @Decorate
    Object3 decorated;

    public Object3 getAdapted()
    {
        return adapted;
    }

    public Object3 getDecorated()
    {
        return decorated;
    }
}
