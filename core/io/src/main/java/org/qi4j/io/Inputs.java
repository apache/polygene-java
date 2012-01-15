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

import org.qi4j.functional.Visitor;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

/**
 * Common inputs
 */
public class Inputs
{
    // START SNIPPET: method
    /**
     * Read lines from a UTF-8 encoded textfile.
     *
     * If the filename ends with .gz, then the data is automatically unzipped when read.
     *
     * @param source textfile with lines separated by \n character
     *
     * @return Input that provides lines from the textfiles as strings
     */
    public static Input<String, IOException> text( final File source )
    // END SNIPPET: method
    {
        return text( source, "UTF-8" );
    }

    // START SNIPPET: method
    /**
     * Read lines from a textfile with the given encoding.
     *
     * If the filename ends with .gz, then the data is automatically unzipped when read.
     *
     * @param source   textfile with lines separated by \n character
     * @param encoding encoding of file, e.g. "UTF-8"
     *
     * @return Input that provides lines from the textfiles as strings
     */
    public static Input<String, IOException> text( final File source, final String encoding )
    // END SNIPPET: method
    {
        return new Input<String, IOException>()
        {
           @Override
           public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super String, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
           {
                InputStream stream = new FileInputStream( source );

                // If file is gzipped, unzip it automatically
                if( source.getName().endsWith( ".gz" ) )
                {
                    stream = new GZIPInputStream( stream );
                }

                final BufferedReader reader = new BufferedReader( new InputStreamReader( stream, encoding ) );

                try
                {
                    output.receiveFrom( new Sender<String, IOException>()
                    {
                       @Override
                       public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super String, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                       {
                            String line;
                            while( ( line = reader.readLine() ) != null )
                            {
                                receiver.receive( line );
                            }
                        }
                    } );
                }
                finally
                {
                    reader.close();
                }
            }
        };
    }

    // START SNIPPET: method
    /**
     * Read lines from a textfile at a given URL.
     *
     * If the content support gzip encoding, then the data is automatically unzipped when read.
     *
     * The charset in the content-type of the URL will be used for parsing. Default is UTF-8.
     *
     * @param source textfile with lines separated by \n character
     *
     * @return Input that provides lines from the textfiles as strings
     */
    public static Input<String, IOException> text( final URL source )
    // END SNIPPET: method
    {
        return new Input<String, IOException>()
        {
           @Override
           public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super String, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
           {
                URLConnection urlConnection = source.openConnection();
                urlConnection.setRequestProperty( "Accept-Encoding", "gzip" );
                InputStream stream = urlConnection.getInputStream();

                // If file is gzipped, unzip it automatically
                if( "gzip".equals( urlConnection.getContentEncoding() ) )
                {
                    stream = new GZIPInputStream( stream );
                }

                // Figure out charset given content-type
                String contentType = urlConnection.getContentType();
                String charSet = "UTF-8";
                if( contentType.indexOf( "charset=" ) != -1 )
                {
                    charSet = contentType.substring( contentType.indexOf( "charset=" ) + "charset=".length() );
                }
                final BufferedReader reader = new BufferedReader( new InputStreamReader( stream, charSet ) );

                try
                {
                    output.receiveFrom( new Sender<String, IOException>()
                    {
                       @Override
                       public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super String, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                       {
                            String line;
                            while( ( line = reader.readLine() ) != null )
                            {
                                receiver.receive( line );
                            }
                        }
                    } );
                }
                finally
                {
                    reader.close();
                }
            }
        };
    }

