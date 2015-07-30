/*
 * Copyright 2009 Niclas Hedhman.
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
package org.apache.zest.tutorials.services.step2;

import org.junit.Test;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractQi4jTest;

public class LibraryTest
    extends AbstractQi4jTest
{
    @Test
    public void testLibrary()
        throws Exception
    {
        Consumer consumer = module.newObject( Consumer.class );
        consumer.run();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( LibraryService.class );
        module.values( Book.class );
        module.objects( Consumer.class );
    }
}
