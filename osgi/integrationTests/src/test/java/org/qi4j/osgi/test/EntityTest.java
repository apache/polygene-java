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

import org.osgi.framework.ServiceReference;
import org.qi4j.core.test.osgi.AnEntity;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.property.Property;
import org.qi4j.structure.Module;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class EntityTest extends AbstractTest
{
    public final void testCreational()
        throws Throwable
    {
        ServiceReference moduleServiceRef = getModuleServiceRef();
        assertNotNull( moduleServiceRef );

        Module module = (Module) bundleContext.getService( moduleServiceRef );
        assertNotNull( module );

        UnitOfWorkFactory uowf = module.unitOfWorkFactory();
        UnitOfWork uow = uowf.newUnitOfWork();
        AnEntity entity = uow.newEntity( AnEntity.class );
        assertNotNull( entity );

        Property<String> property = entity.property();
        assertNotNull( property );

        assertNull( property.get() );
        property.set( "abc" );
        assertEquals( "abc", property.get() );

        uow.complete();

        // Clean up
        bundleContext.ungetService( moduleServiceRef );
    }
}
