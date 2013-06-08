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
package org.qi4j.sample.dcicargo.sample_a.bootstrap.sampledata;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.sample.dcicargo.sample_a.data.entity.CargosEntity;
import org.qi4j.sample.dcicargo.sample_a.data.entity.HandlingEventsEntity;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.Location;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.UnLocode;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Schedule;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Voyage;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.VoyageNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Create basic sample data
 */
@Mixins( BaseDataService.Mixin.class )
@Activators( BaseDataService.Activator.class )
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
        @Structure
        ValueBuilderFactory valueBuilderFactory;

        @Structure
        UnitOfWorkFactory uowf;

        private static final Logger logger = LoggerFactory.getLogger( BaseDataService.class );

        @Override
        public void createBaseData()
        	throws Exception
        {
            logger.debug( "CREATING BASIC DATA..." );

            // Resources for the BaseData class
            vbf = valueBuilderFactory;

            uow = uowf.newUnitOfWork( newUsecase( "Open uow for " ) );
            try
            {
	            // UnLocode value objects
	            AUMEL = unlocode( "AUMEL" ); // Melbourne
	            CNHGH = unlocode( "CNHGH" ); // Hangzou
	            CNHKG = unlocode( "CNHKG" ); // Hong Kong
	            CNSHA = unlocode( "CNSHA" ); // Shanghai
	            DEHAM = unlocode( "DEHAM" ); // Hamburg
	            FIHEL = unlocode( "FIHEL" ); // Helsinki
	            JNTKO = unlocode( "JNTKO" ); // Tokyo
	            NLRTM = unlocode( "NLRTM" ); // Rotterdam
	            SEGOT = unlocode( "SEGOT" ); // Gothenburg
	            SESTO = unlocode( "SESTO" ); // Stockholm
	            USCHI = unlocode( "USCHI" ); // Chicago
	            USDAL = unlocode( "USDAL" ); // Dallas
	            USNYC = unlocode( "USNYC" ); // New York
	
	            // Location entity objects
	            MELBOURNE = location( AUMEL, "Melbourne" );
	            HANGZHOU = location( CNHGH, "Hangzhou" );
	            HONGKONG = location( CNHKG, "Hongkong" );
	            SHANGHAI = location( CNSHA, "Shanghai" );
	            HAMBURG = location( DEHAM, "Hamburg" );
	            HELSINKI = location( FIHEL, "Helsinki" );
	            TOKYO = location( JNTKO, "Tokyo" );
	            ROTTERDAM = location( NLRTM, "Rotterdam" );
	            GOTHENBURG = location( SEGOT, "Gothenburg" );
	            STOCKHOLM = location( SESTO, "Stockholm" );
	            CHICAGO = location( USCHI, "Chicago" );
	            DALLAS = location( USDAL, "Dallas" );
	            NEWYORK = location( USNYC, "New York" );
	
	            // Voyage entity objects
	            V100S = voyage( "V100S", schedule(
	                carrierMovement( NEWYORK, CHICAGO, day( 1 ), day( 2 ) ),
	                carrierMovement( CHICAGO, DALLAS, day( 8 ), day( 9 ) )
	            ) );
	            V200T = voyage( "V200T", schedule(
	                carrierMovement( NEWYORK, CHICAGO, day( 7 ), day( 8 ) ),
	                carrierMovement( CHICAGO, DALLAS, day( 8 ), day( 9 ) )
	            ) );
	            V300A = voyage( "V300A", schedule(
	                carrierMovement( DALLAS, HAMBURG, day( 10 ), day( 14 ) ),
	                carrierMovement( HAMBURG, STOCKHOLM, day( 15 ), day( 16 ) ),
	                carrierMovement( STOCKHOLM, HELSINKI, day( 17 ), day( 18 ) )
	            ) );
	            V400S = voyage( "V400S", schedule(
	                carrierMovement( TOKYO, ROTTERDAM, day( 9 ), day( 15 ) ),
	                carrierMovement( ROTTERDAM, HAMBURG, day( 15 ), day( 16 ) ),
	                carrierMovement( HAMBURG, MELBOURNE, day( 17 ), day( 26 ) ),
	                carrierMovement( MELBOURNE, TOKYO, day( 27 ), day( 33 ) )
	            ) );
	            V500S = voyage( "V500S", schedule(
	                carrierMovement( HAMBURG, STOCKHOLM, day( 17 ), day( 19 ) ),
	                carrierMovement( STOCKHOLM, HELSINKI, day( 20 ), day( 21 ) )
	            ) );
	
	            // Cargo and HandlingEvent factories
	            CARGOS = uow.newEntity( CargosEntity.class, CargosEntity.CARGOS_ID );
	            HANDLING_EVENTS = uow.newEntity( HandlingEventsEntity.class, HandlingEventsEntity.HANDLING_EVENTS_ID );
	
	            logger.debug( "BASIC DATA CREATED" );
	            uow.complete();
            }
            catch(Exception e)
            {
            	uow.discard();
            	logger.error("CANNOT CREATE BASIC DATA");
            	throw e;
            }
        }

        private Location location( UnLocode unlocode, String locationStr )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Location> location = uow.newEntityBuilder( Location.class, unlocode.code().get() );
            location.instance().unLocode().set( unlocode );
            location.instance().name().set( locationStr );
            return location.newInstance();
        }

        private Voyage voyage( String voyageNumberStr, Schedule schedule )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Voyage> voyage = uow.newEntityBuilder( Voyage.class, voyageNumberStr );

            // VoyageNumber
            ValueBuilder<VoyageNumber> voyageNumber = vbf.newValueBuilder( VoyageNumber.class );
            voyageNumber.prototype().number().set( voyageNumberStr );
            voyage.instance().voyageNumber().set( voyageNumber.newInstance() );

            // Schedule
            voyage.instance().schedule().set( schedule );
            return voyage.newInstance();
        }

    }
}