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
package org.apache.zest.sample.dcicargo.sample_a.bootstrap.sampledata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
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
import org.apache.zest.sample.dcicargo.sample_a.context.shipping.booking.BookNewCargo;
import org.apache.zest.sample.dcicargo.sample_a.context.shipping.handling.RegisterHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_a.data.entity.CargosEntity;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.cargo.Cargo;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.delivery.ExpectedHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.handling.HandlingEventType;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.itinerary.Itinerary;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.location.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.zest.api.usecase.UsecaseBuilder.newUsecase;
import static org.apache.zest.sample.dcicargo.sample_a.infrastructure.dci.Context.prepareContextBaseClass;

/**
 * Create sample Cargos in different delivery stages
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
        QueryBuilderFactory qbf;

        @Structure
        UnitOfWorkFactory uowf;

        @Service // We depend on BaseData to be inserted
        BaseDataService baseDataService;

        private static final Logger logger = LoggerFactory.getLogger( SampleDataService.class );

        @Override
        public void insertSampleData()
            throws Exception
        {
            prepareContextBaseClass( uowf );

            logger.info( "######  CREATING SAMPLE DATA...  ##########################################" );

            // Create cargos
            populateRandomCargos( 15 );

            // Handle cargos
            UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "### Create sample data" ) );
            try
            {
                int i = 11; // starting at 11 for sortable tracking id prefix in lists
                QueryBuilder<Cargo> qb = qbf.newQueryBuilder( Cargo.class );
                for( Cargo cargo : uow.newQuery( qb ) )
                {
                    String trackingId = cargo.trackingId().get().id().get();
                    ExpectedHandlingEvent nextEvent;
                    LocalDate date;
                    String port;
                    String voyage;
                    HandlingEventType type;

                    // BOOK cargo with no handling (i == 11)

                    // ROUTE
                    if( i > 11 )
                    {
                        Itinerary itinerary = new BookNewCargo( cargo ).routeCandidates().get( 0 );
                        new BookNewCargo( cargo, itinerary ).assignCargoToRoute();
                    }

                    // RECEIVE
                    if( i > 12 )
                    {
                        nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                        port = nextEvent.location().get().getCode();
                        LocalDate mockTime = LocalDate.now();
                        new RegisterHandlingEvent( mockTime, mockTime, trackingId, "RECEIVE", port, null ).register();
                    }

                    // LOAD
                    if( i > 13 )
                    {
                        nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                        date = nextEvent.date().get();
                        port = nextEvent.location().get().getCode();
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        new RegisterHandlingEvent( date, date, trackingId, "LOAD", port, voyage ).register();
                    }

                    // UNLOAD
                    if( i > 14 )
                    {
                        nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                        date = nextEvent.date().get();
                        port = nextEvent.location().get().getCode();
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        new RegisterHandlingEvent( date, date, trackingId, "UNLOAD", port, voyage ).register();
                    }

                    // Cargo is now in port
                    nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                    date = nextEvent.date().get();
                    port = nextEvent.location().get().getCode();
                    type = nextEvent.handlingEventType().get();

                    // MISDIRECTED: Unexpected customs handling before reaching destination
                    if( i == 16 )
                    {
                        new RegisterHandlingEvent( date, date, trackingId, "CUSTOMS", port, null ).register();
                    }

                    // MISDIRECTED: Unexpected claim before reaching destination
                    if( i == 17 )
                    {
                        new RegisterHandlingEvent( date, date, trackingId, "CLAIM", port, null ).register();
                    }

                    // MISDIRECTED: LOAD in wrong port
                    if( i == 18 )
                    {
                        String wrongPort = port.equals( "USDAL" ) ? "USCHI" : "USDAL";
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        new RegisterHandlingEvent( date, date, trackingId, "LOAD", wrongPort, voyage ).register();
                    }

                    // MISDIRECTED: LOAD onto wrong carrier
                    if( i == 19 )
                    {
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        String wrongVoyage = voyage.equals( "V100S" ) ? "V200T" : "V100S";
                        new RegisterHandlingEvent( date, date, trackingId, "LOAD", port, wrongVoyage ).register();
                    }

                    // MISDIRECTED: LOAD onto wrong carrier in wrong port
                    if( i == 20 )
                    {
                        String wrongPort = port.equals( "USDAL" ) ? "USCHI" : "USDAL";
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        String wrongVoyage = voyage.equals( "V100S" ) ? "V200T" : "V100S";
                        new RegisterHandlingEvent( date, date, trackingId, "LOAD", wrongPort, wrongVoyage ).register();
                    }

                    // MISDIRECTED: UNLOAD in wrong port
                    if( i == 21 )
                    {
                        String wrongPort = port.equals( "USDAL" ) ? "USCHI" : "USDAL";
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        new RegisterHandlingEvent( date, date, trackingId, "UNLOAD", wrongPort, voyage ).register();
                    }

                    // MISDIRECTED: UNLOAD from wrong carrier
                    if( i == 22 )
                    {
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        String wrongVoyage = voyage.equals( "V100S" ) ? "V200T" : "V100S";
                        new RegisterHandlingEvent( date, date, trackingId, "UNLOAD", port, wrongVoyage ).register();
                    }

                    // MISDIRECTED: UNLOAD from wrong carrier in wrong port
                    if( i == 23 )
                    {
                        String wrongPort = port.equals( "USDAL" ) ? "USCHI" : "USDAL";
                        voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                        String wrongVoyage = voyage.equals( "V100S" ) ? "V200T" : "V100S";
                        new RegisterHandlingEvent( date, date, trackingId, "UNLOAD", wrongPort, wrongVoyage ).register();
                    }

                    // Complete all LOAD/UNLOADS
                    if( i > 23 )
                    {
                        do
                        {
                            voyage = nextEvent.voyage().get().voyageNumber().get().number().get();
                            new RegisterHandlingEvent( date, date, trackingId, type.name(), port, voyage ).register();

                            nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
                            date = nextEvent.date().get();
                            port = nextEvent.location().get().getCode();
                            type = nextEvent.handlingEventType().get();
                        }
                        while( type != HandlingEventType.CLAIM );
                    }

                    // CLAIM at destination - this ends the life cycle of the cargo delivery
                    if( i == 25 )
                    {
                        new RegisterHandlingEvent( date, date, trackingId, "CLAIM", port, null ).register();
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
            Usecase usecase = UsecaseBuilder.newUsecase( "### Populate Random Cargos ###" );
            UnitOfWork uow = uowf.newUnitOfWork( usecase );

            CargosEntity cargos = uow.get( CargosEntity.class, CargosEntity.CARGOS_ID );

            QueryBuilder<Location> qb = qbf.newQueryBuilder( Location.class );
            Query<Location> allLocations = uow.newQuery( qb );
            int locationSize = (int) allLocations.count();

            // Make array for selection of location with random index
            final List<Location> locationList = new ArrayList<Location>();
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

                    deadline = LocalDate.now().plusDays( 15 + random.nextInt( 10 ) );

                    // Build sortable random tracking ids
                    uuid = UUID.randomUUID().toString().toUpperCase();
                    id = ( i + 11 ) + "-" + uuid.substring( 0, uuid.indexOf( "-" ) );

                    new BookNewCargo( cargos, origin, destination, deadline ).createCargo( id );
                }
                uow.complete();
            }
            catch( Exception e )
            {
                uow.discard();
                logger.error( "Problem booking a new cargo: " + e.getMessage() );
            }
        }
    }
}
