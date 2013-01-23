/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2010, Niclas Hehdman. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.EnumType;
import org.qi4j.api.type.MapType;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.util.Base64Encoder;
import org.qi4j.api.util.Dates;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.api.value.ValueDeserializer;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.functional.Function;
import org.qi4j.functional.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.functional.Iterables.*;

/**
 *
 * @param <InputType> Implementor pull-parsing reader type
 * @param <InputNodeType> Implementor tree-parsing reader type
 */
public abstract class ValueDeserializerAdapter<InputType, InputNodeType>
    implements ValueDeserializer
{

    private static final Logger PULL_PARSING_LOG = LoggerFactory.getLogger( ValueDeserializerAdapter.class.getName() + "#PullParsing" );
    private static final Logger TREE_PARSING_LOG = LoggerFactory.getLogger( ValueDeserializerAdapter.class.getName() + "#TreeParsing" );

    protected static <T> Function<Object, T> identityDeserializer()
    {
        return new Function<Object, T>()
        {
            @Override
            @SuppressWarnings( "unchecked" )
            public T map( Object serialized )
            {
                return (T) serialized;
            }
        };
    }
    private final Map<Class<?>, Function<Object, Object>> deserializers = new HashMap<Class<?>, Function<Object, Object>>();
    private final Module module;

    @SuppressWarnings( "unchecked" )
    protected final <T> void registerDeserializer( Class<T> type, Function<Object, T> deserializer )
    {
        deserializers.put( type, (Function<Object, Object>) deserializer );
    }

    public ValueDeserializerAdapter( @Structure Module module )
    {
        this.module = module;
        // Primitive types
        registerDeserializer( String.class, new Function<Object, String>()
        {
            @Override
            public String map( Object o )
            {
                return o.toString();
            }
        } );
        registerDeserializer( Boolean.class, ValueDeserializerAdapter.<Boolean>identityDeserializer() );
        registerDeserializer( Integer.class, new Function<Object, Integer>()
        {
            @Override
            public Integer map( Object o )
            {
                return ( (Number) o ).intValue();
            }
        } );
        registerDeserializer( Long.class, new Function<Object, Long>()
        {
            @Override
            public Long map( Object o )
            {
                return ( (Number) o ).longValue();
            }
        } );
        registerDeserializer( Short.class, new Function<Object, Short>()
        {
            @Override
            public Short map( Object o )
            {
                return ( (Number) o ).shortValue();
            }
        } );
        registerDeserializer( Byte.class, new Function<Object, Byte>()
        {
            @Override
            public Byte map( Object o )
            {
                return ( (Number) o ).byteValue();
            }
        } );
        registerDeserializer( Float.class, new Function<Object, Float>()
        {
            @Override
            public Float map( Object o )
            {
                return ( (Number) o ).floatValue();
            }
        } );
        registerDeserializer( Double.class, new Function<Object, Double>()
        {
            @Override
            public Double map( Object o )
            {
                return ( (Number) o ).doubleValue();
            }
        } );

        // Number types
        registerDeserializer( BigDecimal.class, new Function<Object, BigDecimal>()
        {
            @Override
            public BigDecimal map( Object json )
            {
                return new BigDecimal( json.toString() );
            }
        } );
        registerDeserializer( BigInteger.class, new Function<Object, BigInteger>()
        {
            @Override
            public BigInteger map( Object json )
            {
                return new BigInteger( json.toString() );
            }
        } );

        // Date types
        registerDeserializer( Date.class, new Function<Object, Date>()
        {
            @Override
            public Date map( Object json )
            {
                return Dates.fromString( json.toString() );
            }
        } );
        registerDeserializer( DateTime.class, new Function<Object, DateTime>()
        {
            @Override
            public DateTime map( Object json )
            {
                return new DateTime( json, DateTimeZone.UTC );
            }
        } );
        registerDeserializer( LocalDateTime.class, new Function<Object, LocalDateTime>()
        {
            @Override
            public LocalDateTime map( Object json )
            {
                return new LocalDateTime( json );
            }
        } );
        registerDeserializer( LocalDate.class, new Function<Object, LocalDate>()
        {
            @Override
            public LocalDate map( Object json )
            {
                return new LocalDate( json );
            }
        } );

        // Other supported types
        registerDeserializer( EntityReference.class, new Function<Object, EntityReference>()
        {
            @Override
            public EntityReference map( Object json )
            {
                return EntityReference.parseEntityReference( json.toString() );
            }
        } );
    }

    @Override
    public final <T> Function2<ValueType, String, T> deserialize()
    {
        return new Function2<ValueType, String, T>()
        {
            @Override
            public T map( ValueType valueType, String input )
            {
                return deserialize( valueType, input );
            }
        };
    }

    @Override
    public final <T> T deserialize( ValueType type, String input )
        throws ValueSerializationException
    {
        try
        {
            InputType adaptedInput = adaptInput( new ByteArrayInputStream( input.getBytes( "UTF-8" ) ) );

            onDeserializationStart( type, adaptedInput );
            T deserialized = doDeserialize( type, adaptedInput );
            onDeserializationEnd( type, adaptedInput );

            return deserialized;
        }
        catch( ValueSerializationException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new ValueSerializationException( "Could not deserialize value", ex );
        }
    }

    @Override
    public final <T> T deserialize( ValueType type, InputStream input )
        throws ValueSerializationException
    {
        try
        {
            InputType wrappedInput = adaptInput( input );

            onDeserializationStart( type, wrappedInput );
            T deserialized = doDeserialize( type, wrappedInput );
            onDeserializationEnd( type, wrappedInput );

            return deserialized;
        }
        catch( ValueSerializationException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new ValueSerializationException( "Could not deserialize value", ex );
        }
    }

    @SuppressWarnings( "unchecked" )
    private <T> T doDeserialize( ValueType valueType, InputType input )
        throws Exception
    {
        final Class<?> type = first( valueType.types() );
        // Registered deserializers
        if( deserializers.get( type ) != null )
        {
            Object value = readValue( input );
            if( value == null )
            {
                return null;
            }
            return (T) deserializers.get( type ).map( value );
        }
        else // Explicit ValueComposite
        if( ValueCompositeType.class.isAssignableFrom( valueType.getClass() ) )
        {
            PULL_PARSING_LOG.debug( "ValueCompositeType assignable - deserializeValueComposite( {} )", input );
            return (T) deserializeValueComposite( valueType, input );
        }
        else // Explicit Collections
        if( CollectionType.class.isAssignableFrom( valueType.getClass() ) )
        {
            PULL_PARSING_LOG.debug( "CollectionType assignable - deserializeCollection( {} )", input );
            return (T) deserializeCollection( (CollectionType) valueType, input );
        }
        else // Explicit Map
        if( MapType.class.isAssignableFrom( valueType.getClass() ) )
        {
            PULL_PARSING_LOG.debug( "MapType assignable - deserializeMap( {} )", input );
            return (T) deserializeMap( (MapType) valueType, input );
        }
        else // Enum
        if( EnumType.class.isAssignableFrom( valueType.getClass() ) || type.isEnum() )
        {
            PULL_PARSING_LOG.debug( "EnumType assignable - readValue( {} )", input );
            return (T) Enum.valueOf( (Class) type, readValue( input ).toString() );
        }
        // Guessed Deserialization
        PULL_PARSING_LOG.debug( "Unknown ValueType - deserializeGuessed( {} )", input );
        return (T) deserializeGuessed( valueType, input );
    }

    private <T> Function<InputType, T> buildDeserializeInputFunction( final ValueType valueType )
    {
        return new Function<InputType, T>()
        {
            @Override
            public T map( InputType input )
            {
                try
                {
                    return doDeserialize( valueType, input );
                }
                catch( ValueSerializationException ex )
                {
                    throw ex;
                }
                catch( Exception ex )
                {
                    throw new ValueSerializationException( ex );
                }
            }
        };
    }

    private <T> Collection<T> deserializeCollection( CollectionType valueType, InputType input )
        throws Exception
    {
        Collection<T> collection;
        Class<?> collectionType = first( valueType.types() );
        if( Set.class.equals( collectionType ) )
        {
            collection = new LinkedHashSet<T>();
        }
        else
        {
            collection = new ArrayList<T>();
        }
        return readArrayInCollection( input,
                                      this.<T>buildDeserializeInputFunction( valueType.collectedType() ),
                                      collection );
    }

    private <K, V> Map<K, V> deserializeMap( MapType valueType, InputType input )
        throws Exception
    {
        return readMapInMap( input,
                             this.<K>buildDeserializeInputFunction( valueType.getKeyType() ),
                             this.<V>buildDeserializeInputFunction( valueType.getValueType() ),
                             new HashMap<K, V>() );
    }

    private <T> T deserializeValueComposite( ValueType valueType, InputType input )
        throws Exception
    {
        PULL_PARSING_LOG.debug( "Switching to TREE PARSING @( {} )", input );
        InputNodeType objectNode = readObjectTree( input );
        TREE_PARSING_LOG.debug( "Switched to TREE PARSING @( {} )", input );
        TREE_PARSING_LOG.trace( "ObjectNode is {}", objectNode );
        if( objectNode == null )
        {
            return null;
        }
        return deserializeNodeValueComposite( valueType, objectNode );
    }

    private <T> T deserializeNodeValueComposite( ValueType valueType, InputNodeType objectNode )
        throws Exception
    {
        ValueCompositeType valueCompositeType = (ValueCompositeType) valueType;
        Class<?> valueBuilderType = first( valueCompositeType.types() );
        String typeInfo = this.<String>getObjectFieldValue(
            objectNode,
            "_type",
            this.<String>buildDeserializeInputNodeFunction( new ValueType( String.class ) ) );
        TREE_PARSING_LOG.trace(
            "In deserializeNodeValueComposite(), getObjectFieldValue( {} ) returned '{}'",
            objectNode, typeInfo );
        if( typeInfo != null )
        {
            ValueDescriptor valueDescriptor = module.valueDescriptor( typeInfo );
            if( valueDescriptor == null )
            {
                throw new ValueSerializationException( "Specified value type could not be resolved: " + typeInfo );
            }
            valueCompositeType = valueDescriptor.valueType();
            valueBuilderType = Class.forName( typeInfo );
            if( !valueType.equals( valueCompositeType ) )
            {
                TREE_PARSING_LOG.debug(
                    "Overriding {} with {} as defined in _type field.",
                    valueType, valueCompositeType );
            }
        }
        return deserializeValueComposite( valueCompositeType, valueBuilderType, objectNode );
    }

    private <T> T deserializeValueComposite( ValueCompositeType valueType, Class<?> valueBuilderType, InputNodeType objectNode )
        throws Exception
    {
        final Map<String, Object> values = new HashMap<String, Object>();

        // Properties
        for( PropertyDescriptor property : valueType.properties() )
        {
            String key = property.qualifiedName().name();
            Object value;
            if( objectHasField( objectNode, key ) )
            {
                value = getObjectFieldValue(
                    objectNode,
                    key,
                    buildDeserializeInputNodeFunction( property.valueType() ) );
                TREE_PARSING_LOG.debug(
                    "In deserializeValueComposite(), getObjectFieldValue( {} ) for key {} returned '{}' of class {}",
                    objectNode, key, value, value == null ? "N/A" : value.getClass() );
                if( property.isImmutable() )
                {
                    if( value instanceof Set )
                    {
                        value = Collections.unmodifiableSet( (Set<?>) value );
                    }
                    else if( value instanceof List )
                    {
                        value = Collections.unmodifiableList( (List<?>) value );
                    }
                    else if( value instanceof Map )
                    {
                        value = Collections.unmodifiableMap( (Map<?, ?>) value );
                    }
                }
                TREE_PARSING_LOG.debug( "Property {}#{}( {} ) deserialized value is '{}' of class {}",
                                        property.qualifiedName().type(),
                                        property.qualifiedName().name(),
                                        property.valueType(),
                                        value,
                                        value == null ? "N/A" : value.getClass() );
            }
            else
            {
                // Serialized object do not contains the field, try to default it
                value = property.initialValue( module );
                TREE_PARSING_LOG.debug(
                    "Property {} was not defined in serialized object and has been defaulted to '{}'",
                    property.qualifiedName(), value );
            }
            values.put( key, value );
        }

        // Associations
        for( AssociationDescriptor association : valueType.associations() )
        {
            String key = association.qualifiedName().name();
            if( objectHasField( objectNode, key ) )
            {
                Object value = getObjectFieldValue(
                    objectNode,
                    key,
                    buildDeserializeInputNodeFunction( new ValueType( EntityReference.class ) ) );
                values.put( key, value );
            }
        }

        // ManyAssociations
        for( AssociationDescriptor manyAssociation : valueType.manyAssociations() )
        {
            String key = manyAssociation.qualifiedName().name();
            if( objectHasField( objectNode, key ) )
            {
                Object value = getObjectFieldValue(
                    objectNode,
                    key,
                    buildDeserializeInputNodeFunction( new CollectionType( Collection.class,
                                                                           new ValueType( EntityReference.class ) ) ) );
                values.put( key, value );
            }
        }

        ValueBuilder<?> valueBuilder = buildNewValueBuilderWithState( valueBuilderType, values );
        return (T) valueBuilder.newInstance(); // Unchecked cast because the builder could use a type != T
    }

    private <T> Function<InputNodeType, T> buildDeserializeInputNodeFunction( final ValueType valueType )
    {
        return new Function<InputNodeType, T>()
        {
            @Override
            public T map( InputNodeType inputNode )
            {
                try
                {
                    return doDeserializeInputNodeValue( valueType, inputNode );
                }
                catch( ValueSerializationException ex )
                {
                    throw ex;
                }
                catch( Exception ex )
                {
                    throw new ValueSerializationException( ex );
                }
            }
        };
    }

    @SuppressWarnings( "unchecked" )
    private <T> T doDeserializeInputNodeValue( ValueType valueType, InputNodeType inputNode )
        throws Exception
    {
        if( inputNode == null )
        {
            return null;
        }
        final Class<?> type = first( valueType.types() );
        // Registered deserializers
        if( deserializers.get( type ) != null )
        {
            Object value = asSimpleValue( inputNode );
            TREE_PARSING_LOG.trace(
                "While registered deserializer attempt, asSimpleValue( {} ) returned '{}'",
                inputNode, value );
            if( value == null )
            {
                return null;
            }
            return (T) deserializers.get( type ).map( value );
        }
        else // Explicit ValueComposite
        if( ValueCompositeType.class.isAssignableFrom( valueType.getClass() ) )
        {
            return (T) deserializeNodeValueComposite( (ValueCompositeType) valueType, inputNode );
        }
        else // Explicit Collections
        if( CollectionType.class.isAssignableFrom( valueType.getClass() ) )
        {
            return (T) deserializeNodeCollection( (CollectionType) valueType, inputNode );
        }
        else // Explicit Map
        if( MapType.class.isAssignableFrom( valueType.getClass() ) )
        {
            return (T) deserializeNodeMap( (MapType) valueType, inputNode );
        }
        else // Enum
        if( EnumType.class.isAssignableFrom( valueType.getClass() ) || type.isEnum() )
        {
            Object value = asSimpleValue( inputNode );
            TREE_PARSING_LOG.trace(
                "While Enum deserialize attempt, asSimpleValue( {} ) returned '{}'",
                inputNode, value );
            if( value == null )
            {
                return null;
            }
            return (T) Enum.valueOf( (Class) type, value.toString() );
        }
        // Guessed deserialization
        return (T) deserializeNodeGuessed( valueType, inputNode );
    }

    private ValueBuilder<?> buildNewValueBuilderWithState( Class<?> type, final Map<String, Object> values )
    {
        return module.newValueBuilderWithState(
            type,
            new Function<PropertyDescriptor, Object>()
            {
                @Override
                public Object map( PropertyDescriptor descriptor )
                {
                    return values.get( descriptor.qualifiedName().name() );
                }
            },
            new Function<AssociationDescriptor, EntityReference>()
            {
                @Override
                public EntityReference map(
                    AssociationDescriptor associationDescriptor )
                {
                    Object ref = values.get( associationDescriptor.qualifiedName().name() );
                    if( ref == null )
                    {
                        return null;
                    }
                    else
                    {
                        return (EntityReference) ref;
                    }
                }
            },
            new Function<AssociationDescriptor, Iterable<EntityReference>>()
            {
                @Override
                @SuppressWarnings( "unchecked" )
                public Iterable<EntityReference> map( AssociationDescriptor associationDescriptor )
                {
                    Object ref = values.get( associationDescriptor.qualifiedName().name() );
                    if( ref == null )
                    {
                        return empty();
                    }
                    else
                    {
                        return (Iterable<EntityReference>) ref;
                    }
                }
            } );

    }

    @SuppressWarnings( "unchecked" )
    private <T> T deserializeGuessed( ValueType valueType, InputType input )
        throws Exception
    {
        InputNodeType objectNode = readObjectTree( input );
        if( objectNode == null )
        {
            return null;
        }
        return deserializeNodeGuessed( valueType, objectNode );
    }

    private <T> Collection<T> deserializeNodeCollection( CollectionType valueType, InputNodeType inputNode )
        throws Exception
    {
        Collection<T> collection;
        Class<?> collectionType = first( valueType.types() );
        if( Set.class.equals( collectionType ) )
        {
            collection = new LinkedHashSet<T>();
        }
        else
        {
            collection = new ArrayList<T>();
        }
        putArrayNodeInCollection( inputNode,
                                  this.<T>buildDeserializeInputNodeFunction( valueType.collectedType() ),
                                  collection );
        return collection;
    }

    private <K, V> Map<K, V> deserializeNodeMap( MapType valueType, InputNodeType inputNode )
        throws Exception
    {
        Map<K, V> map = new HashMap<K, V>();
        putArrayNodeInMap( inputNode,
                           this.<K>buildDeserializeInputNodeFunction( valueType.getKeyType() ),
                           this.<V>buildDeserializeInputNodeFunction( valueType.getValueType() ),
                           map );
        return map;
    }

    @SuppressWarnings( "unchecked" )
    private <T> T deserializeNodeGuessed( ValueType valueType, InputNodeType inputNode )
        throws Exception
    {
        if( isObjectValue( inputNode ) )
        {
            if( !objectHasField( inputNode, "_type" ) )
            {
                throw new ValueSerializationException( "Don't know how to deserialize " + inputNode );
            }
            String typeInfo = this.<String>getObjectFieldValue(
                inputNode,
                "_type",
                this.<String>buildDeserializeInputNodeFunction( new ValueType( String.class ) ) );
            TREE_PARSING_LOG.debug(
                "In deserializeNodeGuessed(), getObjectFieldValue( {} ) returned '{}'",
                inputNode, typeInfo );
            ValueDescriptor valueDescriptor = module.valueDescriptor( typeInfo );
            if( valueDescriptor == null )
            {
                throw new ValueSerializationException( "Specified value type could not be resolved: " + typeInfo );
            }
            ValueCompositeType valueCompositeType = valueDescriptor.valueType();
            Class<?> valueBuilderType = first( valueCompositeType.types() );
            TREE_PARSING_LOG.debug(
                "Overriding {} with {} as defined in _type field.",
                valueType, valueCompositeType );
            return deserializeValueComposite( valueCompositeType, valueBuilderType, inputNode );
        }
        return (T) deserializeBase64Serialized( inputNode );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T deserializeBase64Serialized( InputNodeType inputNode )
        throws Exception
    {
        Object value = asSimpleValue( inputNode );
        TREE_PARSING_LOG.debug(
            "While Base64 deserialize attempt, asSimpleValue( {} ) returned '{}'",
            inputNode, value );
        if( value == null )
        {
            return null;
        }
        String base64 = value.toString();
        byte[] bytes = base64.getBytes( "UTF-8" );
        bytes = Base64Encoder.decode( bytes );
        ObjectInputStream oin = new ObjectInputStream( new ByteArrayInputStream( bytes ) );
        Object result = oin.readObject();
        oin.close();
        return (T) result;
    }

    //
    // Deserialization - Pull parsing extension points
    //
    protected abstract InputType adaptInput( InputStream input )
        throws Exception;

    protected void onDeserializationStart( ValueType valueType, InputType input )
        throws Exception
    {
        // NOOP
    }

    protected void onDeserializationEnd( ValueType valueType, InputType input )
        throws Exception
    {
        // NOOP
    }

    // TODO rename into readSimpleValue
    protected abstract Object readValue( InputType input )
        throws Exception;

    /**
     * @return The filled collection or null if no array
     */
    protected abstract <T> Collection<T> readArrayInCollection( InputType input,
                                                                Function<InputType, T> deserializer,
                                                                Collection<T> collection )
        throws Exception;

    /**
     * A Map&lt;K,V&gt; is serialized in an array of entries objects.
     *
     * <p>Here is an example in JSON:</p>
     * <pre>
     * [
     *     { "key": "foo",       "value": "bar"   },
     *     { "key": "cathedral", "value": "bazar" }
     * ]
     * </pre>
     * <p>And an empty Map:</p>
     * <pre>[]</pre>
     * <p>
     *     This allow to use any type as keys and values while keeping the Map order at the cost of having
     *     non-predictible order of key/value inside an entry object.
     * </p>
     *
     * @return The filled map or null if no array
     */
    protected abstract <K, V> Map<K, V> readMapInMap( InputType input,
                                                      Function<InputType, K> keyDeserializer,
                                                      Function<InputType, V> valueDeserializer,
                                                      Map<K, V> map )
        throws Exception;

    /**
     * @return an InputNodeType or null if the value was null
     */
    protected abstract InputNodeType readObjectTree( InputType input )
        throws Exception;

    //
    // Deserialization - Tree parsing extension points
    //
    protected abstract Object asSimpleValue( InputNodeType inputNode )
        throws Exception;

    protected abstract boolean isObjectValue( InputNodeType inputNode )
        throws Exception;

    protected abstract boolean objectHasField( InputNodeType inputNode, String key )
        throws Exception;

    /**
     * Return null if the field do not exists.
     */
    protected abstract <T> T getObjectFieldValue( InputNodeType inputNode,
                                                  String key,
                                                  Function<InputNodeType, T> valueDeserializer )
        throws Exception;

    protected abstract <T> void putArrayNodeInCollection( InputNodeType inputNode,
                                                          Function<InputNodeType, T> deserializer,
                                                          Collection<T> collection )
        throws Exception;

    protected abstract <K, V> void putArrayNodeInMap( InputNodeType inputNode,
                                                      Function<InputNodeType, K> keyDeserializer,
                                                      Function<InputNodeType, V> valueDeserializer,
                                                      Map<K, V> map )
        throws Exception;
}