    // START SNIPPET: method
    /**
     * Read a file using ByteBuffer of a given size. Useful for transferring raw data.
     *
     * @param source
     * @param bufferSize
     *
     * @return
     */
    public static Input<ByteBuffer, IOException> byteBuffer( final File source, final int bufferSize )
    // END SNIPPET: method
    {
        return new Input<ByteBuffer, IOException>()
        {
           @Override
           public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super ByteBuffer, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
           {
                final FileInputStream stream = new FileInputStream( source );
                final FileChannel fci = stream.getChannel();

                final ByteBuffer buffer = ByteBuffer.allocate( bufferSize );

                try
                {
                    output.receiveFrom( new Sender<ByteBuffer, IOException>()
                    {
                       @Override
                       public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super ByteBuffer, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                       {
                            while( fci.read( buffer ) != -1 )
                            {
                                buffer.flip();
                                receiver.receive( buffer );
                                buffer.clear();
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
     * Read an inputstream using ByteBuffer of a given size.
     *
     * @param source
     * @param bufferSize
     *
     * @return
     */
    public static Input<ByteBuffer, IOException> byteBuffer( final InputStream source, final int bufferSize )
    // END SNIPPET: method
    {
        return new Input<ByteBuffer, IOException>()
        {
           @Override
           public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super ByteBuffer, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
           {
                try
                {
                    output.receiveFrom( new Sender<ByteBuffer, IOException>()
                    {
                       @Override
                       public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super ByteBuffer, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                       {
                            byte[] buffer = new byte[ bufferSize ];

                            int len;
                            while( ( len = source.read( buffer ) ) != -1 )
                            {
                                ByteBuffer byteBuffer = ByteBuffer.wrap( buffer, 0, len );
                                receiver.receive( byteBuffer );
                            }
                        }
                    } );
                }
                finally
                {
                    source.close();
                }
            }
        };
    }

    // START SNIPPET: method
    /**
     * Combine many Input into one single Input. When a transfer is initiated from it all items from all inputs will be transferred
     * to the given Output.
     *
     * @param inputs
     * @param <T>
     * @param <SenderThrowableType>
     *
     * @return
     */
    public static <T, SenderThrowableType extends Throwable> Input<T, SenderThrowableType> combine( final Iterable<Input<T, SenderThrowableType>> inputs )
    // END SNIPPET: method
    {
        return new Input<T, SenderThrowableType>()
        {
           @Override
           public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super T, ReceiverThrowableType> output) throws SenderThrowableType, ReceiverThrowableType
           {
                output.receiveFrom( new Sender<T, SenderThrowableType>()
                {
                   @Override
                   public <ReceiverThrowableType extends Throwable> void sendTo(final Receiver<? super T, ReceiverThrowableType> receiver) throws ReceiverThrowableType, SenderThrowableType
                   {
                        for( Input<T, SenderThrowableType> input : inputs )
                        {
                            input.transferTo( new Output<T, ReceiverThrowableType>()
                            {
                               @Override
                               public <SenderThrowableType extends Throwable> void receiveFrom(Sender<? extends T, SenderThrowableType> sender) throws ReceiverThrowableType, SenderThrowableType
                               {
                                    sender.sendTo( new Receiver<T, ReceiverThrowableType>()
                                    {
                                        public void receive( T item )
                                            throws ReceiverThrowableType
                                        {
                                            receiver.receive( item );
                                        }
                                    } );
                                }
                            } );
                        }
                    }
                } );
            }
        };
    }

    // START SNIPPET: method
    /**
     * Create an Input that takes its items from the given Iterable.
     *
     * @param iterable
     * @param <T>
     * @return
     */
    public static <T> Input<T, RuntimeException> iterable( final Iterable<T> iterable )
    // END SNIPPET: method
    {
        return new Input<T, RuntimeException>()
        {
           @Override
           public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super T, ReceiverThrowableType> output) throws RuntimeException, ReceiverThrowableType
           {
                output.receiveFrom( new Sender<T, RuntimeException>()
                {
                   @Override
                   public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super T, ReceiverThrowableType> receiver) throws ReceiverThrowableType, RuntimeException
                   {
                        for( T item : iterable )
                        {
                            receiver.receive( item );
                        }
                    }
                } );
            }
        };
    }

    // START SNIPPET: method
    /**
     * Create an Input that allows a Visitor to write to an OutputStream. The stream is a BufferedOutputStream, so when enough
     * data has been gathered it will send this in chunks of the given size to the Output it is transferred to. The Visitor does not have to call
     * close() on the OutputStream, but should ensure that any wrapper streams or writers are flushed so that all data is sent.
     *
     * @param outputVisitor
     * @param bufferSize
     * @return
     */
    public static Input<ByteBuffer, IOException> output( final Visitor<OutputStream, IOException> outputVisitor, final int bufferSize)
    // END SNIPPET: method
    {
        return new Input<ByteBuffer, IOException>()
        {
           @Override
           public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super ByteBuffer, ReceiverThrowableType> output) throws IOException, ReceiverThrowableType
           {
                output.receiveFrom( new Sender<ByteBuffer, IOException>()
                {
                   @Override
                   public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super ByteBuffer, ReceiverThrowableType> receiver) throws ReceiverThrowableType, IOException
                   {
                       OutputStream out = new BufferedOutputStream(new OutputStream()
                       {
                           @Override
                           public void write( int b ) throws IOException
                           {
                               // Ignore
                           }

                           @Override
                           public void write( byte[] b, int off, int len ) throws IOException
                           {
                               try
                               {
                                   ByteBuffer byteBuffer = ByteBuffer.wrap( b, 0, len );
                                   receiver.receive( byteBuffer );
                               } catch( Throwable ex)
                               {
                                 throw new IOException( ex );
                               }
                           }
                       }, bufferSize);

                       try
                       {
                           outputVisitor.visit( out );
                       } catch (IOException ex)
                       {
                          throw (ReceiverThrowableType) ex.getCause();
                       } finally
                       {
                           out.close();
                       }
                   }
                } );
            }
        };
    }
}
