/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.valueserialization.orgjson;

import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.value.AbstractPlainValueSerializationTest;

public class OrgJsonPlainValueSerializationTest
    extends AbstractPlainValueSerializationTest
{

    // START SNIPPET: assembly
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new OrgJsonValueSerializationAssembler().assemble( module );
    }
    // END SNIPPET: assembly
}
