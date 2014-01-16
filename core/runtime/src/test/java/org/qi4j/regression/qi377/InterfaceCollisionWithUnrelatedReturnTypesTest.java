/*
 * Copyright (c) 2013, Chris Chapman. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.regression.qi377;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

public class InterfaceCollisionWithUnrelatedReturnTypesTest
    extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
    }

    public interface Person
    {
        @UseDefaults
        Property<String> name();
    }

    public interface Robot
    {
        @UseDefaults
        Property<Integer> name();
    }

    /**
     * DOESN'T COMPILE!!!
     * java: types org.qi4j.regression.qi377.InterfaceCollisionWithUnrelatedReturnTypesTest.Robot and org.qi4j.regression.qi377.InterfaceCollisionWithUnrelatedReturnTypesTest.Person are incompatible; both define name(), but with unrelated return types
     */
//    public interface TeamMember extends Person, Robot {}
}
