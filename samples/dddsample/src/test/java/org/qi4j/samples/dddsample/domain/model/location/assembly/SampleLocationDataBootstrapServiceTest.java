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

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertNotNull;
import static org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService.LOCATIONS;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class SampleLocationDataBootstrapServiceTest
    extends AbstractQi4jTest
{
    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new LocationModelAssembler( true ).assemble( module );
        module.services(
            MemoryEntityStoreService.class,
            UuidIdentityGeneratorService.class
        );
    }

    @Test
    public final void testSampleLocations()
    {
        LocationRepository locationRepository = module.findService( LocationRepository.class )
            .get();

        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            for( String[] locations : LOCATIONS )
            {
                UnLocode unLocode = new UnLocode( locations[ 0 ] );
                Location location = locationRepository.find( unLocode );
                assertNotNull( location );

                Assert.assertEquals( locations[ 1 ], location.name() );
            }
        }
        finally
        {
            uow.discard();
        }
    }
}
