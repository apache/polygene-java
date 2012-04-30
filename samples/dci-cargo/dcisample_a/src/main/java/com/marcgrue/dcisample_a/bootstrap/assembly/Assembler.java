package com.marcgrue.dcisample_a.bootstrap.assembly;

import com.marcgrue.dcisample_a.bootstrap.sampledata.BaseDataService;
import com.marcgrue.dcisample_a.bootstrap.sampledata.SampleDataService;
import com.marcgrue.dcisample_a.communication.query.BookingQueries;
import com.marcgrue.dcisample_a.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_a.communication.query.dto.HandlingEventDTO;
import com.marcgrue.dcisample_a.communication.query.dto.LocationDTO;
import com.marcgrue.dcisample_a.communication.query.dto.VoyageDTO;
import com.marcgrue.dcisample_a.context.rolemap.*;
import com.marcgrue.dcisample_a.context.support.ApplicationEvents;
import com.marcgrue.dcisample_a.context.support.RegisterHandlingEventAttemptDTO;
import com.marcgrue.dcisample_a.context.support.RoutingService;
import com.marcgrue.dcisample_a.data.entity.LocationEntity;
import com.marcgrue.dcisample_a.data.entity.VoyageEntity;
import com.marcgrue.dcisample_a.data.shipping.cargo.TrackingId;
import com.marcgrue.dcisample_a.data.shipping.delivery.Delivery;
import com.marcgrue.dcisample_a.data.shipping.delivery.ExpectedHandlingEvent;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Leg;
import com.marcgrue.dcisample_a.data.shipping.location.UnLocode;
import com.marcgrue.dcisample_a.data.shipping.voyage.CarrierMovement;
import com.marcgrue.dcisample_a.data.shipping.voyage.Schedule;
import com.marcgrue.dcisample_a.data.shipping.voyage.VoyageNumber;
import com.marcgrue.dcisample_a.infrastructure.WicketQi4jApplication;
import com.marcgrue.dcisample_a.infrastructure.conversion.EntityToDTOService;
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
 * Qi4j assembly of the DCI Sample application (version A)
 *
 * A Qi4j application structure is declared by an assembly that defines which layers and modules
 * the application has and how they are allowed to depend on each other. Each layer could have it's
 * own assembly file in larger applications (ead more at http://www.qi4j.org/qi4j/70.html).
 *
 * The Qi4j assembly doesn't follow a strict 1-1 correlation between the directory hierarchy and
 * the assembly structures. An example is the Entities:
 *
 * Entities can be promoted to Role Players when they are needed to play a Role in a Context.
 * One Role Map is created for each Entity and it lists Roles in different Contexts that the Entity
 * can play. It hence has knowledge about the Context layer. If an Entity is in a Role Map it doesn't
 * get assembled as an Entity down in the Data layer but rather up in the CONTEXT-EntityRole module.
 * The Entities left behind without Roles could still have been assembled in a DATA-Entity module but
 * to avoid swapping Entities up and down between the Data and Context layers we have them all in the
 * Context layer. Note that there are still no "physical" upward dependencies from the Entities to
 * layers above.
 *
 * So dependency structure layers (ie. as shown by Structure101) are not the same as Qi4j layers.
 * See more at http://www.qi4j.org/qi4j/70.html
 *
 * TRY THIS: Run VisualizeApplicationStructure to see a cool visualization of the assembly below!
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
        assembly.setName( "DCI Sample (version A)" );
        assembly.setVersion( "A.1.0" );
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

        // Load sample data on startup
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
              .transients(
                    BookingQueries.class )
              .visibleIn( application );

        queryModule
              .values(
                    CargoDTO.class,
                    LocationDTO.class,
                    HandlingEventDTO.class,
                    VoyageDTO.class );

        queryModule
              .addServices(
                    EntityToDTOService.class )
              .visibleIn( application );
    }

    private void assembleContextLayer( LayerAssembly contextLayer ) throws AssemblyException
    {
        // Role-playing entities
        ModuleAssembly entityRoleModule = contextLayer.module( "CONTEXT-EntityRole" );
        entityRoleModule
              .entities(
                    CargoRoleMap.class,
                    CargosRoleMap.class,
                    HandlingEventRoleMap.class,
                    HandlingEventsRoleMap.class )
              .visibleIn( application );

        // Non-role-playing entities
        ModuleAssembly entityNonRoleModule = contextLayer.module( "CONTEXT-EntityNonRole" );
        entityNonRoleModule
              .entities(
                    LocationEntity.class,
                    VoyageEntity.class )
              .visibleIn( application );


        // Role-playing values
        ModuleAssembly valueRoleModule = contextLayer.module( "CONTEXT-ValueRole" );
        valueRoleModule
              .values(
                    ItineraryRoleMap.class,
                    RouteSpecificationRoleMap.class )
              .visibleIn( application );


        ModuleAssembly contextSupportModule = contextLayer.module( "CONTEXT-ContextSupport" );
        contextSupportModule
              .addServices(
                    RoutingService.class,
                    ApplicationEvents.class )
              .visibleIn( application );

        contextSupportModule
              .values(
                    RegisterHandlingEventAttemptDTO.class )
              .visibleIn( application );
    }

    private void assembleDataLayer( LayerAssembly dataLayer ) throws AssemblyException
    {
        // Non-role-playing values
        ModuleAssembly dataModule = dataLayer.module( "DATA-Data" );
        dataModule
              .values(
                    TrackingId.class,
                    Delivery.class,
                    ExpectedHandlingEvent.class,
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
              .addServices(
                    MemoryRepositoryService.class,
                    RdfIndexingEngineService.class )
              .instantiateOnStartup()
              .visibleIn( application );


        ModuleAssembly entityStoreModule = infrastructureLayer.module( "INFRASTRUCTURE-EntityStore" );
        entityStoreModule
              .addServices(
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
