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
package org.apache.polygene.serialization.messagepack;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.composite.StatefulAssociationCompositeDescriptor;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.serialization.ConvertedBy;
import org.apache.polygene.api.serialization.Converter;
import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.serialization.Serializer;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.type.ArrayType;
import org.apache.polygene.api.type.EnumType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.StatefulAssociationValueType;
import org.apache.polygene.spi.serialization.AbstractBinarySerializer;
import org.apache.polygene.spi.util.ArrayIterable;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

import static java.util.stream.Collectors.toList;
import static org.apache.polygene.api.util.Collectors.toMap;

@Mixins( MessagePackSerializer.Mixin.class )
public interface MessagePackSerializer extends Serializer
{
    class Mixin extends AbstractBinarySerializer
    {
        @This
        private Converters converters;

        @This
        private MessagePackAdapters adapters;

        @Structure
        private Module module;

        @Override
        public void serialize( Options options, OutputStream output, @Optional Object object )
        {
            try( MessagePacker packer = MessagePack.newDefaultPacker( output ) )
            {
                Value value = doSerialize( options, object, true );
                packer.packValue( value );
                packer.flush();
            }
            catch( IOException ex )
            {
                throw new SerializationException( "Unable to serialize " + object, ex );
            }
        }

        private Value doSerialize( Options options, Object object, boolean root )
        {
            try
            {
                if( object == null )
                {
                    return ValueFactory.newNil();
                }
                Class<?> objectClass = object.getClass();
                Converter<Object> converter = converters.converterFor( objectClass );
                if( converter != null )
                {
                    return doSerialize( options, converter.toString( object ), false );
                }
                MessagePackAdapter<?> adapter = adapters.adapterFor( objectClass );
                if( adapter != null )
                {
                    return adapter.serialize( object, obj -> doSerialize( options, obj, false ) );
                }
                if( EnumType.isEnum( objectClass ) )
                {
                    return ValueFactory.newString( object.toString() );
                }
                if( StatefulAssociationValueType.isStatefulAssociationValue( objectClass ) )
                {
                    return serializeStatefulAssociationValue( options, object, root );
                }
                if( MapType.isMap( objectClass ) )
                {
                    return serializeMap( options, (Map<?, ?>) object );
                }
                if( ArrayType.isArray( objectClass ) )
                {
                    return serializeArray( options, object );
                }
                if( Iterable.class.isAssignableFrom( objectClass ) )
                {
                    return serializeIterable( options, (Iterable<?>) object );
                }
                if( Stream.class.isAssignableFrom( objectClass ) )
                {
                    return serializeStream( options, (Stream<?>) object );
                }
                throw new SerializationException( "Don't know how to serialize " + object );
            }
            catch( IOException ex )
            {
                throw new SerializationException( "Unable to serialize " + object, ex );
            }
        }

        private MapValue serializeStatefulAssociationValue( Options options, Object composite, boolean root )
        {
            CompositeInstance instance = PolygeneAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( (Composite) composite );
            StatefulAssociationCompositeDescriptor descriptor =
                (StatefulAssociationCompositeDescriptor) instance.descriptor();
            AssociationStateHolder state = (AssociationStateHolder) instance.state();
            StatefulAssociationValueType<?> valueType = descriptor.valueType();

            ValueFactory.MapBuilder builder = ValueFactory.newMapBuilder();
            valueType.properties().forEach(
                property ->
                {
                    Object value = state.propertyFor( property.accessor() ).get();
                    ConvertedBy convertedBy = property.metaInfo( ConvertedBy.class );
                    if( convertedBy != null )
                    {
                        value = module.newObject( convertedBy.value() ).toString( value );
                    }
                    builder.put(
                        ValueFactory.newString( property.qualifiedName().name() ),
                        doSerialize( options, value, false ) );
                } );
            valueType.associations().forEach(
                association -> builder.put(
                    ValueFactory.newString( association.qualifiedName().name() ),
                    doSerialize( options, state.associationFor( association.accessor() ).reference(), false ) ) );
            valueType.manyAssociations().forEach(
                association -> builder.put(
                    ValueFactory.newString( association.qualifiedName().name() ),
                    doSerialize( options,
                                 state.manyAssociationFor( association.accessor() ).references().collect( toList() ),
                                 false ) ) );
            valueType.namedAssociations().forEach(
                association -> builder.put(
                    ValueFactory.newString( association.qualifiedName().name() ),
                    doSerialize( options,
                                 state.namedAssociationFor( association.accessor() ).references().collect( toMap() ),
                                 false ) ) );

            if( ( root && options.rootTypeInfo() ) || ( !root && options.nestedTypeInfo() ) )
            {
                builder.put( ValueFactory.newString( "_type" ),
                             ValueFactory.newString( valueType.primaryType().getName() ) );
            }
            return builder.build();
        }

        private MapValue serializeMap( Options options, Map<?, ?> map )
        {
            ValueFactory.MapBuilder builder = ValueFactory.newMapBuilder();
            map.forEach( ( key, value ) -> builder.put( doSerialize( options, key, false ),
                                                        doSerialize( options, value, false ) ) );
            return builder.build();
        }

        private Value serializeArray( Options options, Object object )
        {
            ArrayType valueType = ArrayType.of( object.getClass() );
            if( valueType.isArrayOfPrimitiveBytes() )
            {
                return ValueFactory.newBinary( (byte[]) object );
            }
            if( valueType.isArrayOfPrimitives() )
            {
                return serializeIterable( options, new ArrayIterable( object ) );
            }
            return serializeStream( options, Stream.of( (Object[]) object ) );
        }

        private ArrayValue serializeIterable( Options options, Iterable<?> iterable )
        {
            return serializeStream( options, StreamSupport.stream( iterable.spliterator(), false ) );
        }

        private ArrayValue serializeStream( Options options, Stream<?> stream )
        {
            return ValueFactory.newArray( stream.map( element -> doSerialize( options, element, false ) )
                                                .collect( toList() ) );
        }
    }
}
