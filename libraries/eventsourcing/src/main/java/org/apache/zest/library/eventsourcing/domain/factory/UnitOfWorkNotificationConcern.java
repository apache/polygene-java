/**
 *
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

package org.apache.zest.library.eventsourcing.domain.factory;

import java.io.IOException;
import org.apache.zest.api.Qi4j;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCallback;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.functional.Iterables;
import org.apache.zest.io.Inputs;
import org.apache.zest.io.Output;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.apache.zest.library.eventsourcing.domain.source.EventStore;
import org.apache.zest.library.eventsourcing.domain.source.UnitOfWorkEventsVisitor;
import org.apache.zest.library.eventsourcing.domain.spi.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Structure
    Qi4j api;

    String version;

    Logger logger = LoggerFactory.getLogger( DomainEventFactory.class );

    Output<UnitOfWorkDomainEventsValue, IOException> eventOutput;

    public void init( @Structure Application application )
    {
        version = application.version();
        eventOutput = eventStore.storeEvents();
    }

    @Override
    public DomainEventValue createEvent( EntityComposite entity, String name, Object[] args )
    {
        final UnitOfWork unitOfWork = uowf.currentUnitOfWork();

        DomainEventValue eventValue = next.createEvent( api.dereference( entity ), name, args );

        // Add eventValue to list in UoW
        UnitOfWorkEvents events = unitOfWork.metaInfo(UnitOfWorkEvents.class );
        if (events == null)
        {
            events = new UnitOfWorkEvents();
            unitOfWork.setMetaInfo( events );

            unitOfWork.addUnitOfWorkCallback( new UnitOfWorkCallback()
            {
                String user;

                @Override
                public void beforeCompletion() throws UnitOfWorkCompletionException
                {
                    user = currentUser.getCurrentUser();
                }

                @Override
                public void afterCompletion( UnitOfWorkStatus status )
                {
                    if (status.equals( UnitOfWorkStatus.COMPLETED ))
                    {
                        UnitOfWorkEvents events = unitOfWork.metaInfo( UnitOfWorkEvents.class );

                        ValueBuilder<UnitOfWorkDomainEventsValue> builder = vbf.newValueBuilder( UnitOfWorkDomainEventsValue.class );
                        builder.prototype().user().set( user );
                        builder.prototype().timestamp().set( System.currentTimeMillis() );
                        builder.prototype().usecase().set( unitOfWork.usecase().name() );
                        builder.prototype().version().set( version );
                        builder.prototype().events().get().addAll( events.getEventValues() );

                        try
                        {
                            final UnitOfWorkDomainEventsValue unitOfWorkDomainValue = builder.newInstance();
                            Inputs.iterable( Iterables.iterable( unitOfWorkDomainValue ) ).transferTo( eventOutput );

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
