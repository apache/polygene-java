package com.marcgrue.dcisample_b.bootstrap.test;

import com.marcgrue.dcisample_b.bootstrap.sampledata.BaseDataService;
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
import static org.qi4j.api.structure.Application.Mode.test;

/**
 * Test application assembler
 *
 * (Has no communication layer)
 */
@SuppressWarnings( "unchecked" )
public class TestAssembler
      implements ApplicationAssembler
{
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
          throws AssemblyException
    {
        // Application assembly
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setName( "DCI Sample (version A) - TEST" );
        assembly.setVersion( "B.1.0" );
        assembly.setMode( test );

        // Layers
        LayerAssembly infrastructureLayer = assembly.layer( "INFRASTRUCTURE" );
        LayerAssembly dataLayer = assembly.layer( "DATA" );
        LayerAssembly contextLayer = assembly.layer( "CONTEXT" );
        LayerAssembly bootstrapLayer = assembly.layer( "BOOTSTRAP" );

        // Layer dependencies
        bootstrapLayer.uses(
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
        assembleDomainLayer( dataLayer );
        assembleContextLayer( contextLayer );
        assembleBootstrapLayer( bootstrapLayer );
        assembleInfrastructureLayer( infrastructureLayer );

        return assembly;
    }

    private void assembleBootstrapLayer( LayerAssembly bootstrapLayer ) throws AssemblyException
    {
        ModuleAssembly bootstrapModule = bootstrapLayer.module( "BOOTSTRAP-Bootstrap" );

        // Load base data on startup
        bootstrapModule
              .addServices(
                    BaseDataService.class )
              .visibleIn( application )
              .instantiateOnStartup();
    }

    private void assembleContextLayer( LayerAssembly contextLayer ) throws AssemblyException
    {
        ModuleAssembly roleMapModule = contextLayer.module( "CONTEXT-RoleMap" );

        // Role-playing entities
        roleMapModule
              .entities(
                    CargoRoleMap.class,
                    CargosRoleMap.class,
                    HandlingEventsRoleMap.class )
              .visibleIn( application );

        // Non-role-playing entities
        roleMapModule
              .entities(
                    HandlingEventEntity.class,
                    LocationEntity.class,
                    VoyageEntity.class )
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

    private void assembleDomainLayer( LayerAssembly dataLayer ) throws AssemblyException
    {
        // Non-role-playing values
        ModuleAssembly structureModule = dataLayer.module( "DATA-Structure" );
        structureModule
              .values(
                    TrackingId.class,
                    RouteSpecification.class,
                    Delivery.class,
                    NextHandlingEvent.class,
                    UnLocode.class,
                    Itinerary.class,
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
              .visibleIn( application );


        ModuleAssembly entityStoreModule = infrastructureLayer.module( "INFRASTRUCTURE-EntityStore" );
        entityStoreModule
              .addServices(
                    MemoryEntityStoreService.class,
                    UuidIdentityGeneratorService.class )
              .visibleIn( application );


        ModuleAssembly externalServiceModule = infrastructureLayer.module( "INFRASTRUCTURE-ExternalService" );
        externalServiceModule
              .importedServices(
                    GraphTraversalService.class )
              .setMetaInfo( new GraphTraversalServiceImpl( new GraphDAO() ) )
              .visibleIn( application );
    }
}
