/*
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zest.library.eventsourcing.application.factory;

import java.io.IOException;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCallback;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.library.eventsourcing.application.api.ApplicationEvent;
import org.apache.zest.library.eventsourcing.application.source.ApplicationEventStore;
import org.apache.zest.library.eventsourcing.domain.factory.DomainEventFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notify transaction listeners when a complete transaction of domain events is available.
 */
public class TransactionNotificationConcern
    extends ConcernOf<ApplicationEventFactory>
    implements ApplicationEventFactory
{
    @Service
    ApplicationEventStore eventStore;

    @Structure
    UnitOfWorkFactory uowf;

    Logger logger = LoggerFactory.getLogger( DomainEventFactory.class );

    @Override
    public ApplicationEvent createEvent( String name, Object[] args )
    {
        final UnitOfWork unitOfWork = uowf.currentUnitOfWork();

        ApplicationEvent event = next.createEvent( name, args );

        // Add event to list in UoW
        UnitOfWorkApplicationEvents events = unitOfWork.metaInfo( UnitOfWorkApplicationEvents.class );
        if( events == null )
        {
            events = new UnitOfWorkApplicationEvents();
            unitOfWork.setMetaInfo( events );

            unitOfWork.addUnitOfWorkCallback( new UnitOfWorkCallback()
            {
                @Override
                public void beforeCompletion()
                    throws UnitOfWorkCompletionException
                {
                }

                @Override
                public void afterCompletion( UnitOfWorkStatus status )
                {
                    if( status.equals( UnitOfWorkStatus.COMPLETED ) )
                    {
                        UnitOfWorkApplicationEvents events = unitOfWork.metaInfo( UnitOfWorkApplicationEvents.class );

                        try
                        {
                            eventStore.storeEvents( events.getEvents() );
                        }
                        catch( IOException e )
                        {
                            logger.error( "Could not store events", e );
                            // How do we handle this? This is a major error!
                        }
                    }
                }
            } );
        }

        events.add( event );

        return event;
    }
}
