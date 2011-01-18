/*
 * Copyright 2010 Niclas Hedhman.
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

package org.qi4j.library.cxf.divs;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.cxf.Subscription;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.*;

@WebService
@Mixins( ProjectedDividends.ProjectedDividendsMixin.class )
public interface ProjectedDividends
{
    @WebMethod( action = "DSDataRequest", operationName = "DSData" )
    @WebResult( name = "Snapshot" )
    Map<String, DivStream> getSnapshot(
        @WebParam( name = "RequestType" ) String requestType,
        @WebParam( name = "DataService" ) String dataService,
        @WebParam( name = "Subscription" ) Subscription subscription
    );

    public class ProjectedDividendsMixin
        implements ProjectedDividends
    {
        @Structure
        private ValueBuilderFactory vbf;

        private int counter;

        public Map<String, DivStream> getSnapshot( String requestType, String dataService, Subscription subscription )
        {
            HashMap<String, DivStream> result = new HashMap<String, DivStream>();
            result.put( "bt.l/PRIVATE_niclas", createDivStream() );
            return result;
        }

        private DivStream createDivStream()
        {
            ValueBuilder<DivStream> builder = vbf.newValueBuilder( DivStream.class );
            DivStream prototype = builder.prototype();
            List<DivPoint> divPoints = new ArrayList<DivPoint>();
            for( int i = 0; i < 2; i++ )
            {
                divPoints.add( createDivPoint() );
            }
            prototype.divPoints().set( divPoints );
            prototype.consolidate().set( "N" );
            prototype.streamName().set( "PRIVATE_niclas" );
            prototype.systemUpdateTS().set( new Date(2010,10,10).toString() );
            prototype.userUpdateTS().set( new Date(2010,10,10 ).toString() );
            prototype.issueId().set( "PC10YZNZC100" );
            prototype.mdSymbol().set( "bt.l" );
            return builder.newInstance();
        }

        private DivPoint createDivPoint()
        {
            ValueBuilder<DivPoint> builder = vbf.newValueBuilder( DivPoint.class );
            DivPoint prototype = builder.prototype();
            prototype.comment().set( "Silly comment " );
            prototype.dt().set( createRandomDate() );
            float amount = randomAmount();
            prototype.val().set( "" + amount );
            prototype.valCcy().set( "USD" );
            prototype.divType().set( "REG" );
            prototype.recType().set( "A" );
            prototype.net().set( "" + ( amount * 0.9 ) );
            prototype.netCcy().set( "USD" );
            prototype.recDate().set( createRandomDate() );
            prototype.paydate().set( createRandomDate() );
            prototype.divTypeCD().set( "" );
            prototype.comment().set( "Silly comment" );
            prototype.updateTS().set( new Date(2010,10,10).toString() );
            prototype.lastUpdater().set( "niclas" );
            return builder.newInstance();
        }

        private float randomAmount()
        {
            return 0.236f * counter++;

        }

        private String createRandomDate()
        {
            return "20" + ( counter++ + 10 ) + "-0" + ( ( counter++ % 10 ) + 1 ) + "-" + ( ( counter++ % 19 ) + 10 );
        }
    }
}
