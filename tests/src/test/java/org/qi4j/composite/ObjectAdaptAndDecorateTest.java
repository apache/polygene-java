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

package org.qi4j.composite;

import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.model1.Object2;
import org.qi4j.test.model1.Object3;

/**
 * TODO
 */
public class ObjectAdaptAndDecorateTest
    extends AbstractQi4jTest
{
    @Override public void configure( ModuleAssembly module )
    {
        module.addObjects( Object2.class );
    }

    public void testAdaptInjection()
        throws Exception
    {
        ObjectBuilder<Object2> builder = objectBuilderFactory.newObjectBuilder( Object2.class );
        Object3 adapted = new Object3( "Object1" );
        Object3 decorated = new Object3( "Object2" );
        builder.adapt( adapted );
        builder.decorate( decorated );

        Object2 object = builder.newInstance();

        assertEquals( adapted, object.getAdapted() );
        assertEquals( decorated, object.getDecorated() );
    }
}
