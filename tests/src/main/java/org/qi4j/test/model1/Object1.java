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

import org.qi4j.composite.scope.PropertyField;
import org.qi4j.composite.scope.PropertyParameter;

/**
 * Object with property injections
 */
public class Object1
{
    @PropertyField
    String foo;

    @PropertyField( optional = true )
    String bar;

    int xyzzy;

    public Object1( @PropertyParameter( "xyzzy" )int xyzzy )
    {
        this.xyzzy = xyzzy;
    }


    public String getFoo()
    {
        return foo;
    }

    public String getBar()
    {
        return bar;
    }

    public int getXyzzy()
    {
        return xyzzy;
    }
}
