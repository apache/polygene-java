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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

/**
 * Utility methods for creating standard Outputs
 */
public class Outputs
{
    // START SNIPPET: method

    /**
     * Write lines to a text file with UTF-8 encoding. Separate each line with a newline ("\n" character). If the writing or sending fails,
     * the file is deleted.
     * <p/>
     * If the filename ends with .gz, then the data is automatically GZipped.
     *
     * @param file the file to save the text to
     *
     * @return an Output for storing text in a file
     */
    public static Output<String, IOException> text( final File file )
    // END SNIPPET: method
    {
        return text( file, "UTF-8" );
    }

    // START SNIPPET: method

    /**
     * Write lines to a text file. Separate each line with a newline ("\n" character). If the writing or sending fails,
     * the file is deleted.
     * <p/>
     * If the filename ends with .gz, then the data is automatically GZipped.
     *
     * @param file the file to save the text to
     *
     * @return an Output for storing text in a file
     */
    public static Output<String, IOException> text( final File file, final String encoding )
    // END SNIPPET: method
    {
        return new Output<String, IOException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends String, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                File tmpFile = Files.createTemporayFileOf( file );

                OutputStream stream = new FileOutputStream( tmpFile );

                // If file should be gzipped, do that automatically
                if( file.getName().endsWith( ".gz" ) )
                {
                    stream = new GZIPOutputStream( stream );
                }

                final BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( stream, encoding ) );

