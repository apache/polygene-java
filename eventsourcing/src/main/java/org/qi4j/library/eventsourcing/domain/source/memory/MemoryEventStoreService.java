/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.eventsourcing.domain.source.memory;

/**
 * JAVADOC
 */

import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.source.AbstractEventStoreMixin;
import org.qi4j.library.eventsourcing.domain.source.EventSource;
import org.qi4j.library.eventsourcing.domain.source.EventStore;
import org.qi4j.library.eventsourcing.domain.source.EventStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * In-Memory EventStore. Mainly used for testing.
 */
@Mixins(MemoryEventStoreService.MemoryEventStoreMixin.class)
public interface MemoryEventStoreService
        extends EventSource, EventStore, EventStream, Activatable, ServiceComposite
{
   abstract class MemoryEventStoreMixin
           extends AbstractEventStoreMixin
           implements EventSource
   {
      // This list holds all transactions
      private LinkedList<UnitOfWorkDomainEventsValue> store = new LinkedList<UnitOfWorkDomainEventsValue>();

      private long currentCount = 0;

      public void activate() throws IOException
      {
         super.activate();
      }

      public void passivate() throws Exception
      {
         super.passivate();
      }

      public Input<UnitOfWorkDomainEventsValue, IOException> events(final long offset, final long limit)
      {
         return new Input<UnitOfWorkDomainEventsValue, IOException>()
         {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super UnitOfWorkDomainEventsValue, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
            {
               // Lock store first
               lock.lock();
               try
               {
                  output.receiveFrom(new Sender<UnitOfWorkDomainEventsValue, IOException>()
                  {
                     @Override
                     public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super UnitOfWorkDomainEventsValue, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                     {
                        ListIterator<UnitOfWorkDomainEventsValue> iterator = store.listIterator((int) offset);

                        long count = 0;

                        while (iterator.hasNext() && count < limit)
                        {
                           UnitOfWorkDomainEventsValue next = iterator.next();
                           receiver.receive(next);
                           count++;
                        }
                     }
                  });
               } finally
               {
                  lock.unlock();
               }
            }
         };
      }

      public long count()
      {
         return currentCount;
      }

      protected void storeEvents0(UnitOfWorkDomainEventsValue unitOfWorkDomainValue)
              throws IOException
      {
         store.addLast(unitOfWorkDomainValue);
         currentCount++;
      }
   }
}