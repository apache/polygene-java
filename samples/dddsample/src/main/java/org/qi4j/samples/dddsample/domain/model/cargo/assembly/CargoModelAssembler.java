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
package org.qi4j.samples.dddsample.domain.model.cargo.assembly;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spring.importer.SpringImporter;
import org.qi4j.samples.dddsample.application.routing.ExternalRoutingService;
import org.qi4j.samples.dddsample.routingteam.internal.GraphTraversalServiceImpl;

import static org.qi4j.api.common.Visibility.*;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class CargoModelAssembler
    implements Assembler
{
    private final boolean loadSampleData;

    public CargoModelAssembler()
    {
        this( false );
    }

    public CargoModelAssembler( boolean loadSampleData )
    {
        this.loadSampleData = loadSampleData;
    }

    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients(
            RouteSpecificationComposite.class
        );

        module.addEntities(
            CargoEntity.class,
            LegEntity.class,
            ItineraryEntity.class
        ).visibleIn( application );

        module.addServices(
            ExternalRoutingService.class,
            CargoRepositoryService.class,
            BookingService.class
        ).visibleIn( application );

        module.importServices( GraphTraversalServiceImpl.class ).importedBy( SpringImporter.class );

        if( loadSampleData )
        {
            module.addServices( SampleCargoDataBootstrapService.class )
                .visibleIn( layer )
                .instantiateOnStartup();
        }
    }
}
