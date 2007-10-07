/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.model1;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.Assertions;
import org.qi4j.api.annotation.Mixins;
import org.qi4j.api.model.CompositeModel;

@Assertions( { Modifier1.class } )
@Mixins( { Mixin1Impl.class } )
public class Composition7
    implements Composite
{
    public <T extends Composite> T newInstance( Class<T> anObjectType )
    {
        return null;
    }

    public <T extends Composite> T cast( Class<T> anObjectType )
    {
        return null;
    }

    public boolean isInstance( Class anObjectType )
    {
        return false;
    }

    public CompositeModel getCompositeModel()
    {
        return null;
    }

    public Composite dereference()
    {
        return null;
    }
}
