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
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.sample.dcicargo.pathfinder_b.api.GraphTraversalService;
import org.qi4j.sample.dcicargo.pathfinder_b.api.TransitEdge;
import org.qi4j.sample.dcicargo.pathfinder_b.api.TransitPath;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.aggregateroot.HandlingEventAggregateRoot;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.UnLocode;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Schedule;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Voyage;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.VoyageNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Create basic sample data on startup of application.
 */
@Mixins(BaseDataService.Mixin.class)
@Activators(BaseDataService.Activator.class)
public interface BaseDataService
    extends ServiceComposite
{

    void createBaseData()
        throws Exception;

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
        @Service
        GraphTraversalService graphTraversalService;

        private static final Logger logger = LoggerFactory.getLogger( BaseDataService.class );

        protected Mixin( @Structure Module module )
        {
            super( module );
        }

        @Override
        public void createBaseData()
            throws Exception
        {
            logger.debug( "CREATING BASIC DATA..." );

            UnitOfWork uow = module.newUnitOfWork( newUsecase( "Create base data" ) );

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
                    List<CarrierMovement> carrierMovements = new ArrayList<>();
                    for( TransitEdge voyageEdge : voyagePath.getTransitEdges() )
                    {
                        voyageNumber = voyageEdge.getVoyageNumber();
                        Location from = uow.get( Location.class, voyageEdge.getFromUnLocode() );
                        Location to = uow.get( Location.class, voyageEdge.getToUnLocode() );
                        carrierMovements.add( carrierMovement( from, to, voyageEdge.getFromDate(), voyageEdge.getToDate() ) );
                    }

                    ValueBuilder<Schedule> schedule = module.newValueBuilder( Schedule.class );
                    schedule.prototype().carrierMovements().set( carrierMovements );
                    voyage( voyageNumber, schedule.newInstance() );
                }
            }
            catch( RemoteException e )
            {
                e.printStackTrace();
            }

            // Cargo and HandlingEvent aggregate roots
            uow.newEntity( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID );
            uow.newEntity( HandlingEventAggregateRoot.class, HandlingEventAggregateRoot.HANDLING_EVENTS_ID );

            try
            {
                uow.complete();
                logger.debug( "BASIC DATA CREATED" );
            }
            catch( UnitOfWorkCompletionException ex )
            {
                uow.discard();
                logger.error( "UNABLE TO CREATE BASIC DATA" );
                throw ex;
            }
        }

        protected UnLocode unlocode( String unlocodeString )
        {
            ValueBuilder<UnLocode> unlocode = module.newValueBuilder( UnLocode.class );
            unlocode.prototype().code().set( unlocodeString );
            return unlocode.newInstance();
        }

        protected Location location( UnLocode unlocode, String locationStr )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            EntityBuilder<Location> location = uow.newEntityBuilder( Location.class, unlocode.code().get() );
            location.instance().unLocode().set( unlocode );
            location.instance().name().set( locationStr );
            return location.newInstance();
        }

        protected Voyage voyage( String voyageNumberStr, Schedule schedule )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            EntityBuilder<Voyage> voyage = uow.newEntityBuilder( Voyage.class, voyageNumberStr );

            // VoyageNumber
            ValueBuilder<VoyageNumber> voyageNumber = module.newValueBuilder( VoyageNumber.class );
            voyageNumber.prototype().number().set( voyageNumberStr );
            voyage.instance().voyageNumber().set( voyageNumber.newInstance() );

            // Schedule
            voyage.instance().schedule().set( schedule );
            return voyage.newInstance();
        }
    }
}