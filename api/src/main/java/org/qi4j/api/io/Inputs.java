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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Common inputs
 */
public class Inputs
{
    /**
     * Read lines from a textfile
     *
     * @param source textfile with lines separated by \n character
     * @return Input that provides lines from the textfiles as strings
     */
    public static Input<String, IOException> text( final File source )
    {
        return new Input<String, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<String, ReceiverThrowableType> output ) throws IOException, ReceiverThrowableType
            {
                final BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( source ), "UTF-8" ) );

                try
                {
                    output.receiveFrom( new Sender<String, IOException>()
                    {
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<String, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, IOException
                        {
                            String line;
                            while ((line = reader.readLine()) != null)
                            {
                                receiver.receive( line );
                            }
                        }
                    } );
                } finally
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
     * @return
     */
    public static Input<ByteBuffer, IOException> byteBuffer( final File source, final int bufferSize )
    {
        return new Input<ByteBuffer, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<ByteBuffer, ReceiverThrowableType> output ) throws IOException, ReceiverThrowableType
            {
                final FileInputStream stream = new FileInputStream( source );
                final FileChannel fci = stream.getChannel();

                final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

                try
                {
                    output.receiveFrom( new Sender<ByteBuffer, IOException>()
                    {
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<ByteBuffer, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, IOException
                        {
                            while (fci.read( buffer ) != -1)
                            {
                                buffer.flip();
                                receiver.receive( buffer );
                                buffer.clear();
                            }
                        }
                    } );
                } finally
                {
                    stream.close();
                }
            }
        };
    }
}
