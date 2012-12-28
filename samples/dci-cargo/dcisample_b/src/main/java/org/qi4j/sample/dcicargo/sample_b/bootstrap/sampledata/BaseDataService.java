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
package org.qi4j.sample.dcicargo.sample_b.bootstrap.sampledata;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.sample.dcicargo.pathfinder.api.GraphTraversalService;
import org.qi4j.sample.dcicargo.pathfinder.api.TransitEdge;
import org.qi4j.sample.dcicargo.pathfinder.api.TransitPath;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.entity.LocationEntity;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.UnLocode;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Create basic sample data on startup of application.
 */
@Mixins( BaseDataService.Mixin.class )
@Activators( BaseDataService.Activator.class )
public interface BaseDataService
    extends ServiceComposite
{

    void createBaseData();

    class Activator
            extends ActivatorAdapter<ServiceReference<BaseDataService>>
    {

        @Override
        public void afterActivation( ServiceReference<BaseDataService> activated )
                throws Exception
        {
            activated.get().createBaseData();
        }

    }

    public abstract class Mixin
        extends BaseData
        implements BaseDataService
    {
        @Structure
        ValueBuilderFactory valueBuilderFactory;

        @Structure
        UnitOfWorkFactory uowf;

        @Service
        GraphTraversalService graphTraversalService;

        @Structure
        QueryBuilderFactory qbf;

        private static final Logger logger = LoggerFactory.getLogger( BaseDataService.class );

        @Override
        public void createBaseData()
        {
            logger.debug( "CREATING BASIC DATA..." );

            // Resources for the BaseData class
            vbf = valueBuilderFactory;

            uow = uowf.newUnitOfWork( newUsecase( "Create base data" ) );

            // Create locations
            location( unlocode( "AUMEL" ), "Melbourne" );
            location( unlocode( "CNHGH" ), "Hangzhou" );
            location( unlocode( "CNHKG" ), "Hongkong" );
            location( unlocode( "CNSHA" ), "Shanghai" );
            location( unlocode( "DEHAM" ), "Hamburg" );
            location( unlocode( "FIHEL" ), "Helsinki" );
            location( unlocode( "JNTKO" ), "Tokyo" );
            location( unlocode( "NLRTM" ), "Rotterdam" );
            location( unlocode( "SEGOT" ), "Gothenburg" );
            location( unlocode( "SESTO" ), "Stockholm" );
            location( unlocode( "SOMGQ" ), "Mogadishu" );
            location( unlocode( "USCHI" ), "Chicago" );
            location( unlocode( "USDAL" ), "Dallas" );
            location( unlocode( "USNYC" ), "New York" );

            // Create voyages
            try
            {
                for( TransitPath voyagePath : graphTraversalService.getVoyages() )
                {
                    String voyageNumber = null;
                    List<CarrierMovement> carrierMovements = new ArrayList<CarrierMovement>();
                    for( TransitEdge voyageEdge : voyagePath.getTransitEdges() )
                    {
                        voyageNumber = voyageEdge.getVoyageNumber();
                        Location from = uow.get( Location.class, voyageEdge.getFromUnLocode() );
                        Location to = uow.get( Location.class, voyageEdge.getToUnLocode() );
                        carrierMovements.add( carrierMovement( from, to, voyageEdge.getFromDate(), voyageEdge.getToDate() ) );
                    }

                    ValueBuilder<Schedule> schedule = vbf.newValueBuilder( Schedule.class );
                    schedule.prototype().carrierMovements().set( carrierMovements );
                    voyage( voyageNumber, schedule.newInstance() );
                }
            }
            catch( RemoteException e )
            {
                e.printStackTrace();
            }

            // Cargo and HandlingEvent aggregate roots
            CARGOS = uow.newEntity( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID );
            HANDLING_EVENTS = uow.newEntity( HandlingEventAggregateRoot.class, HandlingEventAggregateRoot.HANDLING_EVENTS_ID );

            logger.debug( "BASIC DATA CREATED" );
        }

        protected static UnLocode unlocode( String unlocodeString )
        {
            ValueBuilder<UnLocode> unlocode = vbf.newValueBuilder( UnLocode.class );
            unlocode.prototype().code().set( unlocodeString );
            return unlocode.newInstance();
        }

        protected static Location location( UnLocode unlocode, String locationStr )
        {
            EntityBuilder<LocationEntity> location = uow.newEntityBuilder( LocationEntity.class, unlocode.code()
                .get() );
            location.instance().unLocode().set( unlocode );
            location.instance().name().set( locationStr );
            return location.newInstance();
        }

    }
}