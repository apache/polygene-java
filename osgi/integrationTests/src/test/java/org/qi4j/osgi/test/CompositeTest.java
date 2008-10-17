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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.core.test.osgi.AComposite;
import org.qi4j.property.Property;
import org.qi4j.structure.Module;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public class CompositeTest extends AbstractTest
{
    public final void testCreational()
    {
        ServiceReference moduleServiceRef = getModuleServiceRef();
        assertNotNull( moduleServiceRef );

        BundleContext bundleContext = droneContext.getBundleContext();
        Module module = (Module) bundleContext.getService( moduleServiceRef );
        assertNotNull( module );

        CompositeBuilderFactory cmpBuilderFactory = module.compositeBuilderFactory();
        AComposite composite = cmpBuilderFactory.newComposite( AComposite.class );
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
