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
package org.apache.zest.runtime.property;

import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class ImmutablePropertyTest
    extends AbstractZestTest
{
    private static final String KUALA_LUMPUR = "Kuala Lumpur";

    @Test
    public final void testCreationalWithStateFor()
    {
        Location location = createLocation( KUALA_LUMPUR );
        testNamePropertyGet( location, KUALA_LUMPUR );
    }

    private Location createLocation( String locationName )
    {
        TransientBuilder<Location> locationBuilder = transientBuilderFactory.newTransientBuilder( Location.class );
        Location locState = locationBuilder.prototypeFor( Location.class );
        locState.name().set( locationName );
        return locationBuilder.newInstance();
    }

    private void testNamePropertyGet( Location location, String locationName )
    {
        assertNotNull( location );
        assertEquals( locationName, location.name().get() );
    }

    @Test
    public final void testCreationWithStateOfComposite()
    {
        TransientBuilder<Location> locationBuilder = transientBuilderFactory.newTransientBuilder( Location.class );
        Location locState = locationBuilder.prototype();
        locState.name().set( KUALA_LUMPUR );
        Location location = locationBuilder.newInstance();

        testNamePropertyGet( location, KUALA_LUMPUR );
    }

    @Test( expected = IllegalStateException.class )
    public final void testSetter()
    {
        Location location = createLocation( KUALA_LUMPUR );

        // Must fail!
        Property<String> stringProperty = location.name();
        stringProperty.set( "abc" );
    }

    @Test
    public final void testImmutableEntityProperty()
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            EntityBuilder<LocationEntity> builder = uow.newEntityBuilder( LocationEntity.class );
            builder.instance().name().set( "Rickard" );
            Location location = builder.newInstance();

            try
            {
                location.name().set( "Niclas" );
                Assert.fail( "Should be immutable" );
            }
            catch( IllegalStateException e )
            {
                // Ok
            }
        }
        finally
        {
            uow.discard();
        }
    }

    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( LocationComposite.class );
        module.entities( LocationEntity.class );
        new EntityTestAssembler().assemble( module );
    }

    interface LocationComposite
        extends Location, TransientComposite
    {
    }

    interface LocationEntity
        extends Location, EntityComposite
    {
    }

    interface Location
    {
        @Immutable
        Property<String> name();
    }
}
