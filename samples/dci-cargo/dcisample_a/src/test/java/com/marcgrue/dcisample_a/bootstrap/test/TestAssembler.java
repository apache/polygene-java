package com.marcgrue.dcisample_a.bootstrap.test;

import com.marcgrue.dcisample_a.bootstrap.sampledata.BaseDataService;
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
        assembly.setVersion( "A.1.0" );
        assembly.setMode( test );

        // Layers
        LayerAssembly infrastructureLayer = assembly.layer( "INFRASTRUCTURE" );
        LayerAssembly domainLayer = assembly.layer( "DOMAIN" );
        LayerAssembly contextLayer = assembly.layer( "CONTEXT" );
        LayerAssembly bootstrapLayer = assembly.layer( "BOOTSTRAP" );

        // Layer dependencies
        bootstrapLayer.uses(
              contextLayer,
              domainLayer,
              infrastructureLayer );

        contextLayer.uses(
              domainLayer,
              infrastructureLayer );

        domainLayer.uses(
              contextLayer,
              infrastructureLayer
        );

        // Assemble
        assembleDomainLayer( domainLayer );
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
        // Role-playing entities
        ModuleAssembly entityRoleModule = contextLayer.module( "CONTEXT-EntityRole" );
        entityRoleModule
              .entities(
                    CargoRoleMap.class,
                    CargosRoleMap.class,
                    HandlingEventRoleMap.class,
                    HandlingEventsRoleMap.class )
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

    private void assembleDomainLayer( LayerAssembly domainLayer ) throws AssemblyException
    {
        // Non-role-playing entities
        ModuleAssembly entityModule = domainLayer.module( "DOMAIN-Entity" );
        entityModule
              .entities(
                    LocationEntity.class,
                    VoyageEntity.class )
              .visibleIn( application );


        // Non-role-playing values
        ModuleAssembly dataModule = domainLayer.module( "DOMAIN-Data" );
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
