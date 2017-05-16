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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.structure.MetaInfoHolder;
import org.apache.polygene.api.type.HasTypes;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.util.Annotations;

import static org.apache.polygene.api.type.HasTypesCollectors.closestType;

/**
 * Serialization Converters.
 */
@Mixins( Converters.Mixin.class )
public interface Converters
{
    /**
     * Register a converter for a value type.
     *
     * @param valueType the value type
     * @param converter the converter
     */
    void registerConverter( ValueType valueType, Converter<?> converter );

    /**
     * Find a matching converter amongst registered ones.
     *
     * See {@link org.apache.polygene.api.type.HasTypesCollectors#closestType(HasTypes)}.
     *
     * @param valueType the value type
     * @param <T> the converted type
     * @return the closest matching registered converter, or {@literal null} if none
     */
    <T> Converter<T> converterFor( ValueType valueType );

    /**
     * Find a matching converter amongst registered ones.
     *
     * See {@link org.apache.polygene.api.type.HasTypesCollectors#closestType(HasTypes)}.
     *
     * @param type the value type
     * @param <T> the converted type
     * @return the closest matching registered converter, or {@literal null} if none
     */
    default <T> Converter<T> converterFor( Class<? extends T> type )
    {
        return converterFor( ValueType.of( type ) );
    }

    /**
     * Find converter registered as meta-info.
     *
     * Meta-info converters are registered either using {@link ConvertedBy} annotation or at assembly time.
     * The latter takes precedence over the former.
     *
     * @param metaInfoHolder the meta-info holder, e.g. a property descriptor
     * @param <T> the converted type
     * @return the registered converted, or {@literal null} if none
     */
    <T> Converter<T> converterFor( MetaInfoHolder metaInfoHolder );

    /**
     * Serialization Converters default Mixin.
     */
    class Mixin implements Converters
    {
        private final Map<ValueType, Converter<?>> converters = new LinkedHashMap<>();
        private final Map<ValueType, Converter<?>> resolvedConvertersCache = new HashMap<>();
        private final Map<Class<? extends Converter>, Converter<?>> convertersInstancesCache = new HashMap<>();

        @Override
        public void registerConverter( ValueType valueType, Converter<?> converter )
        {
            converters.put( valueType, converter );
            resolvedConvertersCache.put( valueType, converter );
        }

        @Override
        public <T> Converter<T> converterFor( ValueType valueType )
        {
            if( resolvedConvertersCache.containsKey( valueType ) )
            {
                return castConverter( resolvedConvertersCache.get( valueType ) );
            }
            Converter<T> converter = lookupConverter( valueType );
            resolvedConvertersCache.put( valueType, converter );
            return converter;
        }

        @SuppressWarnings( "unchecked" )
        private <T> Converter<T> lookupConverter( ValueType valueType )
        {
            Converter<T> converter = lookupConvertedByConverter( valueType );
            if( converter == null )
            {
                converter = castConverter( converters.keySet().stream()
                                                     .collect( closestType( valueType ) )
                                                     .map( converters::get )
                                                     .orElse( null ) );
            }
            if( converter == null && valueType.primaryType().isEnum() )
            {
                converter = new EnumConverter( valueType.primaryType() );
            }
            return converter;
        }

        @Override
        public <T> Converter<T> converterFor( MetaInfoHolder metaInfoHolder )
        {
            Converter converter = metaInfoHolder.metaInfo( Converter.class );
            if( converter != null )
            {
                return castConverter( converter );
            }
            ConvertedBy convertedBy = metaInfoHolder.metaInfo( ConvertedBy.class );
            if( convertedBy != null )
            {
                return converterInstanceOf( convertedBy.value() );
            }
            return null;
        }

        private <T> Converter<T> lookupConvertedByConverter( ValueType valueType )
        {
            ConvertedBy convertedBy = Annotations.annotationOn( valueType.primaryType(), ConvertedBy.class );
            if( convertedBy != null )
            {
                return converterInstanceOf( convertedBy.value() );
            }
            return null;
        }

        private <T> Converter<T> converterInstanceOf( Class<? extends Converter> converterClass )
        {
            if( convertersInstancesCache.containsKey( converterClass ) )
            {
                return castConverter( convertersInstancesCache.get( converterClass ) );
            }
            try
            {
                Converter<T> converter = castConverter( converterClass.newInstance() );
                convertersInstancesCache.put( converterClass, converter );
                return converter;
            }
            catch( InstantiationException | IllegalAccessException ex )
            {
                throw new SerializationException( "Unable to instantiate Converter: " + converterClass.getName(), ex );
            }
        }

        @SuppressWarnings( "unchecked" )
        private <T> Converter<T> castConverter( Converter<?> converter )
        {
            return (Converter<T>) converter;
        }

        private static class EnumConverter<E extends Enum<E>> implements Converter<E>
        {
            private final Class<E> enumType;
            private final Map<String, E> values;

            private EnumConverter( final Class<E> enumType )
            {
                this.enumType = enumType;
                E[] enumValues = enumType.getEnumConstants();
                this.values = new HashMap<>( enumValues.length );
                for( E enumValue : enumValues )
                {
                    values.put( enumValue.name(), enumValue );
                }
            }

            @Override
            public Class<E> type()
            {
                return enumType;
            }

            @Override
            public String toString( E object )
            {
                return object.name();
            }

            @Override
            public E fromString( String string )
            {
                return values.get( string );
            }
        }
    }
}
