/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.samples.dddsample.domain.model.location.assembly;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;

/**
 * Bootstrap ddd sample with sample locations.
 *
 * @author edward.yakop@gmail.com
 */
@Mixins( SampleLocationDataBootstrapService.SampleLocationDataBootstrapServiceMixin.class )
public interface SampleLocationDataBootstrapService
    extends Activatable, ServiceComposite
{
    public static final String HONGKONG = "CNHKG";
    public static final String MELBOURNE = "AUMEL";
    public static final String STOCKHOLM = "SESTO";
    public static final String HELSINKI = "FIHEL";
    public static final String CHICAGO = "USCHI";
    public static final String TOKYO = "JNTKO";
    public static final String HAMBURG = "DEHAM";
    public static final String SHANGHAI = "CNSHA";
    public static final String ROTTERDAM = "NLRTM";
    public static final String GOTHENBURG = "SEGOT";
    public static final String HANGZOU = "CNHGH";
    public static final String NEWYORK = "USNYC";

    static final String[][] LOCATIONS = new String[][]
        {
            { HONGKONG, "Hong Kong" },
            { MELBOURNE, "Melbourne" },
            { STOCKHOLM, "Stockholm" },
            { HELSINKI, "Helsinki" },
            { CHICAGO, "Chicago" },
            { TOKYO, "Tokyo" },
            { HAMBURG, "Hamburg" },
            { ROTTERDAM, "Rotterdam" },
            { SHANGHAI, "Shanghai" },
            { GOTHENBURG, "Göteborg" },
            { HANGZOU, "Hangzhou" },
            { NEWYORK, "New York" }
        };

    public class SampleLocationDataBootstrapServiceMixin
        implements Activatable
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Service
        private LocationRepository locationRepository;
        @Service
        private LocationFactoryService locationFactory;

        public void activate()
            throws Exception
        {
            UnitOfWork uow = uowf.newUnitOfWork();

            try
            {
                for( String[] locationStrings : LOCATIONS )
                {
                    UnLocode unLocode = new UnLocode( locationStrings[ 0 ] );
                    Location location = locationRepository.find( unLocode );
                    if( location != null )
                    {
                        continue;
                    }

                    locationFactory.createLocation( unLocode, locationStrings[ 1 ] );
                }
            }
            finally
            {
                uow.complete();
            }
        }

        public void passivate()
            throws Exception
        {
            // Do nothing
        }
    }
}
