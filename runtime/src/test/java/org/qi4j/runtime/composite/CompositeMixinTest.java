/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.runtime.composite;

import org.junit.Test;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.*;

/**
 * Test CompositeMixin implementation
 */
public class CompositeMixinTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( TestComposite.class );
    }

    @Test
    public void testGetCompositeType()
    {
        TestComposite composite = transientBuilderFactory.newTransient( TestComposite.class );
        Class<? extends Composite> compositeType = composite.type();

        assertEquals( TestComposite.class, compositeType );
    }

    public interface TestComposite
        extends TransientComposite
    {
    }
}
