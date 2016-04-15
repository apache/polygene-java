/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.regression.qi377;

import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.property.Property;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

public class InterfaceCollisionWithUnrelatedReturnTypesTest
    extends AbstractZestTest
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
     * java: types org.apache.zest.regression.qi377.InterfaceCollisionWithUnrelatedReturnTypesTest.Robot and org.apache.zest.regression.qi377.InterfaceCollisionWithUnrelatedReturnTypesTest.Person are incompatible; both define name(), but with unrelated return types
     */
//    public interface TeamMember extends Person, Robot {}
}
