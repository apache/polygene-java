/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package org.qi4j.library.eventsourcing.domain.factory;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.source.EventStore;
import org.qi4j.library.eventsourcing.domain.source.UnitOfWorkEventsVisitor;
import org.qi4j.library.eventsourcing.domain.spi.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Notify event listeners when a complete UoW of domain events is available.
 */
public class UnitOfWorkNotificationConcern
        extends ConcernOf<DomainEventFactory>
        implements DomainEventFactory
{
    @Service
    EventStore eventStore;

    @Service
    Iterable<UnitOfWorkEventsVisitor> transactionVisitors;

    @Service
    CurrentUser currentUser;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    UnitOfWorkFactory uowf;

    String version;

    Logger logger = LoggerFactory.getLogger( DomainEventFactory.class );

    public void init( @Structure Application application )
    {
        version = application.version();
    }

    public DomainEventValue createEvent( EntityComposite entity, String name, Object[] args )
    {
        final UnitOfWork unitOfWork = uowf.currentUnitOfWork();

        DomainEventValue eventValue = next.createEvent( entity, name, args );

        // Add eventValue to list in UoW
        UnitOfWorkEvents events = unitOfWork.metaInfo().get( UnitOfWorkEvents.class );
        if (events == null)
        {
            events = new UnitOfWorkEvents();
            unitOfWork.metaInfo().set( events );

            unitOfWork.addUnitOfWorkCallback( new UnitOfWorkCallback()
            {
                String user;
                public void beforeCompletion() throws UnitOfWorkCompletionException
                {
                    user = currentUser.getCurrentUser();
                }

                public void afterCompletion( UnitOfWorkStatus status )
                {
                    if (status.equals( UnitOfWorkStatus.COMPLETED ))
                    {
                        UnitOfWorkEvents events = unitOfWork.metaInfo().get( UnitOfWorkEvents.class );

                        ValueBuilder<UnitOfWorkDomainEventsValue> builder = vbf.newValueBuilder( UnitOfWorkDomainEventsValue.class );
                        builder.prototype().user().set( user );
                        builder.prototype().timestamp().set( System.currentTimeMillis() );
                        builder.prototype().usecase().set( unitOfWork.usecase().name() );
                        builder.prototype().version().set( version );
                        builder.prototype().events().get().addAll( events.getEventValues() );

                        try
                        {
                            final UnitOfWorkDomainEventsValue unitOfWorkDomainValue = builder.newInstance();
                            eventStore.storeEvents( unitOfWorkDomainValue );

                            for (UnitOfWorkEventsVisitor unitOfWorkEventsVisitor : transactionVisitors)
                            {
                                try
                                {
                                    unitOfWorkEventsVisitor.visit( unitOfWorkDomainValue );
                                } catch (Exception e)
                                {
                                    logger.warn( "Could not deliver events", e );

                                }
                            }
                        } catch (IOException e)
                        {
                            logger.error( "Could not store events", e );
                            // How do we handle this? This is a major error!
                        }
                    }
                }
            } );
        }

        events.add( eventValue );

        return eventValue;
    }
}
