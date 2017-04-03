/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.spi.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.serialization.Serializer;

/**
 * Base Serializer.
 *
 * Provides default implementations for convenience API methods.
 *
 * See {@link AbstractDeserializer}.
 */
public abstract class AbstractSerializer implements Serializer
{
    @Override
    public void serialize( Writer writer, @Optional Object object )
    {
        serialize( Options.DEFAULT, writer, object );
    }

    @Override
    public void serialize( OutputStream output, @Optional Object object )
    {
        serialize( Options.DEFAULT, output, object );
    }

    @Override
    public String serialize( Options options, @Optional Object object )
    {
        StringWriter writer = new StringWriter();
        serialize( options, writer, object );
        return writer.toString();
    }

    @Override
    public String serialize( @Optional Object object )
    {
        return serialize( Options.DEFAULT, object );
    }

    @Override
    public <T> Function<T, String> serializeFunction( Options options )
    {
        return object -> serialize( options, object );
    }

    @Override
    public <T> Function<T, String> serializeFunction()
    {
        return object -> serialize( Options.DEFAULT, object );
    }

    @Override
    public Stream<String> serializeEach( Options options, Iterable<Object> objects )
    {
        return StreamSupport.stream( objects.spliterator(), false )
                            .map( object -> serialize( options, object ) );
    }

    @Override
    public Stream<String> serializeEach( Iterable<Object> objects )
    {
        return StreamSupport.stream( objects.spliterator(), false )
                            .map( object -> serialize( Options.DEFAULT, object ) );
    }

    @Override
    public Stream<String> serializeEach( Options options, Object... objects )
    {
        return Stream.of( objects ).map( object -> serialize( options, object ) );
    }

    @Override
    public Stream<String> serializeEach( Object... objects )
    {
        return Stream.of( objects ).map( object -> serialize( Options.DEFAULT, object ) );
    }

    @Override
    public byte[] toBytes( Options options, @Optional Object object )
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serialize( options, output, object );
        return output.toByteArray();
    }

    @Override
    public byte[] toBytes( @Optional Object object )
    {
        return toBytes( Options.DEFAULT, object );
    }

    @Override
    public <T> Function<T, byte[]> toBytesFunction( Options options )
    {
        return object -> toBytes( options, object );
    }

    @Override
    public <T> Function<T, byte[]> toBytesFunction()
    {
        return object -> toBytes( Options.DEFAULT, object );
    }

    @Override
    public Stream<byte[]> toBytesEach( Options options, Iterable<Object> objects )
    {
        return StreamSupport.stream( objects.spliterator(), false )
                            .map( object -> toBytes( options, object ) );
    }

    @Override
    public Stream<byte[]> toBytesEach( Iterable<Object> objects )
    {
        return StreamSupport.stream( objects.spliterator(), false )
                            .map( object -> toBytes( Options.DEFAULT, object ) );
    }

    @Override
    public Stream<byte[]> toBytesEach( Options options, Object... objects )
    {
        return Stream.of( objects ).map( object -> toBytes( options, object ) );
    }

    @Override
    public Stream<byte[]> toBytesEach( Object... objects )
    {
        return Stream.of( objects ).map( object -> toBytes( Options.DEFAULT, object ) );
    }

    protected byte[] serializeJava( Object object )
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try( ObjectOutputStream out = new ObjectOutputStream( bout ) )
        {
            out.writeUnshared( object );
            out.flush();
            return bout.toByteArray();
        }
        catch( IOException ex )
        {
            throw new SerializationException( "Unable to serialize using Java serialization", ex );
        }
    }
}
