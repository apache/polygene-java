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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.CollectionType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.spi.module.ModuleSpi;

/**
 * Base Deserializer.
 *
 * Provides default implementations for convenience API methods.
 *
 * See {@link AbstractSerializer}.
 */
public abstract class AbstractDeserializer implements Deserializer
{
    protected static final ValueType ENTITY_REF_LIST_VALUE_TYPE = CollectionType.listOf( EntityReference.class );
    protected static final ValueType ENTITY_REF_MAP_VALUE_TYPE = MapType.of( String.class, EntityReference.class );

    @Override
    public <T> T deserialize( ModuleDescriptor module, ValueType valueType, String state )
    {
        return deserialize( module, valueType, new StringReader( state ) );
    }

    @Override
    public <T> Function<String, T> deserializeFunction( ModuleDescriptor module, ValueType valueType )
    {
        return state -> deserialize( module, valueType, new StringReader( state ) );
    }

    @Override
    public <T> Stream<T> deserializeEach( ModuleDescriptor module, ValueType valueType, Iterable<String> states )
    {
        return StreamSupport.stream( states.spliterator(), false )
                            .map( state -> deserialize( module, valueType, new StringReader( state ) ) );
    }

    @Override
    public <T> Stream<T> deserializeEach( ModuleDescriptor module, ValueType valueType, String... states )
    {
        return Stream.of( states ).map( state -> deserialize( module, valueType, new StringReader( state ) ) );
    }

    @Override
    public <T> T fromBytes( ModuleDescriptor module, ValueType valueType, byte[] bytes )
    {
        return deserialize( module, valueType, new ByteArrayInputStream( bytes ) );
    }

    @Override
    public <T> Function<byte[], T> fromBytesFunction( ModuleDescriptor module, ValueType valueType )
    {
        return bytes -> deserialize( module, valueType, new ByteArrayInputStream( bytes ) );
    }

    @Override
    public <T> Stream<T> fromBytesEach( ModuleDescriptor module, ValueType valueType, Iterable<byte[]> states )
    {
        return StreamSupport.stream( states.spliterator(), false )
                            .map( bytes -> deserialize( module, valueType, new ByteArrayInputStream( bytes ) ) );
    }

    @Override
    public <T> Stream<T> fromBytesEach( ModuleDescriptor module, ValueType valueType, byte[]... states )
    {
        return Stream.of( states ).map( bytes -> deserialize( module, valueType, new ByteArrayInputStream( bytes ) ) );
    }

    @Override
    public <T> T deserialize( ModuleDescriptor module, Class<T> type, InputStream state )
    {
        return deserialize( module, valueTypeOf( module, type ), state );
    }

    @Override
    public <T> T deserialize( ModuleDescriptor module, Class<T> type, Reader state )
    {
        return deserialize( module, valueTypeOf( module, type ), state );
    }

    @Override
    public <T> T deserialize( ModuleDescriptor module, Class<T> type, String state )
    {
        return deserialize( module, valueTypeOf( module, type ), state );
    }

    @Override
    public <T> Function<String, T> deserializeFunction( ModuleDescriptor module, Class<T> type )
    {
        return deserializeFunction( module, valueTypeOf( module, type ) );
    }

    @Override
    public <T> Stream<T> deserializeEach( ModuleDescriptor module, Class<T> type, Iterable<String> states )
    {
        return deserializeEach( module, valueTypeOf( module, type ), states );
    }

    @Override
    public <T> Stream<T> deserializeEach( ModuleDescriptor module, Class<T> type, String... states )
    {
        return deserializeEach( module, valueTypeOf( module, type ), states );
    }

    @Override
    public <T> T fromBytes( ModuleDescriptor module, Class<T> type, byte[] bytes )
    {
        return fromBytes( module, valueTypeOf( module, type ), bytes );
    }

    @Override
    public <T> Function<byte[], T> fromBytesFunction( ModuleDescriptor module, Class<T> type )
    {
        return fromBytesFunction( module, valueTypeOf( module, type ) );
    }

    @Override
    public <T> Stream<T> fromBytesEach( ModuleDescriptor module, Class<T> type, Iterable<byte[]> states )
    {
        return fromBytesEach( module, valueTypeOf( module, type ), states );
    }

    @Override
    public <T> Stream<T> fromBytesEach( ModuleDescriptor module, Class<T> type, byte[]... states )
    {
        return fromBytesEach( module, valueTypeOf( module, type ), states );
    }

    private ValueType valueTypeOf( ModuleDescriptor module, Class<?> type )
    {
        // TODO Remove (ModuleSpi) cast
        return ( (ModuleSpi) module.instance() ).valueTypeFactory().valueTypeOf( module, type );
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
