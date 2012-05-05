package com.marcgrue.dcisample_b.bootstrap.sampledata;

import com.marcgrue.dcisample_b.context.interaction.booking.BookNewCargo;
import com.marcgrue.dcisample_b.context.interaction.booking.routing.AssignCargoToRoute;
import com.marcgrue.dcisample_b.context.interaction.handling.ProcessHandlingEvent;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.InspectUnhandledCargo;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import com.marcgrue.dcisample_b.context.service.routing.RoutingService;
import com.marcgrue.dcisample_b.data.aggregateroot.CargoAggregateRoot;
import com.marcgrue.dcisample_b.data.factory.RouteSpecificationFactoryService;
import com.marcgrue.dcisample_b.data.structure.cargo.Cargo;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.NextHandlingEvent;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.voyage.CarrierMovement;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.*;
import static com.marcgrue.dcisample_b.infrastructure.dci.Context.prepareContextBaseClass;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Create sample cargos in different delivery stages.
 *
 * Add more cases if needed in the loop below.
 */
@Mixins( SampleDataService.Mixin.class )
public interface SampleDataService
      extends ServiceComposite, Activatable
{
    public abstract class Mixin
          implements SampleDataService, Activatable
    {
        @Structure
        QueryBuilderFactory qbf;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        TransientBuilderFactory tbf;

        @Structure
        UnitOfWorkFactory uowf;

        @Service
        RoutingService routingService;

        @Service
        BaseDataService baseDataService;

        @Service
        RouteSpecificationFactoryService routeSpecFactory;

        private static final Logger logger = LoggerFactory.getLogger( SampleDataService.class );

        public void activate() throws Exception
        {
            baseDataService.create();

            prepareContextBaseClass( uowf, vbf );

            logger.info( "######  CREATING SAMPLE DATA...  ##########################################" );

            // Create cargos
            populateRandomCargos( 12 );

            // Handle cargos
            UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "Create sample data" ) );
            try
            {
                int i = 11; // starting at 11 for sortable tracking id prefix in lists
                for (Cargo cargo : qbf.newQueryBuilder( Cargo.class ).newQuery( uow ))
                {
                    final String trackingId = cargo.trackingId().get().id().get();
                    final RouteSpecification routeSpec = cargo.routeSpecification().get();
                    routeSpec.print();

                    NextHandlingEvent nextEvent = null;
                    Date time = null;
                    String port = null;
                    String voyageNumber = null;
                    Voyage voyage;
                    HandlingEventType type = null;
                    String wrongPort = null;
                    String wrongVoyage = null;

                    // First cargo with id 11 is not routed

                    // ROUTE
                    if (i > 11)
                    {
                        final List<Itinerary> routes = routingService.fetchRoutesForSpecification( routeSpec );
                        final Itinerary itinerary = routes.get( 0 );
                        new AssignCargoToRoute( cargo, itinerary ).assign();
                    }

                    // MISROUTE: Route specification not satisfied with itinerary
                    if (i == 12)
                    {
                        Location origin = routeSpec.origin().get();
                        Location dest = routeSpec.destination().get();
                        Location badDest = null;
                        Query<Location> locations = qbf.newQueryBuilder( Location.class ).newQuery( uow );
                        for (Location loc : locations)
                        {
                            if (!origin.equals( loc ) && !dest.equals( loc ))
                            {
                                badDest = loc;
                                break;
                            }
                        }

                        final RouteSpecification unsatisfiedRouteSpec =
                              routeSpecFactory.build( origin, badDest, new Date(), new DateTime().plusDays( 25 ).toDate() );
                        cargo.routeSpecification().set( unsatisfiedRouteSpec );

                        new InspectUnhandledCargo( cargo ).inspect();
                    }

                    // RECEIVE
                    if (i > 13)
                    {
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        port = nextEvent.location().get().getCode();
                        final Date mockTime = new Date();
                        registerEvent( mockTime, mockTime, trackingId, RECEIVE, port, null );
                    }

                    // MISDIRECT: LOAD onto wrong carrier
                    if (i == 15)
                    {
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        time = nextEvent.time().get();
                        port = nextEvent.location().get().getCode();
                        voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();

                        // Find earliest wrong carrier movement (voyage) with same departure location
                        final Query<Voyage> voyages = qbf.newQueryBuilder( Voyage.class ).newQuery( uowf.currentUnitOfWork() );
                        int depth = 0;
                        do
                        {
                            for (Voyage voy : voyages)
                            {
                                if (voy.voyageNumber().get().number().get().equals( voyageNumber ))
                                    continue;

                                if (depth >= voy.schedule().get().carrierMovements().get().size())
                                    continue;

                                // Carrier movement at current depth
                                final CarrierMovement movement = voy.schedule().get().carrierMovements().get().get( depth );
                                final boolean goingFromSamePort = movement.departureLocation().get().getCode().equals( port );
                                final boolean notGoingToDestination = !movement.arrivalLocation().get().equals( routeSpec.destination().get() );

                                if (goingFromSamePort && notGoingToDestination)
                                {
                                    wrongVoyage = voy.voyageNumber().get().number().get();
                                    break;
                                }
                            }
                        }
                        while (wrongVoyage == null && depth++ < 10);

                        registerEvent( time, time, trackingId, LOAD, port, wrongVoyage );
                    }

                    // LOAD
                    if (i > 15)
                    {
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        time = nextEvent.time().get();
                        port = nextEvent.location().get().getCode();
                        voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();
                        registerEvent( time, time, trackingId, LOAD, port, voyageNumber );

                        // Cargo is now on board carrier
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        time = nextEvent.time().get();
                        type = nextEvent.handlingEventType().get();
                        port = nextEvent.location().get().getCode();
                        voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();
                    }

                    // MISDIRECT: UNLOAD from carrier in wrong location
                    if (i == 17)
                    {
                        voyage = uow.get( Voyage.class, voyageNumber );
                        for (CarrierMovement movement : voyage.schedule().get().carrierMovements().get())
                        {
                            final String arrivalPort = movement.arrivalLocation().get().getCode();

                            // Take first voyage with different arrival location
                            if (!arrivalPort.equals( port ))
                            {
                                wrongPort = movement.arrivalLocation().get().unLocode().get().code().get();
                                break;
                            }
                        }
                        registerEvent( time, time, trackingId, UNLOAD, wrongPort, voyageNumber );
                    }

                    // UNLOAD
                    if (i > 17)
                    {
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        time = nextEvent.time().get();
                        port = nextEvent.location().get().getCode();
                        voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();
                        registerEvent( time, time, trackingId, UNLOAD, port, voyageNumber );

                        // Cargo is now in midpoint location
                        nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                        time = nextEvent.time().get();
                        type = nextEvent.handlingEventType().get();
                        port = nextEvent.location().get().getCode();
                    }

                    // CUSTOMS: Customs handling in midpoint location (doesn't affect misdirection status)
                    if (i == 19)
                    {
                        registerEvent( time, time, trackingId, CUSTOMS, port, null );
                    }

                    // MISDIRECT: Unexpected claim before reaching destination
                    if (i == 20)
                    {
                        registerEvent( time, time, trackingId, CLAIM, port, null );
                    }

                    // Complete all LOAD/UNLOADS
                    if (i > 20)
                    {
                        do
                        {
                            voyageNumber = nextEvent.voyage().get().voyageNumber().get().number().get();
                            registerEvent( time, time, trackingId, type, port, voyageNumber );

                            nextEvent = cargo.delivery().get().nextHandlingEvent().get();
                            time = nextEvent.time().get();
                            port = nextEvent.location().get().getCode();
                            type = nextEvent.handlingEventType().get();
                        }
                        while (type != HandlingEventType.CLAIM);
                    }

                    // CLAIM at destination - this ends the life cycle of the cargo delivery
                    if (i == 22)
                        registerEvent( time, time, trackingId, CLAIM, port, null );

                    // Add more cases if needed...
                    i++;
                }

                uow.complete();
            }
            catch (Exception e)
            {
                uow.discard();
                logger.error( "Problem handling cargos: " + e.getMessage() );
                throw e;
            }

            logger.info( "######  SAMPLE DATA CREATED  ##############################################" );
        }

        public void passivate() throws Exception
        {
            // Do nothing
        }

        private void populateRandomCargos( int numberOfCargos )
        {
            Usecase usecase = UsecaseBuilder.newUsecase( "Populate Random Cargos" );
            UnitOfWork uow = uowf.newUnitOfWork( usecase );

            CargoAggregateRoot cargos = uow.get( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID );

            Query<Location> allLocations = qbf.newQueryBuilder( Location.class ).newQuery( uow );
            int locationSize = (int) allLocations.count();

            // Make array for selection of location with random index
            final List<Location> locationList = new ArrayList<Location>();
            for (Location location : allLocations)
                locationList.add( location );

            Location origin;
            Location destination;
            Random random = new Random();
            Date deadline;
            String uuid;
            String id;
            try
            {
                for (int i = 0; i < numberOfCargos; i++)
                {
                    origin = locationList.get( random.nextInt( locationSize ) );

                    // Find destination different from origin
                    do destination = locationList.get( random.nextInt( locationSize ) );
                    while (destination.equals( origin ));

                    deadline = new LocalDate().plusDays( 35 + random.nextInt( 10 ) ).toDateTime( new LocalTime() ).toDate();

                    // Build sortable random tracking ids
                    uuid = UUID.randomUUID().toString().toUpperCase();
                    id = ( i + 11 ) + "-" + uuid.substring( 0, uuid.indexOf( "-" ) );

                    new BookNewCargo( cargos, origin, destination, deadline ).withTrackingId( id );
                }
                uow.complete();
            }
            catch (Exception e)
            {
                uow.discard();
                logger.error( "Problem booking a new cargo: " + e.getMessage() );
            }
        }

        private void registerEvent( Date registrationTime,
                                    Date completionTime,
                                    String trackingIdString,
                                    HandlingEventType handlingEventType,
                                    String unLocodeString,
                                    String voyageNumberString ) throws Exception
        {
            ValueBuilder<ParsedHandlingEventData> event = vbf.newValueBuilder( ParsedHandlingEventData.class );
            event.prototype().registrationTime().set( registrationTime );
            event.prototype().completionTime().set( completionTime );
            event.prototype().trackingIdString().set( trackingIdString );
            event.prototype().handlingEventType().set( handlingEventType );
            event.prototype().unLocodeString().set( unLocodeString );
            event.prototype().voyageNumberString().set( voyageNumberString );

            tbf.newTransient( ProcessHandlingEvent.class ).register( event.newInstance() );
        }
    }
}
