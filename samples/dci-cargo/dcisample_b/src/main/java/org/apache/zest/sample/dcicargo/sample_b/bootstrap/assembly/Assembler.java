/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.sample.dcicargo.sample_b.bootstrap.assembly;

import java.util.function.Function;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.ApplicationAssembler;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.index.rdf.RdfIndexingEngineService;
import org.apache.zest.library.rdf.entity.EntityStateSerializer;
import org.apache.zest.library.rdf.entity.EntityTypeSerializer;
import org.apache.zest.library.rdf.repository.MemoryRepositoryService;
import org.apache.zest.sample.dcicargo.pathfinder_b.api.GraphTraversalService;
import org.apache.zest.sample.dcicargo.pathfinder_b.internal.GraphDAO;
import org.apache.zest.sample.dcicargo.pathfinder_b.internal.GraphTraversalServiceImpl;
import org.apache.zest.sample.dcicargo.sample_b.bootstrap.DCISampleApplication_b;
import org.apache.zest.sample.dcicargo.sample_b.bootstrap.sampledata.BaseDataService;
import org.apache.zest.sample.dcicargo.sample_b.bootstrap.sampledata.SampleDataService;
import org.apache.zest.sample.dcicargo.sample_b.communication.query.BookingQueries;
import org.apache.zest.sample.dcicargo.sample_b.communication.query.dto.CargoDTO;
import org.apache.zest.sample.dcicargo.sample_b.communication.query.dto.HandlingEventDTO;
import org.apache.zest.sample.dcicargo.sample_b.communication.query.dto.LocationDTO;
import org.apache.zest.sample.dcicargo.sample_b.communication.query.dto.VoyageDTO;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.ProcessHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.parsing.ParseHandlingEventData;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import org.apache.zest.sample.dcicargo.sample_b.context.rolemap.CargoRoleMap;
import org.apache.zest.sample.dcicargo.sample_b.context.rolemap.CargosRoleMap;
import org.apache.zest.sample.dcicargo.sample_b.context.rolemap.HandlingEventsRoleMap;
import org.apache.zest.sample.dcicargo.sample_b.context.service.routing.RoutingService;
import org.apache.zest.sample.dcicargo.sample_b.data.entity.HandlingEventEntity;
import org.apache.zest.sample.dcicargo.sample_b.data.entity.LocationEntity;
import org.apache.zest.sample.dcicargo.sample_b.data.entity.VoyageEntity;
import org.apache.zest.sample.dcicargo.sample_b.data.factory.RouteSpecificationFactoryService;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.itinerary.Leg;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.location.UnLocode;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.tracking.TrackingId;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.Schedule;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.VoyageNumber;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.conversion.EntityToDTOService;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.apache.zest.api.common.Visibility.application;
import static org.apache.zest.api.structure.Application.Mode.development;

/**
 * Zest assembly of the DCI Sample application (version B)
 *
 * A Zest application structure is declared by an assembly that defines which layers and modules
 * the application has and how they are allowed to depend on each other. Each layer could have it's
 * own assembly file in larger applications.
 *
 * The Zest assembly doesn't strictly map 1-1 to the directory hierarchy and the assembly
 * structures. An example is the Entities:
 *
 * Data objects (Entities and ValuesComposites) can be promoted to Role Players when they are
 * needed to play a Role in a Context. One Role Map is created for each Data object and it lists
 * Roles in different Contexts that the object can play. It then has knowledge about the Context
 * layer and therefore goes into the CONTEXT-RoleMap module.
 *
 * All other Date objects are candidates to play a role if needed by some context and they "stand by"
 * in the CONTEXT-RoleMapCandidates module. Note that there are still no "physical" upward dependencies
 * from the data package (containing entities and values) to layers above.
 *
 * TRY THIS:
 * Run VisualizeApplicationStructure to see a cool visualization of the assembly!
 */
