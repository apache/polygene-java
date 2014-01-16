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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.functional.Function;
import org.qi4j.functional.Visitor;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.io.Inputs.text;
import static org.qi4j.io.Transforms.lock;
import static org.qi4j.test.util.Assume.assumeConnectivity;

/**
 * Test Input/Output.
 */
public class InputOutputTest
{
    @Test
    public void testCopyFileNoAPI()
        throws IOException
    {
        File source = sourceFile();
        File destination = File.createTempFile( "test", ".txt" );
        destination.deleteOnExit();

        BufferedReader reader = new BufferedReader( new FileReader( source ) );
        long count = 0;
        try
        {
            BufferedWriter writer = new BufferedWriter( new FileWriter( destination ) );
            try
            {
                String line;
                while( ( line = reader.readLine() ) != null )
                {
                    count++;
                    writer.append( line ).append( '\n' );
                }
                writer.close();
            }
            catch( IOException e )
            {
                writer.close();
                destination.delete();
            }
        }
        finally
        {
            reader.close();
        }
        System.out.println( count );
    }

    @Test
    public void testInputOutput()
        throws IOException
    {
        URL source = getClass().getResource( "/iotest.txt" );
        File destination = File.createTempFile( "test", ".txt" );
        destination.deleteOnExit();
        text( source ).transferTo( Outputs.text( destination ) );
    }

    @Test
    public void testCopyFile()
        throws IOException
    {
        File source = sourceFile();
        File tempFile = File.createTempFile( "test", ".txt" );
        tempFile.deleteOnExit();

        Inputs.byteBuffer( source, 1024 ).transferTo( Outputs.byteBuffer( tempFile ) );

        Assert.assertThat( tempFile.length(), CoreMatchers.equalTo( source.length() ) );
    }

    @Test
    public void testCopyURL()
        throws IOException
    {
        assumeConnectivity( "www.google.com", 80 );

        File tempFile = File.createTempFile( "test", ".txt" );
        tempFile.deleteOnExit();

        Inputs.text( new URL( "http://www.google.com" ) ).transferTo( Outputs.text( tempFile ) );

// Uncomment to check output        Inputs.text( tempFile ).transferTo( Outputs.systemOut() );
    }

    @Test
    public void testCopyFileStreams()
        throws IOException
    {
        File source = sourceFile();
        File tempFile = File.createTempFile( "test", ".txt" );
        tempFile.deleteOnExit();

        Inputs.byteBuffer( new FileInputStream( source ), 1024 ).transferTo(
            Outputs.byteBuffer( new FileOutputStream( tempFile ) ) );

        Assert.assertThat( tempFile.length(), CoreMatchers.equalTo( source.length() ) );
    }

    @Test
    public void testLog()
        throws IOException
    {
        File source = sourceFile();

        text( source ).transferTo(
            Transforms.map( new Transforms.Log<String>( LoggerFactory.getLogger( getClass() ), "Line: {0}" ),
                            Outputs.<String>noop() ) );
    }

    @Test
    public void testProgressLog()
        throws Throwable
    {
        Integer[] data = new Integer[ 105 ];
        Arrays.fill( data, 42 );

        Inputs.iterable( iterable( data ) ).transferTo(
            Transforms.map(
                new Transforms.ProgressLog<Integer>(
                    LoggerFactory.getLogger( InputOutputTest.class ), "Data transferred: {0}", 10 ),
                Outputs.<Integer>noop() ) );
    }

    @Test
    public void testTextInputsOutputs()
        throws IOException
    {
        File tempFile = File.createTempFile( "test", ".txt" );
        tempFile.deleteOnExit();
        File sourceFile = sourceFile();
        Transforms.Counter<String> stringCounter = new Transforms.Counter<>();
        text( sourceFile ).transferTo(
            Transforms.map(
                stringCounter,
                Transforms.map( new Function<String, String>()
                {
                    public String map( String s )
                    {
                        System.out.println( s );
                        return s;
                    }
                }, Outputs.text( tempFile ) )
            )
        );

        Assert.assertThat( tempFile.length(), CoreMatchers.equalTo( sourceFile.length() ) );
        Assert.assertThat( stringCounter.count(), CoreMatchers.equalTo( 4L ) );
    }

