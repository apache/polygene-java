package com.marcgrue.dcisample_b.bootstrap.sampledata;

import com.marcgrue.dcisample_b.data.aggregateroot.CargoAggregateRoot;
import com.marcgrue.dcisample_b.data.aggregateroot.HandlingEventAggregateRoot;
import com.marcgrue.dcisample_b.data.entity.LocationEntity;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.location.UnLocode;
import com.marcgrue.dcisample_b.data.structure.voyage.CarrierMovement;
import com.marcgrue.dcisample_b.data.structure.voyage.Schedule;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pathfinder.api.GraphTraversalService;
import pathfinder.api.TransitEdge;
import pathfinder.api.TransitPath;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Create basic sample data on startup of application.
 */
@Mixins( BaseDataService.Mixin.class )
public interface BaseDataService
      extends ServiceComposite, Activatable
{
    void create();

    public abstract class Mixin
          extends BaseData
          implements BaseDataService, Activatable
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

        public void activate()
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
                for (TransitPath voyagePath : graphTraversalService.getVoyages())
                {
                    String voyageNumber = null;
                    List<CarrierMovement> carrierMovements = new ArrayList<CarrierMovement>();
                    for (TransitEdge voyageEdge : voyagePath.getTransitEdges())
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
            catch (RemoteException e)
            {
                e.printStackTrace();
            }

            // Cargo and HandlingEvent aggregate roots
            CARGOS = uow.newEntity( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID );
            HANDLING_EVENTS = uow.newEntity( HandlingEventAggregateRoot.class, HandlingEventAggregateRoot.HANDLING_EVENTS_ID );

            logger.debug( "BASIC DATA CREATED" );
        }

        public void passivate() throws Exception
        {
            // Do nothing
        }

        protected static UnLocode unlocode( String unlocodeString )
        {
            ValueBuilder<UnLocode> unlocode = vbf.newValueBuilder( UnLocode.class );
            unlocode.prototype().code().set( unlocodeString );
            return unlocode.newInstance();
        }

        protected static Location location( UnLocode unlocode, String locationStr )
        {
            EntityBuilder<LocationEntity> location = uow.newEntityBuilder( LocationEntity.class, unlocode.code().get() );
            location.instance().unLocode().set( unlocode );
            location.instance().name().set( locationStr );
            return location.newInstance();
        }

        public void create()
        {
            try
            {
                // Save entities in (memory) store
                uow.complete();
            }
            catch (Exception e)
            {
                uow.discard();
                logger.error( "Problem creating basic data: " + e.getMessage() );
            }
        }
    }
}