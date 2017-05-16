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
package org.apache.polygene.api.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A Converter based on Java Serialization.
 *
 * Note that this Converter works with String representations.
 * The serialized payload is encoded using Base64.
 * This is suitable for text based formats, if you are using a binary format, see the serialization extension.
 *
 * Prefer using it with the {@link ConvertedBy} annotation.
 * If you register it as a serialization converter, it will catch all types.
 * Or you could extend it and override {@link #type()}.
 */
public class JavaSerializationConverter implements Converter<Object>
{
    @Override
    public Class<Object> type()
    {
        return Object.class;
    }

    @Override
    public String toString( Object object )
    {
        byte[] bytes = Base64.getEncoder().encode( serializeJava( object ) );
        return new String( bytes, UTF_8 );
    }

    @Override
    public Object fromString( String string )
    {
        byte[] bytes = Base64.getDecoder().decode( string );
        return deserializeJava( bytes );
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

    protected Object deserializeJava( byte[] bytes )
    {
        try( ObjectInputStream oin = new ObjectInputStream( new ByteArrayInputStream( bytes ) ) )
        {
            return oin.readObject();
        }
        catch( IOException | ClassNotFoundException ex )
        {
            throw new SerializationException( "Unable to deserialize using Java serialization", ex );
        }
    }
}