@SuppressWarnings( "unchecked" )
public class Assembler
    implements ApplicationAssembler
{
    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        // Application assembly
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setName( "DCI Sample (version B)" );
        assembly.setVersion( "B.1.0" );
        assembly.setMode( development );

        // Layers (adding bottom-up - will be assembled in this order)
        LayerAssembly infrastructureLayer = assembly.layer( "INFRASTRUCTURE" );
        LayerAssembly dataLayer = assembly.layer( "DATA" );
        LayerAssembly contextLayer = assembly.layer( "CONTEXT" );
        LayerAssembly communicationLayer = assembly.layer( "COMMUNICATION" );
        LayerAssembly bootstrapLayer = assembly.layer( "BOOTSTRAP" );

        // Layer dependencies
        bootstrapLayer.uses(
            communicationLayer,
            contextLayer,
            dataLayer,
            infrastructureLayer );

        communicationLayer.uses(
            contextLayer,
            dataLayer,
            infrastructureLayer );

        contextLayer.uses(
            dataLayer,
            infrastructureLayer );

        dataLayer.uses(
            infrastructureLayer
        );

        // Assemble
        assembleBootstrapLayer( bootstrapLayer );
        assembleCommunicationLayer( communicationLayer );
        assembleContextLayer( contextLayer );
        assembleDataLayer( dataLayer );
        assembleInfrastructureLayer( infrastructureLayer );

        return assembly;
    }

    private void assembleBootstrapLayer( LayerAssembly bootstrapLayer )
        throws AssemblyException
    {
        ModuleAssembly bootstrapModule = bootstrapLayer.module( "BOOTSTRAP-Bootstrap" );
        bootstrapModule
            .objects(
                DCISampleApplication_b.class );

        bootstrapModule
            .addServices(
                BaseDataService.class )
            .instantiateOnStartup();

        bootstrapModule
            .addServices(
                SampleDataService.class )
            .instantiateOnStartup();
    }

    private void assembleCommunicationLayer( LayerAssembly communicationLayer )
        throws AssemblyException
    {
        ModuleAssembly queryModule = communicationLayer.module( "COMMUNICATION-Query" );
        queryModule
            .values(
                CargoDTO.class,
                LocationDTO.class,
                HandlingEventDTO.class,
                VoyageDTO.class );

        queryModule
            .transients(
                BookingQueries.class )
            .visibleIn( application );

        queryModule
            .addServices(
                EntityToDTOService.class,
                OrgJsonValueSerializationService.class )
            .visibleIn( application );
    }

    private void assembleContextLayer( LayerAssembly contextLayer )
        throws AssemblyException
    {
        ModuleAssembly roleMapModule = contextLayer.module( "CONTEXT-RoleMap" );
        roleMapModule
            .entities(
                CargoRoleMap.class,
                CargosRoleMap.class,
                HandlingEventsRoleMap.class )
            .visibleIn( application );

        ModuleAssembly roleMapCandidatesModule = contextLayer.module( "CONTEXT-RoleMapCandidates" );
        roleMapCandidatesModule
            .entities(
                HandlingEventEntity.class,
                LocationEntity.class,
                VoyageEntity.class )
            .visibleIn( application );

        roleMapCandidatesModule
            .values(
                Itinerary.class )
            .visibleIn( application );

        ModuleAssembly interactionModule = contextLayer.module( "CONTEXT-Interaction" );
        interactionModule
            .transients(
                ProcessHandlingEvent.class )
            .visibleIn( application );

        ModuleAssembly contextServiceModule = contextLayer.module( "CONTEXT-Service" );
        contextServiceModule
            .addServices(
                ParseHandlingEventData.class,
                RoutingService.class,
                RouteSpecificationFactoryService.class )
            .visibleIn( application );

        contextServiceModule
            .values(
                ParsedHandlingEventData.class )
            .visibleIn( application );
    }

    private void assembleDataLayer( LayerAssembly dataLayer )
        throws AssemblyException
    {
        ModuleAssembly dataModule = dataLayer.module( "DATA-Structure" );
        dataModule
            .values(
                TrackingId.class,
                RouteSpecification.class,
                Delivery.class,
                NextHandlingEvent.class,
                UnLocode.class,
                Leg.class,
                CarrierMovement.class,
                Schedule.class,
                VoyageNumber.class )
            .visibleIn( application );
    }

    private void assembleInfrastructureLayer( LayerAssembly infrastructureLayer )
        throws AssemblyException
    {
        ModuleAssembly serializationModule = infrastructureLayer.module( "INFRASTRUCTURE-Serialization" );
        serializationModule
            .services( OrgJsonValueSerializationService.class )
            .taggedWith( ValueSerialization.Formats.JSON )
            .setMetaInfo( (Function<Application, Module>) application -> application.findModule( "CONTEXT", "CONTEXT-RoleMap" ) )
        .visibleIn( application );

        ModuleAssembly indexingModule = infrastructureLayer.module( "INFRASTRUCTURE-Indexing" );
        indexingModule
            .objects(
                EntityStateSerializer.class,
                EntityTypeSerializer.class );

        indexingModule
            .services(
                MemoryRepositoryService.class,
                RdfIndexingEngineService.class )
            .instantiateOnStartup()
            .visibleIn( application );

        ModuleAssembly entityStoreModule = infrastructureLayer.module( "INFRASTRUCTURE-EntityStore" );
        entityStoreModule
            .services(
                MemoryEntityStoreService.class,
                UuidIdentityGeneratorService.class )
            .instantiateOnStartup()
            .visibleIn( application );

        ModuleAssembly externalServiceModule = infrastructureLayer.module( "INFRASTRUCTURE-ExternalService" );
        externalServiceModule
            .importedServices(
                GraphTraversalService.class )
            .setMetaInfo( new GraphTraversalServiceImpl( new GraphDAO() ) )
            .visibleIn( application );
    }
}
