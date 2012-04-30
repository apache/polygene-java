package com.marcgrue.dcisample_b.bootstrap.assembly;

import com.marcgrue.dcisample_b.bootstrap.sampledata.BaseDataService;
import com.marcgrue.dcisample_b.bootstrap.sampledata.SampleDataService;
import com.marcgrue.dcisample_b.communication.query.BookingQueries;
import com.marcgrue.dcisample_b.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_b.communication.query.dto.HandlingEventDTO;
import com.marcgrue.dcisample_b.communication.query.dto.LocationDTO;
import com.marcgrue.dcisample_b.communication.query.dto.VoyageDTO;
import com.marcgrue.dcisample_b.context.interaction.handling.ProcessHandlingEvent;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.ParseHandlingEventData;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import com.marcgrue.dcisample_b.context.rolemap.CargoRoleMap;
import com.marcgrue.dcisample_b.context.rolemap.CargosRoleMap;
import com.marcgrue.dcisample_b.context.rolemap.HandlingEventsRoleMap;
import com.marcgrue.dcisample_b.context.service.routing.RoutingService;
import com.marcgrue.dcisample_b.data.entity.HandlingEventEntity;
import com.marcgrue.dcisample_b.data.entity.LocationEntity;
import com.marcgrue.dcisample_b.data.entity.VoyageEntity;
import com.marcgrue.dcisample_b.data.factory.RouteSpecificationFactoryService;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.delivery.NextHandlingEvent;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import com.marcgrue.dcisample_b.data.structure.itinerary.Leg;
import com.marcgrue.dcisample_b.data.structure.location.UnLocode;
import com.marcgrue.dcisample_b.data.structure.tracking.TrackingId;
import com.marcgrue.dcisample_b.data.structure.voyage.CarrierMovement;
import com.marcgrue.dcisample_b.data.structure.voyage.Schedule;
import com.marcgrue.dcisample_b.data.structure.voyage.VoyageNumber;
import com.marcgrue.dcisample_b.infrastructure.WicketQi4jApplication;
import com.marcgrue.dcisample_b.infrastructure.conversion.EntityToDTOService;
import org.qi4j.bootstrap.*;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import pathfinder.api.GraphTraversalService;
import pathfinder.internal.GraphDAO;
import pathfinder.internal.GraphTraversalServiceImpl;

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

    private void assembleBootstrapLayer( LayerAssembly bootstrapLayer ) throws AssemblyException
    {
        ModuleAssembly bootstrapModule = bootstrapLayer.module( "BOOTSTRAP-Bootstrap" );
        bootstrapModule
              .objects(
                    WicketQi4jApplication.class );

        bootstrapModule
              .addServices(
                    BaseDataService.class );

        bootstrapModule
              .addServices(
                    SampleDataService.class )
              .instantiateOnStartup();
    }

    private void assembleCommunicationLayer( LayerAssembly communicationLayer ) throws AssemblyException
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
                    EntityToDTOService.class )
              .visibleIn( application );
    }

    private void assembleContextLayer( LayerAssembly contextLayer ) throws AssemblyException
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

    private void assembleDataLayer( LayerAssembly dataLayer ) throws AssemblyException
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

    private void assembleInfrastructureLayer( LayerAssembly infrastructureLayer ) throws AssemblyException
    {
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
