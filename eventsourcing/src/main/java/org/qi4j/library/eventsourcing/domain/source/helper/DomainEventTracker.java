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

package org.qi4j.library.eventsourcing.domain.source.helper;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Transforms;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.source.EventSource;
import org.qi4j.library.eventsourcing.domain.source.EventStream;
import org.qi4j.library.eventsourcing.domain.source.UnitOfWorkEventsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
public class DomainEventTracker
        implements Runnable, UnitOfWorkEventsListener
{
    private Configuration<? extends DomainEventTrackerConfiguration> configuration;
    private final Output<UnitOfWorkDomainEventsValue, ? extends Throwable> output;
    private EventStream stream;
    private EventSource source;
    private boolean started = false;
    private Logger logger;

    public DomainEventTracker( EventStream stream, EventSource source,
                               Configuration<? extends DomainEventTrackerConfiguration> configuration,
                               Output<UnitOfWorkDomainEventsValue, ? extends Throwable> output )
    {
        this.stream = stream;
        this.configuration = configuration;
        this.output = output;
        this.source = source;

        logger = LoggerFactory.getLogger( configuration.configuration().identity().get() );
    }

    public synchronized void start()
    {
        if (!started)
        {
            started = true;

            run();

            stream.registerListener( this );
        }
    }

    public synchronized void stop()
    {
        if (started)
        {
            started = false;
            stream.unregisterListener( this );
        }
    }

    public synchronized void run()
    {
        // TODO This should optionally use a CircuitBreaker
        if (started && configuration.configuration().enabled().get())
        {
            Transforms.Counter counter = new Transforms.Counter();
            try
            {
                long currentOffset = configuration.configuration().lastOffset().get();
                source.events( currentOffset, Long.MAX_VALUE ).transferTo( Transforms.map( counter, output ) );

                // Save new offset, to be used in next round
                configuration.configuration().lastOffset().set( currentOffset+counter.getCount() );
                configuration.save();
            } catch (Throwable throwable)
            {
                logger.warn( "Event handling failed", throwable );
            }
        }
    }

    public void notifyTransactions( Iterable<UnitOfWorkDomainEventsValue> transactions )
    {
        run();
    }
}
