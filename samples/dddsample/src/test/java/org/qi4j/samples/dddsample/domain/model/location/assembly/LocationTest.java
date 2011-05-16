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
package org.qi4j.samples.dddsample.domain.model.location.assembly;

import org.junit.Test;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

import static org.junit.Assert.*;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public class LocationTest
    extends AbstractQi4jTest
{
    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new LocationModelAssembler().assemble( module );
        module.services(
            MemoryEntityStoreService.class,
            UuidIdentityGeneratorService.class
        );
    }

    @Test
    public final void testEquals()
    {
        UnitOfWork uow = module.newUnitOfWork();

        try
        {
            // Same UN locode - equal
            assertTrue(
                createLocation( "ATEST", "test-name" ).equals( createLocation( "ATEST", "test-name" ) )
            );

            // Different UN locodes - not equal
            assertFalse(
                createLocation( "ATEST", "test-name" ).equals( createLocation( "TESTB", "test-name" ) )
            );

            // Always equal to itself
            Location location = createLocation( "ATEST", "test-name" );
            assertTrue( location.equals( location ) );

            // Never equal to null
            assertFalse( location.equals( null ) );

            // Special UNKNOWN location is equal to itself
            ServiceReference<LocationRepository> locationRepositoryServiceReference = locationRepositoryService();
            LocationRepository locationRepository = locationRepositoryServiceReference.get();
            Location unknownLocation = locationRepository.unknownLocation();
            assertTrue( unknownLocation.equals( unknownLocation ) );

            try
            {
                createLocation( null, null );
                fail( "Should not allow any null constructor arguments" );
            }
            catch( IllegalArgumentException expected )
            {
            }
        }
        finally
        {
            uow.discard();
        }
    }

    private ServiceReference<LocationRepository> locationRepositoryService()
    {
        return module.findService( LocationRepository.class );
    }

    private Location createLocation( String unlocodeIdString, String locationName )
    {
        ServiceReference<LocationFactoryService> factoryRef =
            module.findService( LocationFactoryService.class );
        LocationFactoryService factory = factoryRef.get();

        UnLocode unLocode = new UnLocode( unlocodeIdString );
        return factory.createLocation( unLocode, locationName );
    }
}
