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
package org.qi4j.samples.dddsample.domain.model.cargo.assembly;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;

/**
 * @author edward.yakop@gmail.com
 */
public class ItineraryLifecycleConcern
    extends ConcernOf<Lifecycle>
    implements Lifecycle
{
    @Structure
    UnitOfWorkFactory uowf;
    @This
    CargoState cargo;

    private static final String EMPTY_ITINERARY_ID = Itinerary.class.getName() + ".Empty";

    public void create()
        throws LifecycleException
    {
        next.create();

        Itinerary emptyItinerary;
        try
        {
            emptyItinerary = uowf.currentUnitOfWork().get( Itinerary.class, EMPTY_ITINERARY_ID );
        }
        catch( NoSuchEntityException e )
        {
            emptyItinerary = uowf.currentUnitOfWork().newEntity( Itinerary.class, EMPTY_ITINERARY_ID );
        }
        cargo.itinerary().set( emptyItinerary );
    }

    public void remove()
        throws LifecycleException
    {
        next.remove();

        if( !( (Identity) cargo.itinerary().get() ).identity().get().equals( EMPTY_ITINERARY_ID ) )
        {
            ( (Lifecycle) cargo.itinerary() ).remove();
        }
    }
}
