package com.marcgrue.dcisample_a.bootstrap.sampledata;

import com.marcgrue.dcisample_a.context.shipping.booking.BookNewCargo;
import com.marcgrue.dcisample_a.context.shipping.handling.RegisterHandlingEvent;
import com.marcgrue.dcisample_a.data.entity.CargosEntity;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.data.shipping.delivery.ExpectedHandlingEvent;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.marcgrue.dcisample_a.infrastructure.dci.Context.prepareContextBaseClass;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Create sample Cargos in different delivery stages
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
        UnitOfWorkFactory uowf;

        @Service
        BaseDataService baseDataService;

        private static final Logger logger = LoggerFactory.getLogger( SampleDataService.class );

        public void activate() throws Exception
        {
            baseDataService.create();

            prepareContextBaseClass( uowf );

            logger.info( "######  CREATING SAMPLE DATA...  ##########################################" );

            // Create cargos
            populateRandomCargos( 15 );

            // Handle cargos
            UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "### Create sample data" ) );
            try
            {
                int i = 11; // starting at 11 for sortable tracking id prefix in lists
                for (Cargo cargo : qbf.newQueryBuilder( Cargo.class ).newQuery( uow ))
                {
                    String trackingId = cargo.trackingId().get().id().get();
                    ExpectedHandlingEvent nextEvent;
                    Date time;
                    String port;
                    String voyage;
                    HandlingEventType type;

                    // BOOK cargo with no handling (i == 11)

                    // ROUTE
                    if (i > 11)
                    {
                        Itinerary itinerary = new BookNewCargo( cargo ).routeCandidates().get( 0 );
                        new BookNewCargo( cargo, itinerary ).assignCargoToRoute();
                    }

                    // RECEIVE
                    if (i > 12)
                    {
                        nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                        port = nextEvent.location().get().getCode();
                        Date mockTime = new Date();
                        new RegisterHandlingEvent( mockTime, mockTime, trackingId, "RECEIVE", port, null ).register();
                    }

                    // LOAD
                    if (i > 13)
                    {
                        nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                        time = nextEvent.time().get();
                        port = nextEvent.location().get().getCode();
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        new RegisterHandlingEvent( time, time, trackingId, "LOAD", port, voyage ).register();
                    }

                    // UNLOAD
                    if (i > 14)
                    {
                        nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                        time = nextEvent.time().get();
                        port = nextEvent.location().get().getCode();
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        new RegisterHandlingEvent( time, time, trackingId, "UNLOAD", port, voyage ).register();
                    }

                    // Cargo is now in port
                    nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                    time = nextEvent.time().get();
                    port = nextEvent.location().get().getCode();
                    type = nextEvent.handlingEventType().get();

                    // MISDIRECTED: Unexpected customs handling before reaching destination
                    if (i == 16)
                    {
                        new RegisterHandlingEvent( time, time, trackingId, "CUSTOMS", port, null ).register();
                    }

                    // MISDIRECTED: Unexpected claim before reaching destination
                    if (i == 17)
                    {
                        new RegisterHandlingEvent( time, time, trackingId, "CLAIM", port, null ).register();
                    }

                    // MISDIRECTED: LOAD in wrong port
                    if (i == 18)
                    {
                        String wrongPort = port.equals( "USDAL" ) ? "USCHI" : "USDAL";
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        new RegisterHandlingEvent( time, time, trackingId, "LOAD", wrongPort, voyage ).register();
                    }

                    // MISDIRECTED: LOAD onto wrong carrier
                    if (i == 19)
                    {
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        String wrongVoyage = voyage.equals( "V100S" ) ? "V200T" : "V100S";
                        new RegisterHandlingEvent( time, time, trackingId, "LOAD", port, wrongVoyage ).register();
                    }

                    // MISDIRECTED: LOAD onto wrong carrier in wrong port
                    if (i == 20)
                    {
                        String wrongPort = port.equals( "USDAL" ) ? "USCHI" : "USDAL";
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        String wrongVoyage = voyage.equals( "V100S" ) ? "V200T" : "V100S";
                        new RegisterHandlingEvent( time, time, trackingId, "LOAD", wrongPort, wrongVoyage ).register();
                    }

                    // MISDIRECTED: UNLOAD in wrong port
                    if (i == 21)
                    {
                        String wrongPort = port.equals( "USDAL" ) ? "USCHI" : "USDAL";
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        new RegisterHandlingEvent( time, time, trackingId, "UNLOAD", wrongPort, voyage ).register();
                    }

                    // MISDIRECTED: UNLOAD from wrong carrier
                    if (i == 22)
                    {
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        String wrongVoyage = voyage.equals( "V100S" ) ? "V200T" : "V100S";
                        new RegisterHandlingEvent( time, time, trackingId, "UNLOAD", port, wrongVoyage ).register();
                    }

                    // MISDIRECTED: UNLOAD from wrong carrier in wrong port
                    if (i == 23)
                    {
                        String wrongPort = port.equals( "USDAL" ) ? "USCHI" : "USDAL";
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        String wrongVoyage = voyage.equals( "V100S" ) ? "V200T" : "V100S";
                        new RegisterHandlingEvent( time, time, trackingId, "UNLOAD", wrongPort, wrongVoyage ).register();
                    }

                    // Complete all LOAD/UNLOADS
                    if (i > 23)
                    {
                        do
                        {
                            voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                            new RegisterHandlingEvent( time, time, trackingId, type.name(), port, voyage ).register();

                            nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                            time = nextEvent.time().get();
                            port = nextEvent.location().get().getCode();
                            type = nextEvent.handlingEventType().get();
                        }
                        while (type != HandlingEventType.CLAIM);
                    }

                    // CLAIM at destination - this ends the life cycle of the cargo delivery
                    if (i == 25)
                        new RegisterHandlingEvent( time, time, trackingId, "CLAIM", port, null ).register();

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
            Usecase usecase = UsecaseBuilder.newUsecase( "### Populate Random Cargos ###" );
            UnitOfWork uow = uowf.newUnitOfWork( usecase );

            CargosEntity cargos = uow.get( CargosEntity.class, CargosEntity.CARGOS_ID );

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

                    deadline = new LocalDate().plusDays( 15 + random.nextInt( 10 ) ).toDateTime( new LocalTime() ).toDate();

                    // Build sortable random tracking ids
                    uuid = UUID.randomUUID().toString().toUpperCase();
                    id = ( i + 11 ) + "-" + uuid.substring( 0, uuid.indexOf( "-" ) );

                    new BookNewCargo( cargos, origin, destination, deadline ).createCargo( id );
                }
                uow.complete();
            }
            catch (Exception e)
            {
                uow.discard();
                logger.error( "Problem booking a new cargo: " + e.getMessage() );
            }
        }
    }
}
