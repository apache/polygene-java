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

package org.qi4j.io;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;
import org.slf4j.Logger;

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
     *
     * @return
     */
    public static <T, ReceiverThrowableType extends Throwable> Output<T, ReceiverThrowableType> filter( final Specification<? super T> specification,
                                                                                                        final Output<T, ReceiverThrowableType> output
    )
    {
        return new Output<T, ReceiverThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<? extends T, SenderThrowableType> sender )
                throws ReceiverThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<T, SenderThrowableType>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super T, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<T, ReceiverThrowableType>()
                        {
                            public void receive( T item )
                                throws ReceiverThrowableType
                            {
                                if( specification.satisfiedBy( item ) )
                                {
                                    receiver.receive( item );
                                }
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
     *
     * @return
     */
    public static <From, To, ReceiverThrowableType extends Throwable> Output<From, ReceiverThrowableType> map( final Function<? super From, ? extends To> function,
                                                                                                               final Output<To, ReceiverThrowableType> output
    )
    {
        return new Output<From, ReceiverThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<? extends From, SenderThrowableType> sender )
                throws ReceiverThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<To, SenderThrowableType>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super To, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<From, ReceiverThrowableType>()
                        {
                            public void receive( From item )
                                throws ReceiverThrowableType
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
     *
     * @return
     */
    public static <T, ReceiverThrowableType extends Throwable> Output<T, ReceiverThrowableType> filteredMap( final Specification<? super T> specification,
                                                                                                             final Function<? super T, ? extends T> function,
                                                                                                             final Output<T, ReceiverThrowableType> output
    )
    {
        return new Output<T, ReceiverThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<? extends T, SenderThrowableType> sender )
                throws ReceiverThrowableType, SenderThrowableType
            {
                output.receiveFrom( new Sender<T, SenderThrowableType>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super T, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, SenderThrowableType
                    {
                        sender.sendTo( new Receiver<T, ReceiverThrowableType>()
                        {
                            public void receive( T item )
                                throws ReceiverThrowableType
                            {
                                if( specification.satisfiedBy( item ) )
                                {
                                    receiver.receive( function.map( item ) );
                                }
                                else
                                {
                                    receiver.receive( item );
                                }
                            }
                        } );
                    }
                } );
            }
        };
    }

    /**
     * Wrapper for Outputs that uses a lock whenever a transfer is instantiated. Typically a read-lock would be used on
     * the sending side and a write-lock would be used on the receiving side. Inputs can use this as well to create a
     * wrapper on the send side when transferTo is invoked.
     *
     * @param lock                    the lock to be used for transfers
     * @param output                  output to be wrapped
     * @param <T>
     * @param <ReceiverThrowableType>
     *
     * @return Output wrapper that uses the given lock during transfers.
     */
    public static <T, ReceiverThrowableType extends Throwable> Output<T, ReceiverThrowableType> lock( final Lock lock,
                                                                                                      final Output<T, ReceiverThrowableType> output
    )
    {
        return new Output<T, ReceiverThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends T, SenderThrowableType> sender )
                throws ReceiverThrowableType, SenderThrowableType
            {
                /**
                 * Fix for this bug:
                 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
                 */
                while( true )
                {
                    try
                    {
                        while( !lock.tryLock( 1000, TimeUnit.MILLISECONDS ) )
                        {
                            // On timeout, try again
                        }
                        break; // Finally got a lock
                    }
                    catch( InterruptedException e )
                    {
                        // Try again
                    }
                }

                try
                {
                    output.receiveFrom( sender );
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
    }

    /**
     * Wrapper for Outputs that uses a lock whenever a transfer is instantiated. Typically a read-lock would be used on the sending side and a write-lock
     * would be used on the receiving side.
     *
     * @param lock                  the lock to be used for transfers
     * @param input                 input to be wrapped
     * @param <T>
     * @param <SenderThrowableType>
     *
     * @return Input wrapper that uses the given lock during transfers.
     */
    public static <T, SenderThrowableType extends Throwable> Input<T, SenderThrowableType> lock( final Lock lock,
                                                                                                 final Input<T, SenderThrowableType> input
    )
    {
        return new Input<T, SenderThrowableType>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super T, ReceiverThrowableType> output )
                throws SenderThrowableType, ReceiverThrowableType
            {
                /**
                 * Fix for this bug:
                 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
                 */
                while( true )
                {
                    try
                    {
                        while( !( lock.tryLock() || lock.tryLock( 1000, TimeUnit.MILLISECONDS ) ) )
                        {
                            // On timeout, try again
                        }
                        break; // Finally got a lock
                    }
                    catch( InterruptedException e )
                    {
                        // Try again
                    }
                }

                try
                {
                    input.transferTo( output );
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
    }

    /**
     * Count the number of items in the transfer.
     *
     * @param <T>
     */
    // START SNIPPET: counter
    public static class Counter<T>
        implements Function<T, T>
    {
        private volatile long count = 0;

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
    // END SNIPPET: counter

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
     * Convert ByteBuffers to Strings using the given CharSet
     */
    public static class ByteBuffer2String
        implements Function<ByteBuffer, String>
    {
        private Charset charSet;

        public ByteBuffer2String( Charset charSet )
        {
            this.charSet = charSet;
        }

        public String map( ByteBuffer buffer )
        {
            return new String( buffer.array(), charSet );
        }
    }

    /**
     * Convert objects to Strings using .toString()
     */
    public static class ObjectToString
        implements Function<Object, String>
    {
        public String map( Object o )
        {
            return o.toString();
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
            logger.info( format.format( new String[]{ item.toString() } ) );
            return item;
        }
    }

    /**
     * Track progress of transfer by emitting a log message in given intervals.
     *
     * If logger or format is null, then you need to override the logProgress to do something
     *
     * @param <T> type of items to be transferred
     */
    // START SNIPPET: progress
    public static class ProgressLog<T>
        implements Function<T, T>
    {
        private Counter<T> counter;
        private Log<String> log;
        private final long interval;

        public ProgressLog( Logger logger, String format, long interval )
        {
            this.interval = interval;
            if( logger != null && format != null )
            {
                log = new Log<String>( logger, format );
            }
            counter = new Counter<T>();
        }

        public ProgressLog( long interval )
        {
            this.interval = interval;
            counter = new Counter<T>();
        }

        public T map( T t )
        {
            counter.map( t );
            if( counter.count % interval == 0 )
            {
                logProgress();
            }
            return t;
        }

        // Override this to do something other than logging the progress
        protected void logProgress()
        {
            if( log != null )
            {
                log.map( counter.count + "" );
            }
        }
    }
    // END SNIPPET: progress
}
