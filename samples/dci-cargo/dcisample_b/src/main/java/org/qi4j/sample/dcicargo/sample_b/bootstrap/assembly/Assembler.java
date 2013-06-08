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
package org.qi4j.sample.dcicargo.sample_b.bootstrap.assembly;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.functional.Function;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.sample.dcicargo.pathfinder.api.GraphTraversalService;
import org.qi4j.sample.dcicargo.pathfinder.internal.GraphDAO;
import org.qi4j.sample.dcicargo.pathfinder.internal.GraphTraversalServiceImpl;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.DCISampleApplication_b;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.sampledata.BaseDataService;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.sampledata.SampleDataService;
import org.qi4j.sample.dcicargo.sample_b.communication.query.BookingQueries;
import org.qi4j.sample.dcicargo.sample_b.communication.query.dto.CargoDTO;
import org.qi4j.sample.dcicargo.sample_b.communication.query.dto.HandlingEventDTO;
import org.qi4j.sample.dcicargo.sample_b.communication.query.dto.LocationDTO;
import org.qi4j.sample.dcicargo.sample_b.communication.query.dto.VoyageDTO;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.ProcessHandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.parsing.ParseHandlingEventData;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import org.qi4j.sample.dcicargo.sample_b.context.rolemap.CargoRoleMap;
import org.qi4j.sample.dcicargo.sample_b.context.rolemap.CargosRoleMap;
import org.qi4j.sample.dcicargo.sample_b.context.rolemap.HandlingEventsRoleMap;
import org.qi4j.sample.dcicargo.sample_b.context.service.routing.RoutingService;
import org.qi4j.sample.dcicargo.sample_b.data.entity.HandlingEventEntity;
import org.qi4j.sample.dcicargo.sample_b.data.entity.LocationEntity;
import org.qi4j.sample.dcicargo.sample_b.data.entity.VoyageEntity;
import org.qi4j.sample.dcicargo.sample_b.data.factory.RouteSpecificationFactoryService;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Leg;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.UnLocode;
import org.qi4j.sample.dcicargo.sample_b.data.structure.tracking.TrackingId;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Schedule;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.VoyageNumber;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.conversion.EntityToDTOService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.structure.Application.Mode.development;

/**
 * Qi4j assembly of the DCI Sample application (version B)
 *
 * A Qi4j application structure is declared by an assembly that defines which layers and modules
 * the application has and how they are allowed to depend on each other. Each layer could have it's
 * own assembly file in larger applications.
 *
 * The Qi4j assembly doesn't strictly map 1-1 to the directory hierarchy and the assembly
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
 * See more at http://www.qi4j.org/qi4j/70.html
 *
 * TRY THIS:
 * Run VisualizeApplicationStructure to see a cool visualization of the assembly!
 */
@SuppressWarnings( "unchecked" )
public class Assembler
    implements ApplicationAssembler
{
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
            .setMetaInfo( new Function<Application, Module>()
        {
            @Override
            public Module map( Application application )
            {
                return application.findModule( "CONTEXT", "CONTEXT-RoleMap" );
            }
        } )
        .visibleIn( application );

        ModuleAssembly indexingModule = infrastructureLayer.module( "INFRASTRUCTURE-Indexing" );
        indexingModule
            .objects(
                EntityStateSerializer.class,
                EntityTypeSerializer.class );

        indexingModule
            .services( OrgJsonValueSerializationService.class );

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
