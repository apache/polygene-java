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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.rmi.RemoteException;

/**
 * Test Input/Output
 */
public class InputOutputTest
{
    @Test
    public void testInputOutput() throws IOException
    {
        Inputs.text( new File( getClass().getResource( "/iotest.txt" ).getFile() ) ).
                transferTo( Outputs.systemOut() );
    }

    @Test
    public void testCopyFile() throws IOException
    {
        File source = new File( getClass().getResource( "/iotest.txt" ).getFile() );
        File tempFile = File.createTempFile( "test", ".txt" );
        tempFile.deleteOnExit();

        Inputs.byteBuffer( source, 1024 ).transferTo( Outputs.byteBuffer(tempFile ));

        Assert.assertThat( tempFile.length(), CoreMatchers.equalTo( source.length() ) );
    }

    @Test
    public void testLog() throws IOException
    {
        File source = new File( getClass().getResource( "/iotest.txt" ).getFile() );

        Inputs.text( source ).transferTo( Transforms.map( new Transforms.Log<String>( LoggerFactory.getLogger( getClass() ), "Line: {0}"), Outputs.<String, RuntimeException>noop()));
    }

    @Test
    public void testTextInputsOutputs() throws IOException
    {
        File tempFile = File.createTempFile( "test", ".txt" );
        tempFile.deleteOnExit();
        File sourceFile = new File( getClass().getResource( "/iotest.txt" ).getFile() );
        Transforms.Counter<String> stringCounter = new Transforms.Counter<String>();
        Inputs.text( sourceFile ).transferTo(
                Transforms.map( stringCounter,
                        Transforms.map( new Transforms.Function<String, String>()
                        {
                            public String map( String s )
                            {
                                System.out.println( s );
                                return s;
                            }
                        },
                                Outputs.text( tempFile ) ) ) );

        Assert.assertThat( tempFile.length(), CoreMatchers.equalTo( sourceFile.length() ) );
        Assert.assertThat( stringCounter.getCount(), CoreMatchers.equalTo( 4L ) );
    }

    @Test
    public void testCombineInputs() throws IOException
    {
        File tempFile = File.createTempFile( "test", ".txt" );
        tempFile.deleteOnExit();
        File sourceFile = new File( getClass().getResource( "/iotest.txt" ).getFile() );
        Transforms.Counter<String> stringCounter = new Transforms.Counter<String>();
        Transforms.combine( Inputs.text( sourceFile ), Inputs.text( sourceFile ) ).transferTo(
                Transforms.map( stringCounter,
                        Transforms.map( new Transforms.Function<String, String>()
                        {
                            public String map( String s )
                            {
                                System.out.println( s );
                                return s;
                            }
                        },
                                Outputs.text( tempFile ) ) ) );

        Assert.assertThat( tempFile.length(), CoreMatchers.equalTo( sourceFile.length() * 2 ) );
        Assert.assertThat( stringCounter.getCount(), CoreMatchers.equalTo( 8L ) );
    }

    @Test(expected = IOException.class)
    public void testInputOutputOutputException() throws IOException
    {

        Inputs.text( new File(getClass().getResource( "/iotest.txt" ).getFile()) ).
                transferTo( writerOutput( new Writer()
                {
                    @Override
                    public void write( char[] cbuf, int off, int len ) throws IOException
                    {
                        throw new IOException();
                    }

                    @Override
                    public void flush() throws IOException
                    {
                        throw new IOException();
                    }

                    @Override
                    public void close() throws IOException
                    {
                        throw new IOException();
                    }
                } ) );
    }

    @Test(expected = RemoteException.class)
    public void testInputOutputInputException() throws IOException
    {

        Input<String, RemoteException> input = new Input<String, RemoteException>()
        {
            public <OutputThrowableType extends Throwable> void transferTo( Output<String, OutputThrowableType> output ) throws RemoteException, OutputThrowableType
            {
                output.receiveFrom( new Sender<String, RemoteException>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<String, ReceiverThrowableType> stringReceiverThrowableTypeReceiver ) throws ReceiverThrowableType, RemoteException
                    {
                        throw new RemoteException();
                    }
                } );
            }
        };

        input.transferTo( Transforms.map( new Transforms.Log<String>( LoggerFactory.getLogger( getClass() ), "Line: {0}"),
                Outputs.systemOut() ));
    }

    public Output<String, IOException> writerOutput( final Writer writer )
    {
        return new Output<String, IOException>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<String, SenderThrowableType> sender )
                    throws IOException, SenderThrowableType
            {
                // Here we initiate the transfer
                System.out.println( "Open output" );
                final StringBuilder builder = new StringBuilder();
                try
                {
                    sender.sendTo( new Receiver<String, IOException>()
                    {
                        public void receive( String item ) throws IOException
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
                } catch (IOException e)
                {
                    // If transfer failed, potentially rollback writes
                    System.out.println( "Input failed" );
                    throw e;
                }
            }
        };
    }
}
