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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.GZIPInputStream;

/**
 * Common inputs
 */
public class Inputs
{
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
    {
        return text( source, "UTF-8" );
    }

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
    {
        return new Input<String, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<String, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
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
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<String, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, IOException
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
    {
        return new Input<String, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<String, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
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
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<String, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, IOException
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

    /**
     * Read a file using ByteBuffer of a given size. Useful for transferring byte data form one input to another.
     *
     * @param source
     * @param bufferSize
     *
     * @return
     */
    public static Input<ByteBuffer, IOException> byteBuffer( final File source, final int bufferSize )
    {
        return new Input<ByteBuffer, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<ByteBuffer, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                final FileInputStream stream = new FileInputStream( source );
                final FileChannel fci = stream.getChannel();

                final ByteBuffer buffer = ByteBuffer.allocate( bufferSize );

                try
                {
                    output.receiveFrom( new Sender<ByteBuffer, IOException>()
                    {
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<ByteBuffer, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, IOException
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

    /**
     * Read an inputstream using ByteBuffer of a given size.
     *
     * @param source
     * @param bufferSize
     *
     * @return
     */
    public static Input<ByteBuffer, IOException> byteBuffer( final InputStream source, final int bufferSize )
    {
        return new Input<ByteBuffer, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<ByteBuffer, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                try
                {
                    output.receiveFrom( new Sender<ByteBuffer, IOException>()
                    {
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<ByteBuffer, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, IOException
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
    {
        return new Input<T, SenderThrowableType>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<T, ReceiverThrowableType> output )
                throws SenderThrowableType, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<T, SenderThrowableType>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<T, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, SenderThrowableType
                    {
                        for( Input<T, SenderThrowableType> input : inputs )
                        {
                            input.transferTo( new Output<T, ReceiverThrowableType>()
                            {
                                public <SenderThrowableType extends Throwable> void receiveFrom( Sender<T, SenderThrowableType> sender )
                                    throws ReceiverThrowableType, SenderThrowableType
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

    public static <T> Input<T, RuntimeException> iterable( final Iterable<T> iterable )
    {
        return new Input<T, RuntimeException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<T, ReceiverThrowableType> output )
                throws ReceiverThrowableType, RuntimeException
            {
                output.receiveFrom( new Sender<T, RuntimeException>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<T, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, RuntimeException
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
}
