/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.runtime.structure;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.qi4j.functional.Iterables.first;

public class CompositeDescriptorTest
    extends AbstractQi4jTest
{
    @Test
    public final void testCompositeDescriptorWithComposite()
        throws Throwable
    {
        // Test with Standard composite
        AddressComposite address = module.newTransient( AddressComposite.class );
        TransientDescriptor addressDescriptor = spi.transientDescriptorFor( address );

        assertNotNull( addressDescriptor );
        assertEquals( AddressComposite.class, first( addressDescriptor.types() ) );
        assertTrue( TransientDescriptor.class.isAssignableFrom( addressDescriptor.getClass() ) );
    }

    @Test
    public final void testCompositeDescriptorWithMixin()
    {
        // Test with composite
        TransientDescriptor addressDesc = module.transientDescriptor( AddressComposite.class.getName() );
        assertNotNull( addressDesc );

        assertEquals( AddressComposite.class, first( addressDesc.types() ) );
    }

    public final void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.transients( AddressComposite.class );
    }

    private static interface AddressComposite
        extends Address, TransientComposite
    {
    }

    private static interface Address
    {
    }
}