    @Test
    public void testCombineInputs()
        throws IOException
    {
        File tempFile = File.createTempFile( "test", ".txt" );
        tempFile.deleteOnExit();
        File sourceFile = sourceFile();
        Transforms.Counter<String> stringCounter = new Transforms.Counter<>();
        Input<String, IOException> text1 = text( sourceFile );
        Input<String, IOException> text2 = text( sourceFile );
        List<Input<String, IOException>> list = createList( text1, text2 );
        Inputs.combine( list ).transferTo(
            Transforms.map(
                stringCounter,
                Transforms.map( new Function<String, String>()
            {
                public String map( String s )
                {
                    System.out.println( s );
                    return s;
                }
                }, Outputs.text( tempFile ) )
            )
        );

        Assert.assertThat( tempFile.length(), CoreMatchers.equalTo( sourceFile.length() * 2 ) );
        Assert.assertThat( stringCounter.count(), CoreMatchers.equalTo( 8L ) );
    }

    @SuppressWarnings( "unchecked" )
    private List<Input<String, IOException>> createList( Input<String, IOException> text1,
                                                         Input<String, IOException> text2
    )
    {
        return asList( text1, text2 );
    }

    @Test( expected = IOException.class )
    public void testInputOutputOutputException()
        throws IOException
    {

        text( sourceFile() ).
            transferTo( writerOutput( new Writer()
                    {
                        @Override
                        public void write( char[] cbuf, int off, int len )
                        throws IOException
                        {
                            throw new IOException();
                        }

                        @Override
                        public void flush()
                        throws IOException
                        {
                            throw new IOException();
                        }

                        @Override
                        public void close()
                        throws IOException
                        {
                            throw new IOException();
                        }
            } ) );
    }

    @Test( expected = RemoteException.class )
    public void testInputOutputInputException()
        throws IOException
    {

        Input<String, RemoteException> input = new Input<String, RemoteException>()
        {
            @Override
            public <OutputThrowableType extends Throwable> void transferTo( Output<? super String, OutputThrowableType> output )
                throws RemoteException, OutputThrowableType
            {
                output.receiveFrom( new Sender<String, RemoteException>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super String, ReceiverThrowableType> receiverThrowableTypeReceiver )
                        throws ReceiverThrowableType, RemoteException
                    {
                        throw new RemoteException();
                    }
                } );
            }
        };

        input.transferTo(
            Transforms.map(
                new Transforms.Log<String>( LoggerFactory.getLogger( getClass() ), "Line: {0}" ),
                Outputs.systemOut()
            )
        );
    }

    @Test
    public void testLock()
        throws IOException
    {
        Lock inputLock = new ReentrantLock();
        Lock outputLock = new ReentrantLock();

        URL source = getClass().getResource( "/iotest.txt" );
        File destination = File.createTempFile( "test", ".txt" );
        destination.deleteOnExit();
        lock( inputLock, text( source ) ).transferTo( lock( outputLock, Outputs.text( destination ) ) );
    }

    @Test
    public void testGenerics()
    {
        ArrayList<Object> objects = new ArrayList<>( 3 );
        Inputs.iterable( Arrays.asList( "Foo", "Bar", "Xyzzy" ) ).transferTo( Outputs.collection( objects ) );

        Inputs.iterable( objects ).transferTo( Outputs.systemOut() );
    }

    @Test
    public void testOutputstreamInput()
        throws Throwable
    {
        Input<ByteBuffer, IOException> input = Inputs.output( new Visitor<OutputStream, IOException>()
        {
            @Override
            public boolean visit( OutputStream visited )
                throws IOException
            {
                try( PrintWriter writer = new PrintWriter( visited ) )
                {
                    writer.print( "Hello World!" );
                }
                return true;
            }
        }, 256 );

        input.transferTo( Transforms.map( new Transforms.ByteBuffer2String( Charset.defaultCharset() ), Outputs.systemOut() ) );
        input.transferTo( Transforms.map( new Transforms.ByteBuffer2String( Charset.defaultCharset() ), Outputs.systemOut() ) );
    }

    public Output<String, IOException> writerOutput( final Writer writer )
    {
        return new Output<String, IOException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends String, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                // Here we initiate the transfer
                System.out.println( "Open output" );
                final StringBuilder builder = new StringBuilder();
                try
                {
                    sender.sendTo( new Receiver<String, IOException>()
                    {
                        @Override
                        public void receive( String item )
                            throws IOException
                        {
                            System.out.println( "Receive input" );

                            // Here we can do batch writes if needed
                            builder.append( item ).append( "\n" );
                        }
                    } );

                    // If transfer went well, do something with it
                    writer.write( builder.toString() );
                    writer.flush();
                    System.out.println( "Output written" );
                }
                catch( IOException e )
                {
                    // If transfer failed, potentially rollback writes
                    System.out.println( "Input failed" );
                    throw e;
                }
            }
        };
    }

    private File sourceFile()
    {
        String path = getClass().getResource( "/iotest.txt" ).getFile();
        return new File( path.replaceAll( "%20", " " ) );
    }
}
