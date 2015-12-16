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
package org.apache.zest.sample.dcicargo.sample_b.bootstrap.test;

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
import org.apache.zest.sample.dcicargo.sample_b.bootstrap.sampledata.BaseDataService;
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
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.apache.zest.api.common.Visibility.application;
import static org.apache.zest.api.structure.Application.Mode.test;

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

    private void assembleBootstrapLayer( LayerAssembly bootstrapLayer )
        throws AssemblyException
    {
        ModuleAssembly bootstrapModule = bootstrapLayer.module( "BOOTSTRAP-Bootstrap" ).withDefaultUnitOfWorkFactory();

        // Load base data on startup
        bootstrapModule
            .services( BaseDataService.class )
            .visibleIn( application )
            .instantiateOnStartup();
    }

    private void assembleContextLayer( LayerAssembly contextLayer )
        throws AssemblyException
    {
        ModuleAssembly roleMapModule = contextLayer.module( "CONTEXT-RoleMap" ).withDefaultUnitOfWorkFactory();

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

        ModuleAssembly interactionModule = contextLayer.module( "CONTEXT-Interaction" ).withDefaultUnitOfWorkFactory();
        interactionModule
            .transients(
                ProcessHandlingEvent.class )
            .visibleIn( application );

        ModuleAssembly contextServiceModule = contextLayer.module( "CONTEXT-Service" ).withDefaultUnitOfWorkFactory();
        contextServiceModule
            .services(
                ParseHandlingEventData.class,
                RoutingService.class,
                RouteSpecificationFactoryService.class )
            .visibleIn( application );

        contextServiceModule
            .values(
                ParsedHandlingEventData.class )
            .visibleIn( application );
    }

    private void assembleDomainLayer( LayerAssembly dataLayer )
        throws AssemblyException
    {
        // Non-role-playing values
        ModuleAssembly structureModule = dataLayer.module( "DATA-Structure" ).withDefaultUnitOfWorkFactory();
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
//        structureModule.entities( LocationEntity.class, VoyageEntity.class, HandlingEventEntity.class );
    }

    private void assembleInfrastructureLayer( LayerAssembly infrastructureLayer )
        throws AssemblyException
    {
        ModuleAssembly serializationModule = infrastructureLayer.module( "INFRASTRUCTURE-Serialization" ).withDefaultUnitOfWorkFactory();
        serializationModule
            .services( OrgJsonValueSerializationService.class )
            .taggedWith( ValueSerialization.Formats.JSON )
            .setMetaInfo( (Function<Application, Module>) application -> application.findModule( "CONTEXT", "CONTEXT-RoleMap" ) )
        .visibleIn( application );

        ModuleAssembly indexingModule = infrastructureLayer.module( "INFRASTRUCTURE-Indexing" ).withDefaultUnitOfWorkFactory();
        indexingModule
            .objects(
                EntityStateSerializer.class,
                EntityTypeSerializer.class );

        indexingModule
            .services(
                MemoryRepositoryService.class,
                RdfIndexingEngineService.class )
            .visibleIn( application )
            .instantiateOnStartup();

        ModuleAssembly entityStoreModule = infrastructureLayer.module( "INFRASTRUCTURE-EntityStore" ).withDefaultUnitOfWorkFactory();
        entityStoreModule
            .addServices(
                MemoryEntityStoreService.class,
                UuidIdentityGeneratorService.class )
            .visibleIn( application );

        ModuleAssembly externalServiceModule = infrastructureLayer.module( "INFRASTRUCTURE-ExternalService" ).withDefaultUnitOfWorkFactory();
        externalServiceModule
            .importedServices(
                GraphTraversalService.class )
            .setMetaInfo( new GraphTraversalServiceImpl( new GraphDAO() ) )
            .visibleIn( application );
    }
}
