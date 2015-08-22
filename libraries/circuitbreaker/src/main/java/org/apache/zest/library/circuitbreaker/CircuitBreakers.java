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
package org.apache.zest.library.circuitbreaker;

import java.util.function.Predicate;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;

/**
 * CircuitBreaker helper methods.
 */
public class CircuitBreakers
{
   public static <Item, ReceiverThrowable extends Throwable> Output<Item, ReceiverThrowable> withBreaker( final CircuitBreaker breaker, final Output<Item, ReceiverThrowable> output)
   {
      return new Output<Item, ReceiverThrowable>()
      {
         @Override
         public <SenderThrowableType extends Throwable> void receiveFrom(final Sender<? extends Item, SenderThrowableType> sender) throws ReceiverThrowable, SenderThrowableType
         {
            output.receiveFrom( new Sender<Item, SenderThrowableType>()
            {
               @Override
               public <ReceiverThrowableType extends Throwable> void sendTo(final Receiver<? super Item, ReceiverThrowableType> receiver) throws ReceiverThrowableType, SenderThrowableType
               {
                  // Check breaker first
                  if (!breaker.isOn())
                     throw (ReceiverThrowableType) breaker.lastThrowable();

                  sender.sendTo( new Receiver<Item, ReceiverThrowableType>()
                  {
                     @Override
                     public void receive( Item item ) throws ReceiverThrowableType
                     {
                        try
                        {
                           receiver.receive( item );

                           // Notify breaker that it went well
                           breaker.success();
                        } catch (Throwable receiverThrowableType)
                        {
                           // Notify breaker of trouble
                           breaker.throwable( receiverThrowableType );

                           throw (ReceiverThrowableType) receiverThrowableType;
                        }
                     }
                  });
               }
            });
         }
      };
   }

   /**
    * Allow all throwables that are equal to or subclasses of given list of throwables.
    *
    * @param throwables The Throwable types that are allowed.
    * @return A Specification that specifies the allowed Throwables.
    */
   public static Predicate<Throwable> in( final Class<? extends Throwable>... throwables)
   {
      return new Predicate<Throwable>()
      {
         @Override
         public boolean test( Throwable item )
         {
            Class<? extends Throwable> throwableClass = item.getClass();
            for (Class<? extends Throwable> throwable : throwables)
            {
               if (throwable.isAssignableFrom( throwableClass ))
                  return true;
            }
            return false;
         }
      };
   }

   public static Predicate<Throwable> rootCause( final Predicate<Throwable> specification)
   {
      return new Predicate<Throwable>()
      {
         @Override
         public boolean test( Throwable item )
         {
            return specification.test( unwrap(item) );
         }

         private Throwable unwrap(Throwable item)
         {
            if (item.getCause() != null)
               return item.getCause();
            else
               return item;
         }
      };
   }
}
