/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_b.bootstrap.sampledata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryBuilderFactory;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.booking.BookNewCargo;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.booking.routing.AssignCargoToRoute;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.ProcessHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectUnhandledCargo;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import org.apache.zest.sample.dcicargo.sample_b.context.service.routing.RoutingService;
import org.apache.zest.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.apache.zest.sample.dcicargo.sample_b.data.factory.RouteSpecificationFactoryService;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.location.Location;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.Voyage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.zest.api.usecase.UsecaseBuilder.newUsecase;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CLAIM;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.CUSTOMS;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.LOAD;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;
import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.UNLOAD;
import static org.apache.zest.sample.dcicargo.sample_b.infrastructure.dci.Context.prepareContextBaseClass;

/**
 * Create sample cargos in different delivery stages.
 *
 * Add more cases if needed in the loop below.
 */
@Mixins( SampleDataService.Mixin.class )
@Activators( SampleDataService.Activator.class )
public interface SampleDataService
    extends ServiceComposite
{
    void insertSampleData()
        throws Exception;

    class Activator
        extends ActivatorAdapter<ServiceReference<SampleDataService>>
    {

        @Override
        public void afterActivation( ServiceReference<SampleDataService> activated )
            throws Exception
        {
            activated.get().insertSampleData();
        }
    }

    abstract class Mixin
        implements SampleDataService
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        QueryBuilderFactory qbf;

        @Structure
        TransientBuilderFactory tbf;

        @Service
        RoutingService routingService;

        @Service
        BaseDataService baseDataService;

        @Service
        RouteSpecificationFactoryService routeSpecFactory;

        private static final Logger logger = LoggerFactory.getLogger( SampleDataService.class );

        @Override
        public void insertSampleData()
            throws Exception
        {
            prepareContextBaseClass( uowf, vbf );

            logger.info( "######  CREATING SAMPLE DATA...  ##########################################" );

            // Create cargos
            populateRandomCargos( 12 );

            // Handle cargos
            UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "Create sample data" ) );
            try
            {
                int i = 11; // starting at 11 for sortable tracking id prefix in lists
                QueryBuilder<Cargo> qb = qbf.newQueryBuilder( Cargo.class );
                for( Cargo cargo : uow.newQuery( qb ) )

                {
                    final String trackingId = cargo.trackingId().get().id().get();
                    final RouteSpecification routeSpec = cargo.routeSpecification().get();
                    routeSpec.print();

                    NextHandlingEvent nextEvent = null;
                    LocalDate date = null;
                    String port = null;
                    String voyageNumber = null;
                    Voyage voyage;
                    HandlingEventType type = null;
                    String wrongPort = null;
                    String wrongVoyage = null;

                    // First cargo with id 11 is not routed

                    // ROUTE
                    if( i > 11 )
                    {
                        final List<Itinerary> routes = routingService.fetchRoutesForSpecification( routeSpec );
                        final Itinerary itinerary = routes.get( 0 );
                        new AssignCargoToRoute( cargo, itinerary ).assign();
                    }

                    // MISROUTE: Route specification not satisfied with itinerary
                    if( i == 12 )
                    {
                        Location origin = routeSpec.origin().get();
                        Location dest = routeSpec.destination().get();
                        Location badDest = null;
                        Query<Location> locations = uow.newQuery( qbf.newQueryBuilder( Location.class ) );
                        for( Location loc : locations )
                        {
                            if( !origin.equals( loc ) && !dest.equals( loc ) )
                            {
                                badDest = loc;
                                break;
                            }
                        }

                        final RouteSpecification unsatisfiedRouteSpec =
                            routeSpecFactory.build( origin, badDest, LocalDate.now(), LocalDate.now().plusDays( 25 ) );
                        cargo.routeSpecification().set( unsatisfiedRouteSpec );

                        new InspectUnhandledCargo( cargo ).inspect();
                    }

                    // RECEIVE
                    if( i > 13 )
                    {
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        port = nextEvent.location().get().getCode();
                        final LocalDate mockTime = LocalDate.now();
                        registerEvent( mockTime, mockTime, trackingId, RECEIVE, port, null );
                    }

                    // MISDIRECT: LOAD onto wrong carrier
                    if( i == 15 )
                    {
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        date = nextEvent.date().get();
                        port = nextEvent.location().get().getCode();
                        voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();

                        // Find earliest wrong carrier movement (voyage) with same departure location
                        final Query<Voyage> voyages = uowf.currentUnitOfWork()
                            .newQuery( qbf.newQueryBuilder( Voyage.class ) );
                        int depth = 0;
                        do
                        {
                            for( Voyage voy : voyages )
                            {
                                if( voy.voyageNumber().get().number().get().equals( voyageNumber ) )
                                {
                                    continue;
                                }

                                if( depth >= voy.schedule().get().carrierMovements().get().size() )
                                {
                                    continue;
                                }

                                // Carrier movement at current depth
                                final CarrierMovement movement = voy.schedule()
                                    .get()
                                    .carrierMovements()
                                    .get()
                                    .get( depth );
                                final boolean goingFromSamePort = movement.departureLocation()
                                    .get()
                                    .getCode()
                                    .equals( port );
                                final boolean notGoingToDestination = !movement.arrivalLocation()
                                    .get()
                                    .equals( routeSpec.destination().get() );

                                if( goingFromSamePort && notGoingToDestination )
                                {
                                    wrongVoyage = voy.voyageNumber().get().number().get();
                                    break;
                                }
                            }
                        }
                        while( wrongVoyage == null && depth++ < 10 );

                        registerEvent( date, date, trackingId, LOAD, port, wrongVoyage );
                    }

                    // LOAD
                    if( i > 15 )
                    {
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        date = nextEvent.date().get();
                        port = nextEvent.location().get().getCode();
                        voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();
                        registerEvent( date, date, trackingId, LOAD, port, voyageNumber );

                        // Cargo is now on board carrier
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        date = nextEvent.date().get();
                        type = nextEvent.handlingEventType().get();
                        port = nextEvent.location().get().getCode();
                        voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();
                    }

                    // MISDIRECT: UNLOAD from carrier in wrong location
                    if( i == 17 )
                    {
                        voyage = uow.get( Voyage.class, voyageNumber );
                        for( CarrierMovement movement : voyage.schedule().get().carrierMovements().get() )
                        {
                            final String arrivalPort = movement.arrivalLocation().get().getCode();

                            // Take first voyage with different arrival location
                            if( !arrivalPort.equals( port ) )
                            {
                                wrongPort = movement.arrivalLocation().get().unLocode().get().code().get();
                                break;
                            }
                        }
                        registerEvent( date, date, trackingId, UNLOAD, wrongPort, voyageNumber );
                    }

                    // UNLOAD
                    if( i > 17 )
                    {
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        date = nextEvent.date().get();
                        port = nextEvent.location().get().getCode();
                        voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();
                        registerEvent( date, date, trackingId, UNLOAD, port, voyageNumber );

                        // Cargo is now in midpoint location
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        date = nextEvent.date().get();
                        type = nextEvent.handlingEventType().get();
                        port = nextEvent.location().get().getCode();
                    }

                    // CUSTOMS: Customs handling in midpoint location (doesn't affect misdirection status)
                    if( i == 19 )
                    {
                        registerEvent( date, date, trackingId, CUSTOMS, port, null );
                    }

                    // MISDIRECT: Unexpected claim before reaching destination
                    if( i == 20 )
                    {
                        registerEvent( date, date, trackingId, CLAIM, port, null );
                    }

                    // Complete all LOAD/UNLOADS
                    if( i > 20 )
                    {
                        do
                        {
                            //noinspection ConstantConditions
                            voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();
                            registerEvent( date, date, trackingId, type, port, voyageNumber );

                            nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                            date = nextEvent.date().get();
                            port = nextEvent.location().get().getCode();
                            type = nextEvent.handlingEventType().get();
                        }
                        while( type != HandlingEventType.CLAIM );
                    }

                    // CLAIM at destination - this ends the life cycle of the cargo delivery
                    if( i == 22 )
                    {
                        registerEvent( date, date, trackingId, CLAIM, port, null );
                    }

                    // Add more cases if needed...
                    i++;
                }

                uow.complete();
            }
            catch( Exception e )
            {
                uow.discard();
                logger.error( "Problem handling cargos: " + e.getMessage() );
                throw e;
            }

            logger.info( "######  SAMPLE DATA CREATED  ##############################################" );
        }

        private void populateRandomCargos( int numberOfCargos )
        {
            Usecase usecase = UsecaseBuilder.newUsecase( "Populate Random Cargos" );
            UnitOfWork uow = uowf.newUnitOfWork( usecase );

            CargoAggregateRoot cargos = uow.get( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID );

            Query<Location> allLocations = uow.newQuery( qbf.newQueryBuilder( Location.class ) );
            int locationSize = (int) allLocations.count();

            // Make array for selection of location with random index
            final List<Location> locationList = new ArrayList<>();
            for( Location location : allLocations )
            {
                locationList.add( location );
            }

            Location origin;
            Location destination;
            Random random = new Random();
            LocalDate deadline;
            String uuid;
            String id;
            try
            {
                for( int i = 0; i < numberOfCargos; i++ )
                {
                    origin = locationList.get( random.nextInt( locationSize ) );

                    // Find destination different from origin
                    do
                    {
                        destination = locationList.get( random.nextInt( locationSize ) );
                    }
                    while( destination.equals( origin ) );

                    deadline = LocalDate.now().plusDays( 35 + random.nextInt( 10 ) );

                    // Build sortable random tracking ids
                    uuid = UUID.randomUUID().toString().toUpperCase();
                    id = ( i + 11 ) + "-" + uuid.substring( 0, uuid.indexOf( "-" ) );

                    new BookNewCargo( cargos, origin, destination, deadline ).withTrackingId( id );
                }
                uow.complete();
            }
            catch( Exception e )
            {
                uow.discard();
                logger.error( "Problem booking a new cargo: " + e.getMessage() );
            }
        }

        private void registerEvent( LocalDate registrationDate,
                                    LocalDate completionDate,
                                    String trackingIdString,
                                    HandlingEventType handlingEventType,
                                    String unLocodeString,
                                    String voyageNumberString
        )
            throws Exception
        {
            ValueBuilder<ParsedHandlingEventData> event = vbf.newValueBuilder( ParsedHandlingEventData.class );
            event.prototype().registrationDate().set( registrationDate );
            event.prototype().completionDate().set( completionDate );
            event.prototype().trackingIdString().set( trackingIdString );
            event.prototype().handlingEventType().set( handlingEventType );
            event.prototype().unLocodeString().set( unLocodeString );
            event.prototype().voyageNumberString().set( voyageNumberString );

            tbf.newTransient( ProcessHandlingEvent.class ).register( event.newInstance() );
        }
    }
}
