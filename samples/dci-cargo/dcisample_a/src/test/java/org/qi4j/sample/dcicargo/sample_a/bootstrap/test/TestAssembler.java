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
package org.qi4j.sample.dcicargo.sample_a.bootstrap.test;

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
import org.qi4j.sample.dcicargo.sample_a.bootstrap.sampledata.BaseDataService;
import org.qi4j.sample.dcicargo.sample_a.context.rolemap.CargoRoleMap;
import org.qi4j.sample.dcicargo.sample_a.context.rolemap.CargosRoleMap;
import org.qi4j.sample.dcicargo.sample_a.context.rolemap.HandlingEventRoleMap;
import org.qi4j.sample.dcicargo.sample_a.context.rolemap.HandlingEventsRoleMap;
import org.qi4j.sample.dcicargo.sample_a.context.rolemap.ItineraryRoleMap;
import org.qi4j.sample.dcicargo.sample_a.context.rolemap.RouteSpecificationRoleMap;
import org.qi4j.sample.dcicargo.sample_a.context.support.ApplicationEvents;
import org.qi4j.sample.dcicargo.sample_a.context.support.RegisterHandlingEventAttemptDTO;
import org.qi4j.sample.dcicargo.sample_a.context.support.RoutingService;
import org.qi4j.sample.dcicargo.sample_a.data.entity.LocationEntity;
import org.qi4j.sample.dcicargo.sample_a.data.entity.VoyageEntity;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.TrackingId;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.ExpectedHandlingEvent;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary.Leg;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.UnLocode;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.CarrierMovement;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Schedule;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.VoyageNumber;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

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
        ModuleAssembly serializationModule = infrastructureLayer.module( "INFRASTRUCTURE-Serialization" );
        serializationModule
            .services( OrgJsonValueSerializationService.class )
            .taggedWith( ValueSerialization.Formats.JSON )
            .setMetaInfo( new Function<Application, Module>()
        {
            @Override
            public Module map( Application application )
            {
                return application.findModule( "CONTEXT", "CONTEXT-ContextSupport" );
            }
        } )
        .visibleIn( application );

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
