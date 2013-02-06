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
package org.qi4j.sample.dcicargo.sample_b.bootstrap.test;

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
import org.qi4j.sample.dcicargo.sample_b.bootstrap.sampledata.BaseDataService;
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
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

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

    private void assembleBootstrapLayer( LayerAssembly bootstrapLayer )
        throws AssemblyException
    {
        ModuleAssembly bootstrapModule = bootstrapLayer.module( "BOOTSTRAP-Bootstrap" );

        // Load base data on startup
        bootstrapModule
            .services( BaseDataService.class )
            .visibleIn( application )
            .instantiateOnStartup();
    }

    private void assembleContextLayer( LayerAssembly contextLayer )
        throws AssemblyException
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
//        structureModule.entities( LocationEntity.class, VoyageEntity.class, HandlingEventEntity.class );
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
            .services(
                MemoryRepositoryService.class,
                RdfIndexingEngineService.class )
            .visibleIn( application )
            .instantiateOnStartup();

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
