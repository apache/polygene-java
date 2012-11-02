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

package org.qi4j.library.eventsourcing.application.source.helper;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.library.eventsourcing.application.api.TransactionApplicationEvents;
import org.qi4j.library.eventsourcing.application.source.ApplicationEventSource;
import org.qi4j.library.eventsourcing.application.source.ApplicationEventStream;
import org.qi4j.library.eventsourcing.domain.source.helper.DomainEventTrackerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper that enables a service to easily track transactions. Upon startup
 * the tracker will get all the transactions from the store since the last
 * check, and delegate them to the given Output. It will also register itself
 * with the store so that it can get continuous updates.
 * <p/>
 * Then, as transactions come in from the store, they will be processed in real-time.
 * If a transaction is successfully handled the configuration of the service, which must
 * extend DomainEventTrackerConfiguration, will update the marker for the last successfully handled transaction.
 */
public class ApplicationTransactionTracker<ReceiverThrowableType extends Throwable>
{
    private Configuration<? extends DomainEventTrackerConfiguration> configuration;
    private Output<TransactionApplicationEvents, ReceiverThrowableType> output;
    private ApplicationEventStream stream;
    private ApplicationEventSource source;
    private boolean started = false;
    private boolean upToSpeed = false;
    private Logger logger;
    private Output<TransactionApplicationEvents, ReceiverThrowableType> trackerOutput;

    public ApplicationTransactionTracker( ApplicationEventStream stream, ApplicationEventSource source,
                                          Configuration<? extends DomainEventTrackerConfiguration> configuration,
                                          Output<TransactionApplicationEvents, ReceiverThrowableType> output )
    {
        this.stream = stream;
        this.configuration = configuration;
        this.source = source;
        this.output = output;

        logger = LoggerFactory.getLogger( output.getClass() );
    }

    public void start()
    {
        if (!started)
        {
            started = true;

            // Get events since last check
            upToSpeed = true; // Pretend that we are up to speed from now on
            trackerOutput = output();
            try
            {
                source.transactionsAfter( configuration.get().lastOffset().get(), Long.MAX_VALUE ).transferTo( trackerOutput );
            } catch (Throwable receiverThrowableType)
            {
                upToSpeed = false;
            }

            stream.registerListener( trackerOutput );
        }
    }

    public void stop()
    {
        if (started)
        {
            started = false;
            stream.unregisterListener( trackerOutput );
            upToSpeed = false;
        }
    }

    private Output<TransactionApplicationEvents, ReceiverThrowableType> output()
    {
        return new Output<TransactionApplicationEvents, ReceiverThrowableType>()
        {
           @Override
           public <SenderThrowableType extends Throwable> void receiveFrom(final Sender<? extends TransactionApplicationEvents, SenderThrowableType> sender) throws ReceiverThrowableType, SenderThrowableType
           {
                if (!upToSpeed)
                {
                    // The tracker has not handled successfully all transactions before,
                    // so it needs to get the backlog first

                    upToSpeed = true; // Pretend that we are up to speed from now on

                    // Get all transactions from last timestamp, including the one in this call
                    try
                    {
                        source.transactionsAfter( configuration.get().lastOffset().get(), Long.MAX_VALUE ).transferTo( trackerOutput );
                    } catch (Throwable e)
                    {
                        upToSpeed = false;
                        throw (SenderThrowableType) e;
                    }
                }

                try
                {
                    output.receiveFrom( new Sender<TransactionApplicationEvents, SenderThrowableType>()
                    {
                       @Override
                       public <ReceiverThrowableType extends Throwable> void sendTo(final Receiver<? super TransactionApplicationEvents, ReceiverThrowableType> receiver) throws ReceiverThrowableType, SenderThrowableType
                       {
                            sender.sendTo( new Receiver<TransactionApplicationEvents, ReceiverThrowableType>()
                            {
                                @Override
                                public void receive( TransactionApplicationEvents item ) throws ReceiverThrowableType
                                {
                                    receiver.receive( item );

                                    // Events in this transactionDomain were handled successfully so store new marker
                                    configuration.get().lastOffset().set( item.timestamp().get() );
                                    configuration.save();
                                }
                            } );
                        }
                    } );
                } catch (Throwable receiverThrowableType)
                {
                    upToSpeed = false;
                    throw (ReceiverThrowableType) receiverThrowableType;
                }
            }
        };
    }
}
