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
import static org.junit.Assert.fail;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.core.test.osgi.AnEntity;

public final class EntityTest extends AbstractTest
{
    @Inject
    private BundleContext bundleContext;

    private UnitOfWorkFactory getUnitOfWorkFactory( ServiceReference moduleRef )
    {
        assertNotNull( moduleRef );

        Module module = (Module) bundleContext.getService( moduleRef );
        assertNotNull( module );

        return module.unitOfWorkFactory();
    }

    private String createNewEntity( UnitOfWorkFactory uowf )
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        AnEntity entity = uow.newEntity( AnEntity.class );
        assertNotNull( entity );

        String identity = ( (Identity) entity ).identity().get();

        Property<String> property = entity.property();
        assertNotNull( property );

        assertNull( property.get() );
        property.set( "abc" );
        assertEquals( "abc", property.get() );

        uow.complete();

        return identity;
    }

    @Test
    public final void testCRUD()
        throws Throwable
    {
        ServiceReference moduleRef = getModuleServiceRef( bundleContext );
        UnitOfWorkFactory uowf = getUnitOfWorkFactory( moduleRef );

        // Test creational
        String identity = createNewEntity( uowf );

        // Test retrieval
        UnitOfWork work = uowf.newUnitOfWork();
        AnEntity entity = work.get(AnEntity.class, identity);
        assertNotNull( entity );

        // Test update
        String newPropValue = entity.property().get() + "a";
        entity.property().set( newPropValue );
        work.complete();

        work = uowf.newUnitOfWork();
        entity = work.get(AnEntity.class, identity);
        assertNotNull( entity );
        assertEquals( newPropValue, entity.property().get() );
        work.complete();

        // Test removal
        work = uowf.newUnitOfWork();
        entity = work.get(AnEntity.class, identity);
        assertNotNull( entity );
        work.remove( entity );
        work.complete();

        // Commented out: The odd thing is, removal fails here.
        work = uowf.newUnitOfWork();
        try
        {
            entity = work.get(AnEntity.class, identity);
            fail( "Test removal fail. [" + ( entity == null ) + "] identity [" + identity + "]" );
        }
        catch( NoSuchEntityException e )
        {
            // Expected
        }
        work.complete();

        // Clean up
        bundleContext.ungetService( moduleRef );
    }
}
