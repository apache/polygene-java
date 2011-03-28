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
package org.qi4j.samples.dddsample.bootstrap;

import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.performance.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.library.spring.bootstrap.Qi4jApplicationBootstrap;
import org.qi4j.samples.dddsample.application.messaging.MessagingAssembler;
import org.qi4j.samples.dddsample.application.remoting.assembly.RemotingAssembler;
import org.qi4j.samples.dddsample.domain.model.cargo.assembly.CargoModelAssembler;
import org.qi4j.samples.dddsample.domain.model.carrier.assembly.CarrierMovementModelAssembler;
import org.qi4j.samples.dddsample.domain.model.handling.assembly.HandlingEventModelAssembler;
import org.qi4j.samples.dddsample.domain.model.location.assembly.LocationModelAssembler;
import org.qi4j.samples.dddsample.domain.service.assembly.HandlingServiceAssembler;
import org.qi4j.samples.dddsample.domain.service.assembly.TrackingServiceAssembler;
import org.qi4j.samples.dddsample.spring.assembly.SpringModuleAssembler;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

import static org.qi4j.api.common.Visibility.*;

/**
 * @author edward.yakop@gmail.com
 */
public class Qi4jDDDSampleApplicationBootstrap
    extends Qi4jApplicationBootstrap
{
    private static final boolean LOAD_SAMPLE_DATA = true;

    private static final String LAYER_INFRASTRUCTURE = "Infrastructure";
    private static final String MODULE_PERSISTENCE = "Persistence";

    private static final String LAYER_DOMAIN = "Domain";
    private static final String MODULE_CARGO = "Cargo";
    private static final String MODULE_CARRIER = "Carrier";
    private static final String MODULE_HANDLING = "Handling";
    private static final String MODULE_LOCATION = "Location";
    private static final String MODULE_TRACKING = "Tracking";
    private static final String MODULE_MESSAGING = "Messaging";

    private static final String LAYER_APPLICATION = "Application";
    private static final String MODULE_SPRING = "Spring";
    private static final String MODULE_REMOTING = "Remoting";

    public final void assemble( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        applicationAssembly.setName( "Cargo sample DDD application" );

        // Infrastructure
        LayerAssembly infrastructureLayer = createInfrastructureLayer( applicationAssembly );

        // Domain layer
        LayerAssembly domainLayer = createDomainLayer( applicationAssembly );
        domainLayer.uses( infrastructureLayer );

        // Application layer
        LayerAssembly applicationLayer = createApplicationLayer( applicationAssembly );
        applicationLayer.uses( domainLayer );
        applicationLayer.uses( infrastructureLayer );
    }

    private LayerAssembly createInfrastructureLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly infrastructureLayer = applicationAssembly.layer( LAYER_INFRASTRUCTURE );

        // Persistence module
        ModuleAssembly module = infrastructureLayer.module( MODULE_PERSISTENCE );

        // Indexing
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
        module.services( MemoryRepositoryService.class )
            .identifiedBy( "rdf-repository" )
            .instantiateOnStartup();

        module.services( RdfIndexingEngineService.class )
            .visibleIn( application )
            .instantiateOnStartup();

        // Entity store
        module.services( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class )
            .visibleIn( application )
            .instantiateOnStartup();

        return infrastructureLayer;
    }

    private LayerAssembly createDomainLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly modelLayer = applicationAssembly.layer( LAYER_DOMAIN );

        ModuleAssembly locationModule = modelLayer.module( MODULE_LOCATION );
        new LocationModelAssembler( LOAD_SAMPLE_DATA ).assemble( locationModule );

        ModuleAssembly cargoModule = modelLayer.module( MODULE_CARGO );
        new CargoModelAssembler( LOAD_SAMPLE_DATA ).assemble( cargoModule );

        ModuleAssembly carrierModule = modelLayer.module( MODULE_CARRIER );
        new CarrierMovementModelAssembler( LOAD_SAMPLE_DATA ).assemble( carrierModule );

        ModuleAssembly handlingModule = modelLayer.module( MODULE_HANDLING );
        new HandlingEventModelAssembler( LOAD_SAMPLE_DATA ).assemble( handlingModule );
        new HandlingServiceAssembler().assemble( handlingModule );

        ModuleAssembly trackingModule = modelLayer.module( MODULE_TRACKING );
        new TrackingServiceAssembler().assemble( trackingModule );

        ModuleAssembly messagingModule = modelLayer.module( MODULE_MESSAGING );
        new MessagingAssembler().assemble( messagingModule );

        return modelLayer;
    }

    private LayerAssembly createApplicationLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly applicationLayer = applicationAssembly.layer( LAYER_APPLICATION );

        ModuleAssembly remotingModule = applicationLayer.module( MODULE_REMOTING );
        new RemotingAssembler().assemble( remotingModule );

        ModuleAssembly springModule = applicationLayer.module( MODULE_SPRING );
        new SpringModuleAssembler().assemble( springModule );

        return applicationLayer;
    }
}
