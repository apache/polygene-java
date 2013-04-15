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
package org.qi4j.library.circuitbreaker;

import org.qi4j.functional.Specification;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;

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
   public static Specification<Throwable> in( final Class<? extends Throwable>... throwables)
   {
      return new Specification<Throwable>()
      {
         @Override
         public boolean satisfiedBy( Throwable item )
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

   public static Specification<Throwable> rootCause( final Specification<Throwable> specification)
   {
      return new Specification<Throwable>()
      {
         @Override
         public boolean satisfiedBy( Throwable item )
         {
            return specification.satisfiedBy( unwrap(item) );
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
