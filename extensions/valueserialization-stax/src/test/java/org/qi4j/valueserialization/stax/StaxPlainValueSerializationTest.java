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
package org.qi4j.valueserialization.stax;

import org.junit.BeforeClass;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.value.AbstractPlainValueSerializationTest;

import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

public class StaxPlainValueSerializationTest
    extends AbstractPlainValueSerializationTest
{

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    // START SNIPPET: assembly
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new StaxValueSerializationAssembler().assemble( module );
    }
    // END SNIPPET: assembly
}
