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
package org.qi4j.osgi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.core.test.osgi.AComposite;

public final class CompositeTest extends AbstractTest
{
    @Inject
    private BundleContext bundleContext;

    @Test
    public final void testCreational()
    {
        ServiceReference moduleServiceRef = getModuleServiceRef( bundleContext );
        assertNotNull( moduleServiceRef );

        Module module = (Module) bundleContext.getService( moduleServiceRef );
        assertNotNull( module );

        TransientBuilderFactory factory = module.transientBuilderFactory();
        AComposite composite = factory.newTransient( AComposite.class );
        assertNotNull( composite );

        Property<String> property = composite.property();
        assertNotNull( property );

        assertNull( property.get() );
        property.set( "abc" );
        assertEquals( "abc", property.get() );

        // Clean up
        bundleContext.ungetService( moduleServiceRef );
    }
}
