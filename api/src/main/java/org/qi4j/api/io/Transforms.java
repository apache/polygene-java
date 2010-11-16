/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.io;

import org.slf4j.Logger;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Utility class for I/O transforms
 */
public class Transforms
{
    /**
     * Filter items in a transfer by applying the given Specification to each item.
     *
     * @param specification
     * @param output
     * @param <T>
     * @param <ReceiverThrowableType>
     * @return
     */
    public static <T, ReceiverThrowableType extends Throwable> Output<T, ReceiverThrowableType> filter( final Specification<T> specification, final Output<T, ReceiverThrowableType> output )
    {
        return new Output<T, ReceiverThrowableType>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<T, SenderThrowableType> sender ) throws ReceiverThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<T, SenderThrowableType>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<T, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<T, ReceiverThrowableType>()
                        {
                            public void receive( T item ) throws ReceiverThrowableType
                            {
                                if (specification.test( item ))
                                    receiver.receive( item );
                            }
                        } );

                    }
                } );
            }
        };
    }

    /**
     * Map items in a transfer from one type to another by applying the given function.
     *
     * @param function
     * @param output
     * @param <From>
     * @param <To>
     * @param <ReceiverThrowableType>
     * @return
     */
    public static <From, To, ReceiverThrowableType extends Throwable> Output<From, ReceiverThrowableType> map( final Function<From, To> function, final Output<To, ReceiverThrowableType> output )
    {
        return new Output<From, ReceiverThrowableType>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<From, SenderThrowableType> sender ) throws ReceiverThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<To, SenderThrowableType>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<To, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<From, ReceiverThrowableType>()
                        {
                            public void receive( From item ) throws ReceiverThrowableType
                            {
                                receiver.receive( function.map( item ) );
                            }
                        } );

                    }
                } );
            }
        };
    }

    /**
     * Apply the given function to items in the transfer that match the given specification. Other items will pass
     * through directly.
     *
     * @param specification
     * @param function
     * @param output
     * @param <T>
     * @param <ReceiverThrowableType>
     * @return
     */
    public static <T, ReceiverThrowableType extends Throwable> Output<T, ReceiverThrowableType> filteredMap( final Specification<T> specification, final Function<T, T> function, final Output<T, ReceiverThrowableType> output )
    {
        return new Output<T, ReceiverThrowableType>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<T, SenderThrowableType> sender ) throws ReceiverThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<T, SenderThrowableType>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<T, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<T, ReceiverThrowableType>()
                        {
                            public void receive( T item ) throws ReceiverThrowableType
                            {
                                if (specification.test( item ))
                                    receiver.receive( function.map( item ) );
                                else
                                    receiver.receive( item );
                            }
                        } );

                    }
                } );
            }
        };
    }

    /**
     * Wrapper for Outputs that uses a lock whenever a transfer is instantiated. Typically a read-lock would be used on the sending side and a write-lock
     * would be used on the receiving side. Inputs can use this as well to create a wrapper on the send side when transferTo is invoked.
     *
     * @param lock                    the lock to be used for transfers
     * @param output                  output to be wrapped
     * @param <T>
     * @param <ReceiverThrowableType>
     * @return Output wrapper that uses the given lock during transfers.
     */
    public static <T, ReceiverThrowableType extends Throwable> Output<T, ReceiverThrowableType> lock( final Lock lock, final Output<T, ReceiverThrowableType> output )
    {
        return new Output<T, ReceiverThrowableType>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<T, SenderThrowableType> sender ) throws ReceiverThrowableType, SenderThrowableType
            {
                /**
                 * Fix for this bug:
                 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
                 */
                while(true)
                {
                    try
                    {
                        while( !lock.tryLock( 1000, TimeUnit.MILLISECONDS ) )
                        {
                            // On timeout, try again
                        }
                        return; // Finally got a lock
                    }
                    catch( InterruptedException e )
                    {
                        // Try again
                    }
                }
            }
        };
    }

    /**
     * Wrapper for Outputs that uses a lock whenever a transfer is instantiated. Typically a read-lock would be used on the sending side and a write-lock
     * would be used on the receiving side.
     *
     * @param lock                    the lock to be used for transfers
     * @param input                  input to be wrapped
     * @param <T>
     * @param <SenderThrowableType>
     * @return Input wrapper that uses the given lock during transfers.
     */
    public static <T, SenderThrowableType extends Throwable> Input<T, SenderThrowableType> lock( final Lock lock, final Input<T, SenderThrowableType> input )
    {
        return new Input<T, SenderThrowableType>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<T, ReceiverThrowableType> output ) throws SenderThrowableType, ReceiverThrowableType
            {
                /**
                 * Fix for this bug:
                 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
                 */
                while (true)
                {
                    try
                    {
                        lock.tryLock( 1000, TimeUnit.MILLISECONDS );
                        break;
                    } catch (InterruptedException e)
                    {
                        // Try again
                    }
                }

                try
                {
                    input.transferTo( output );
                } finally
                {
                    lock.unlock();
                }
            }
        };
    }

    /**
     * Generic specification interface.
     *
     * @param <T>
     */
    public interface Specification<T>
    {
        /**
         * Test whether an item matches the given specification
         *
         * @param item the item to be tested
         * @return true if the item matches, false otherwise
         */
        boolean test( T item );
    }

    /**
     * Generic function interface to map from one type to another
     *
     * @param <From>
     * @param <To>
     */
    public interface Function<From, To>
    {
        /**
         * Map a single item from one type to another
         *
         * @param from the input item
         * @return the mapped item
         */
        To map( From from );
    }

    /**
     * Count the number of items in the transfer.
     *
     * @param <T>
     */
    public static class Counter<T>
            implements Function<T, T>
    {
        private long count = 0;

        public long getCount()
        {
            return count;
        }

        public T map( T t )
        {
            count++;

            return t;
        }
    }

    /**
     * Convert strings to bytes using the given CharSet
     */
    public static class String2Bytes
            implements Function<String, byte[]>
    {
        private Charset charSet;

        public String2Bytes( Charset charSet )
        {
            this.charSet = charSet;
        }

        public byte[] map( String s )
        {
            return s.getBytes( charSet );
        }
    }

    /**
     * Log the toString() representation of transferred items to the given log. The string is first formatted using MessageFormat
     * with the given format.
     *
     * @param <T>
     */
    public static class Log<T>
            implements Function<T, T>
    {
        private Logger logger;
        private MessageFormat format;

        public Log( Logger logger, String format )
        {
            this.logger = logger;
            this.format = new MessageFormat( format );
        }

        public T map( T item )
        {
            logger.info( format.format( new String[]{item.toString()} ) );
            return item;
        }
    }
}