                try
                {
                    sender.sendTo( new Receiver<String, IOException>()
                    {
                        @Override
                        public void receive( String item )
                            throws IOException
                        {
                            writer.append( item ).append( '\n' );
                        }
                    } );
                    writer.close();

                    // Replace file with temporary file
                    if( !file.exists() || file.delete() )
                    {
                        tmpFile.renameTo( file );
                    }
                }
                catch( IOException e )
                {
                    // We failed writing - close and delete
                    writer.close();
                    tmpFile.delete();
                }
                catch( Throwable senderThrowableType )
                {
                    // We failed writing - close and delete
                    writer.close();
                    tmpFile.delete();

                    throw (SenderThrowableType) senderThrowableType;
                }
            }
        };
    }

    // START SNIPPET: method

    /**
     * Write lines to a Writer. Separate each line with a newline ("\n" character).
     *
     * @param writer the Writer to write the text to
     * @return an Output for storing text in a Writer
     */
    public static Output<String, IOException> text( final Writer writer )
    // END SNIPPET: method
    {
        return new Output<String, IOException>()
        {

            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends String, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                sender.sendTo( new Receiver<String, IOException>()
                {

                    @Override
                    public void receive( String item )
                        throws IOException
                    {
                        writer.append( item ).append( "\n" );
                    }

                } );
            }

        };
    }

    // START SNIPPET: method

    /**
     * Write lines to a StringBuilder. Separate each line with a newline ("\n" character).
     *
     * @param builder the StringBuilder to append the text to
     * @return an Output for storing text in a StringBuilder
     */
    public static Output<String, IOException> text( final StringBuilder builder )
    // END SNIPPET: method
    {
        return new Output<String, IOException>()
        {

            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends String, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                sender.sendTo( new Receiver<String, IOException>()
                {

                    @Override
                    public void receive( String item )
                        throws IOException
                    {
                        builder.append( item ).append( "\n" );
                    }

                } );
            }

        };
    }

    // START SNIPPET: method

    /**
     * Write ByteBuffer data to a file. If the writing or sending of data fails the file will be deleted.
     *
     * @param file
     * @param <T>
     *
     * @return
     */
    public static <T> Output<ByteBuffer, IOException> byteBuffer( final File file )
    // END SNIPPET: method
    {
        return new Output<ByteBuffer, IOException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends ByteBuffer, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                File tmpFile = Files.createTemporayFileOf( file );
                FileOutputStream stream = new FileOutputStream( tmpFile );
                final FileChannel fco = stream.getChannel();

                try
                {
                    sender.sendTo( new Receiver<ByteBuffer, IOException>()
                    {
                        @Override
                        public void receive( ByteBuffer item )
                            throws IOException
                        {
                            fco.write( item );
                        }
                    } );
                    stream.close();

                    // Replace file with temporary file
                    if( !file.exists() || file.delete() )
                    {
                        tmpFile.renameTo( file );
                    }
                }
                catch( IOException e )
                {
                    // We failed writing - close and delete
                    stream.close();
                    tmpFile.delete();
                }
                catch( Throwable senderThrowableType )
                {
                    // We failed writing - close and delete
                    stream.close();
                    tmpFile.delete();

                    throw (SenderThrowableType) senderThrowableType;
                }
            }
        };
    }

    // START SNIPPET: method

    /**
     * Write ByteBuffer data to an OutputStream.
     *
     * @param stream
     * @param <T>
     *
     * @return
     */
    public static <T> Output<ByteBuffer, IOException> byteBuffer( final OutputStream stream )
    // END SNIPPET: method
    {
        return new Output<ByteBuffer, IOException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends ByteBuffer, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                try
                {
                    sender.sendTo( new Receiver<ByteBuffer, IOException>()
                    {
                        @Override
                        public void receive( ByteBuffer item )
                            throws IOException
                        {
                            if( item.hasArray() )
                            {
                                stream.write( item.array(), item.arrayOffset(), item.limit() );
                            }
                            else
                            {
                                for( int i = 0; i < item.limit(); i++ )
                                {
                                    stream.write( item.get( i ) );
                                }
                            }
                        }
                    } );
                }
                finally
                {
                    stream.close();
                }
            }
        };
    }

    // START SNIPPET: method

    /**
     * Write byte array data to a file. If the writing or sending of data fails the file will be deleted.
     *
     * @param file
     * @param bufferSize
     * @param <T>
     *
     * @return
     */
    public static <T> Output<byte[], IOException> bytes( final File file, final int bufferSize )
    // END SNIPPET: method
    {
        return new Output<byte[], IOException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends byte[], SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                File tmpFile = Files.createTemporayFileOf( file );
                final OutputStream stream = new BufferedOutputStream( new FileOutputStream( tmpFile ), bufferSize );

                try
                {
                    sender.sendTo( new Receiver<byte[], IOException>()
                    {
                        @Override
                        public void receive( byte[] item )
                            throws IOException
                        {
                            stream.write( item );
                        }
                    } );
                    stream.close();

                    // Replace file with temporary file
                    if( !file.exists() || file.delete() )
                    {
                        tmpFile.renameTo( file );
                    }
                }
                catch( IOException e )
                {
                    // We failed writing - close and delete
                    stream.close();
                    file.delete();
                }
                catch( Throwable senderThrowableType )
                {
                    // We failed writing - close and delete
                    stream.close();
                    file.delete();

                    throw (SenderThrowableType) senderThrowableType;
                }
            }
        };
    }

    // START SNIPPET: method

    /**
     * Do nothing. Use this if you have all logic in filters and/or specifications
     *
     * @param <T>
     *
     * @return
     */
    public static <T> Output<T, RuntimeException> noop()
    // END SNIPPET: method
    {
        return withReceiver( new Receiver<T, RuntimeException>()
        {
            @Override
            public void receive( T item )
                throws RuntimeException
            {
                // Do nothing
            }
        } );
    }

    // START SNIPPET: method

    /**
     * Use given receiver as Output. Use this if there is no need to create a "transaction" for each transfer, and no need
     * to do batch writes or similar.
     *
     * @param <T>
     * @param receiver receiver for this Output
     *
     * @return
     */
    public static <T, ReceiverThrowableType extends Throwable> Output<T, ReceiverThrowableType> withReceiver( final Receiver<T, ReceiverThrowableType> receiver )
    // END SNIPPET: method
    {
        return new Output<T, ReceiverThrowableType>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends T, SenderThrowableType> sender )
                throws ReceiverThrowableType, SenderThrowableType
            {
                sender.sendTo( receiver );
            }
        };
    }

    // START SNIPPET: method

    /**
     * Write objects to System.out.println.
     *
     * @return
     */
    public static Output<Object, RuntimeException> systemOut()
    // END SNIPPET: method
    {
        return new Output<Object, RuntimeException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends Object, SenderThrowableType> sender )
                throws RuntimeException, SenderThrowableType
            {
                sender.sendTo( new Receiver<Object, RuntimeException>()
                {
                    @Override
                    public void receive( Object item )
                    {
                        System.out.println( item );
                    }
                } );
            }
        };
    }

    // START SNIPPET: method

    /**
     * Write objects to System.err.println.
     */
    public static Output<Object, RuntimeException> systemErr()
    // END SNIPPET: method
    {
        return new Output<Object, RuntimeException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends Object, SenderThrowableType> sender )
                throws RuntimeException, SenderThrowableType
            {
                sender.sendTo( new Receiver<Object, RuntimeException>()
                {
                    @Override
                    public void receive( Object item )
                    {
                        System.err.println( item );
                    }
                } );
            }
        };
    }

    // START SNIPPET: method

    /**
     * Add items to a collection
     */
    public static <T> Output<T, RuntimeException> collection( final Collection<T> collection )
    // END SNIPPET: method
    {
        return new Output<T, RuntimeException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends T, SenderThrowableType> sender )
                throws RuntimeException, SenderThrowableType
            {
                sender.sendTo( new Receiver<T, RuntimeException>()
                {
                    @Override
                    public void receive( T item )
                        throws RuntimeException
                    {
                        collection.add( item );
                    }
                } );
            }
        };
    }
}
