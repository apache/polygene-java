/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.runtime.property;

import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.property.Immutable;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public final class ImmutablePropertyTest
    extends AbstractPolygeneTest
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
        assertThat( location, notNullValue() );
        assertThat( location.name().get(), equalTo( locationName ) );
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

    @Test
    public final void testSetter()
    {
        assertThrows( IllegalStateException.class, () -> {
            Location location = createLocation( KUALA_LUMPUR );
            // Must fail!
            Property<String> stringProperty = location.name();
            stringProperty.set( "abc" );
        } );
    }

    @Test
    public final void testImmutableEntityProperty()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<LocationEntity> builder = uow.newEntityBuilder( LocationEntity.class );
            builder.instance().name().set( "Rickard" );
            Location location = builder.newInstance();

            try
            {
                location.name().set( "Niclas" );
                fail( "Should be immutable" );
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
