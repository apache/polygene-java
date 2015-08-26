/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2010, Niclas Hehdman. All Rights Reserved.
 * Copyright (c) 2012-2014, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.spi.value;

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
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.type.EnumType;
import org.apache.zest.api.type.MapType;
import org.apache.zest.api.type.Serialization;
import org.apache.zest.api.type.ValueCompositeType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.util.Base64Encoder;
import org.apache.zest.api.util.Dates;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.api.value.ValueDeserializer;
import org.apache.zest.api.value.ValueSerializationException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import static org.apache.zest.functional.Iterables.empty;

/**
 * Adapter for pull-parsing and tree-parsing capable ValueDeserializers.
 *
 * <p>
 * Among Plain values (see {@link ValueDeserializer}) some are considered primitives to underlying serialization
 * mechanisms and by so handed/come without conversion to/from implementations. Primitive values can be one of:
 * </p>
 * <ul>
 * <li>String,</li>
 * <li>Character or char,</li>
 * <li>Boolean or boolean,</li>
 * <li>Integer or int,</li>
 * <li>Long or long,</li>
 * <li>Short or short,</li>
 * <li>Byte or byte,</li>
 * <li>Float or float,</li>
 * <li>Double or double.</li>
 * </ul>
 * <p>
 * Some other Plain values are expected in given formats:
 * </p>
 * <ul>
 * <li>BigInteger and BigDecimal depends on {@link org.apache.zest.api.value.ValueSerializer.Options};</li>
 * <li>Date as String in ISO-8601, {@literal @millis@} or {@literal /Date(..)} Microsoft format;</li>
 * <li>DateTime (JodaTime) as a ISO-8601 String with optional timezone offset;</li>
 * <li>LocalDateTime (JodaTime) as whatever {@link LocalDateTime#LocalDateTime(java.lang.Object)} accept as {@literal instant};</li>
 * <li>LocalDate (JodaTime) as whatever {@link LocalDate#LocalDate(java.lang.Object)} accept as {@literal instant};</li>
 * </ul>
 *
 * @param <InputType>     Implementor pull-parser type
 * @param <InputNodeType> Implementor tree-parser node type
 */
public abstract class ValueDeserializerAdapter<InputType, InputNodeType>
    implements ValueDeserializer
{
    public interface ComplexDeserializer<T, InputType, InputNodeType>
    {
        T deserializePull( InputType input )
            throws Exception;

        T deserializeTree( InputNodeType inputNode )
            throws Exception;
    }

    private static final String UTF_8 = "UTF-8";
    private final Map<Class<?>, Function<Object, Object>> deserializers = new HashMap<>( 16 );
    private final Map<Class<?>, ComplexDeserializer<Object, InputType, InputNodeType>> complexDeserializers = new HashMap<>( 2 );
    private final Application application;
    private final Module module;
    private Function<Application, Module> valuesModuleFinder;
    private Module valuesModule;

    /**
     * Register a Plain Value type deserialization Function.
     *
     * @param <T>          Plain Value parametrized Type
     * @param type         Plain Value Type
     * @param deserializer Deserialization Function
     */
    @SuppressWarnings( "unchecked" )
    protected final <T> void registerDeserializer( Class<T> type, Function<Object, T> deserializer )
    {
        deserializers.put( type, (Function<Object, Object>) deserializer );
    }

    @SuppressWarnings( { "UnusedDeclaration", "unchecked" } )
    protected final <T> void registerComplexDeserializer( Class<T> type,
                                                          ComplexDeserializer<T, InputType, InputNodeType> deserializer
    )
    {
        complexDeserializers.put( type, (ComplexDeserializer<Object, InputType, InputNodeType>) deserializer );
    }

    @SuppressWarnings( "unchecked" )
    public ValueDeserializerAdapter( @Structure Application application,
                                     @Structure Module module,
                                     @Service ServiceReference<ValueDeserializer> serviceRef
    )
    {
        this( application, module, serviceRef.metaInfo( Function.class ) );
    }

    protected ValueDeserializerAdapter( Application application,
                                        Module module,
                                        Function<Application, Module> valuesModuleFinder
    )
    {

        this.application = application;
        this.module = module;
        setValuesModuleFinder( valuesModuleFinder );

        // Primitive Value types
        registerDeserializer( String.class, Object::toString );
        registerDeserializer( Character.class, input -> input.toString().charAt( 0 ) );
        registerDeserializer( Boolean.class, input -> ( input instanceof String )
                                                      ? Boolean.parseBoolean( (String) input )
                                                      : (Boolean) input );
        registerDeserializer( Integer.class, input -> ( input instanceof String )
                                                      ? Integer.parseInt( (String) input )
                                                      : ( (Number) input ).intValue() );
        registerDeserializer( Long.class, input -> ( input instanceof String )
                                                   ? Long.parseLong( (String) input )
                                                   : ( (Number) input ).longValue() );
        registerDeserializer( Short.class, input -> ( input instanceof String )
                                                    ? Short.parseShort( (String) input )
                                                    : ( (Number) input ).shortValue() );
        registerDeserializer( Byte.class, input -> ( input instanceof String )
                                                   ? Byte.parseByte( (String) input )
                                                   : ( (Number) input ).byteValue() );
        registerDeserializer( Float.class, input -> ( input instanceof String )
                                                    ? Float.parseFloat( (String) input )
                                                    : ( (Number) input ).floatValue() );
        registerDeserializer( Double.class, input -> ( input instanceof String )
                                                     ? Double.parseDouble( (String) input )
                                                     : ( (Number) input ).doubleValue() );

        // Number types
        registerDeserializer( BigDecimal.class, input -> new BigDecimal( input.toString() ) );
        registerDeserializer( BigInteger.class, input -> new BigInteger( input.toString() ) );

        // Date types
        registerDeserializer( Date.class, input -> Dates.fromString( input.toString() ) );
        registerDeserializer( DateTime.class, input -> DateTime.parse( input.toString() ) );
        registerDeserializer( LocalDateTime.class, LocalDateTime::new );
        registerDeserializer( LocalDate.class, LocalDate::new );

        // Other supported types
        registerDeserializer( EntityReference.class, input -> EntityReference.parseEntityReference( input.toString() ) );
    }

    private void setValuesModuleFinder( Function<Application, Module> valuesModuleFinder )
    {
        this.valuesModuleFinder = valuesModuleFinder;
        this.valuesModule = null;
    }

    private Module valuesModule()
    {
        if( valuesModule == null )
        {
            if( valuesModuleFinder == null )
            {
                valuesModule = module;
            }
            else
            {
                valuesModule = valuesModuleFinder.apply( application );
                if( valuesModule == null )
                {
                    throw new ValueSerializationException( "Values Module provided by the finder Function was null." );
                }
            }
        }
        return valuesModule;
    }

    @Override
    public <T> Function<String, T> deserialize( Class<T> type )
    {
        if( CollectionType.isCollection( type ) )
        {
            ValueType objectValueType = new ValueType( Object.class );
            return deserialize( new CollectionType( type, objectValueType ) );
        }
        if( MapType.isMap( type ) )
        {
            ValueType objectValueType = new ValueType( Object.class );
            return deserialize( new MapType( type, objectValueType, objectValueType ) );
        }
        return deserialize( new ValueType( type ) );
    }

    @Override
    public final <T> Function<String, T> deserialize( final ValueType valueType )
    {
        return input -> deserialize( valueType, input );
    }

    @Override
    public final <T> BiFunction<ValueType, String, T> deserialize()
    {
        return this::deserialize;
    }

    @Override
    public final <T> T deserialize( Class<?> type, String input )
        throws ValueSerializationException
    {
        if( CollectionType.isCollection( type ) )
        {
            ValueType objectValueType = new ValueType( Object.class );
            return deserialize( new CollectionType( type, objectValueType ), input );
        }
        if( MapType.isMap( type ) )
        {
            ValueType objectValueType = new ValueType( Object.class );
            return deserialize( new MapType( type, objectValueType, objectValueType ), input );
        }
        return deserialize( new ValueType( type ), input );
    }

    @Override
    public final <T> T deserialize( ValueType valueType, String input )
        throws ValueSerializationException
    {
        try
        {
            return deserializeRoot( valueType, new ByteArrayInputStream( input.getBytes( UTF_8 ) ) );
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
    public final <T> T deserialize( Class<?> type, InputStream input )
        throws ValueSerializationException
    {
        if( CollectionType.isCollection( type ) )
        {
            ValueType objectValueType = new ValueType( Object.class );
            return deserialize( new CollectionType( type, objectValueType ), input );
        }
        if( MapType.isMap( type ) )
        {
            ValueType objectValueType = new ValueType( Object.class );
            return deserialize( new MapType( type, objectValueType, objectValueType ), input );
        }
        return deserialize( new ValueType( type ), input );
    }

    @Override
    public final <T> T deserialize( ValueType valueType, InputStream input )
        throws ValueSerializationException
    {
        try
        {
            return deserializeRoot( valueType, input );
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
    private <T> T deserializeRoot( ValueType valueType, InputStream input )
        throws Exception
    {
        final Class<?> type = valueType.types().findFirst().orElse( null );
        // Plain ValueType
        if( deserializers.get( type ) != null )
        {
            Scanner scanner = new Scanner( input, UTF_8 ).useDelimiter( "\\A" );
            if( !scanner.hasNext() )
            {
                return String.class.equals( type ) ? (T) "" : null;
            }
            String string = scanner.next();
            return (T) deserializers.get( type ).apply( string );
        }
        else // Array ValueType
            if( type.isArray() )
            {
                Scanner scanner = new Scanner( input, UTF_8 ).useDelimiter( "\\A" );
                if( !scanner.hasNext() )
                {
                    return null;
                }
                String string = scanner.next();
                return (T) deserializeBase64Serialized( string );
            }
            else // Complex ValueType
            {
                InputType adaptedInput = adaptInput( input );
                onDeserializationStart( valueType, adaptedInput );
                T deserialized = doDeserialize( valueType, adaptedInput );
                onDeserializationEnd( valueType, adaptedInput );
                return deserialized;
            }
    }

    @SuppressWarnings( "unchecked" )
    private <T> T doDeserialize( ValueType valueType, InputType input )
        throws Exception
    {
        final Class<?> type = valueType.types().findFirst().orElse( null );
        // Registered deserializers
        if( deserializers.get( type ) != null )
        {
            Object value = readPlainValue( input );
            if( value == null )
            {
                return null;
            }
            return (T) deserializers.get( type ).apply( value );
        }
        else if( complexDeserializers.get( type ) != null )
        {
            return (T) complexDeserializers.get( type ).deserializePull( input );
        }
        else // Explicit ValueComposite
            if( ValueCompositeType.class.isAssignableFrom( valueType.getClass() ) )
            {
                return (T) deserializeValueComposite( valueType, input );
            }
            else // Explicit Collections
                if( CollectionType.class.isAssignableFrom( valueType.getClass() ) )
                {
                    return (T) deserializeCollection( (CollectionType) valueType, input );
                }
                else // Explicit Map
                    if( MapType.class.isAssignableFrom( valueType.getClass() ) )
                    {
                        return (T) deserializeMap( (MapType) valueType, input );
                    }
                    else // Enum
                        if( EnumType.class.isAssignableFrom( valueType.getClass() ) || type.isEnum() )
                        {
                            return (T) Enum.valueOf( (Class) type, readPlainValue( input ).toString() );
                        }
                        else // Array
                            if( type.isArray() )
                            {
                                return (T) deserializeBase64Serialized( readPlainValue( input ).toString() );
                            }
        // Guessed Deserialization
        return (T) deserializeGuessed( valueType, input );
    }

    private <T> Function<InputType, T> buildDeserializeInputFunction( final ValueType valueType )
    {
        return input -> {
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
        };
    }

    private <T> Collection<T> deserializeCollection( CollectionType collectionType, InputType input )
        throws Exception
    {
        Collection<T> collection;
        Class<?> collectionMainType = collectionType.types().findFirst().orElse( null );
        if( Set.class.equals( collectionMainType ) )
        {
            collection = new LinkedHashSet<>();
        }
        else
        {
            collection = new ArrayList<>();
        }
        return readArrayInCollection( input,
                                      this.<T>buildDeserializeInputFunction( collectionType.collectedType() ),
                                      collection );
    }

    private <K, V> Map<K, V> deserializeMap( MapType mapType, InputType input )
        throws Exception
    {
        return readMapInMap( input,
                             this.<K>buildDeserializeInputFunction( mapType.keyType() ),
                             this.<V>buildDeserializeInputFunction( mapType.valueType() ),
                             new HashMap<>() );
    }

    private <T> T deserializeValueComposite( ValueType valueType, InputType input )
        throws Exception
    {
        InputNodeType inputNode = readObjectTree( input );
        if( inputNode == null )
        {
            return null;
        }
        return deserializeNodeValueComposite( valueType, inputNode );
    }

    private <T> T deserializeNodeValueComposite( ValueType valueType, InputNodeType inputNode )
        throws Exception
    {
        ValueCompositeType valueCompositeType = (ValueCompositeType) valueType;
        Class<?> valueBuilderType = valueCompositeType.types().findFirst().orElse( null );
        String typeInfo = this.getObjectFieldValue(
            inputNode,
            "_type",
            this.<String>buildDeserializeInputNodeFunction( new ValueType( String.class ) ) );
        if( typeInfo != null )
        {
            ValueDescriptor valueDescriptor = valuesModule().valueDescriptor( typeInfo );
            if( valueDescriptor == null )
            {
                throw new ValueSerializationException( "Specified value type could not be resolved: " + typeInfo );
            }
            valueCompositeType = valueDescriptor.valueType();
            valueBuilderType = Class.forName( typeInfo );
        }
        return deserializeValueComposite( valueCompositeType, valueBuilderType, inputNode );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T deserializeValueComposite( ValueCompositeType valueCompositeType,
                                             Class<?> valueBuilderType,
                                             InputNodeType inputNode
    )
        throws Exception
    {
        final Map<String, Object> stateMap = new HashMap<>();

        // Properties
        valueCompositeType.properties().forEach( property -> {
            String propertyName = null;
            Object value;
            try
            {
                propertyName = property.qualifiedName().name();
                if( objectHasField( inputNode, propertyName ) )
                {
                    value = getObjectFieldValue(
                        inputNode,
                        propertyName,
                        buildDeserializeInputNodeFunction( property.valueType() ) );
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
                }
                else
                {
                    // Serialized object does not contain the field, try to default it
                    value = property.initialValue( valuesModule() );
                }
            }
            catch( Exception e )
            {
                throw new ValueSerializationException( "Unable to deserialize property " + property, e );
            }
            stateMap.put( propertyName, value );
        } );

        // Associations
        valueCompositeType.associations().forEach( association -> {
            try
            {
                String associationName = association.qualifiedName().name();
                if( objectHasField( inputNode, associationName ) )
                {
                    Object value = getObjectFieldValue(
                        inputNode,
                        associationName,
                        buildDeserializeInputNodeFunction( new ValueType( EntityReference.class ) ) );
                    stateMap.put( associationName, value );
                }
            }
            catch( Exception e )
            {
                throw new ValueSerializationException( "Unable to deserialize association " + association, e );
            }
        } );

        // ManyAssociations
        valueCompositeType.manyAssociations().forEach( manyAssociation -> {
            try
            {
                String manyAssociationName = manyAssociation.qualifiedName().name();
                if( objectHasField( inputNode, manyAssociationName ) )
                {
                    Object value = getObjectFieldValue(
                        inputNode,
                        manyAssociationName,
                        buildDeserializeInputNodeFunction( new CollectionType(
                            Collection.class,
                            new ValueType( EntityReference.class ) ) ) );
                    stateMap.put( manyAssociationName, value );
                }
            }
            catch( Exception e )
            {
                throw new ValueSerializationException( "Unable to deserialize manyassociation " + manyAssociation, e );
            }
        } );

        // NamedAssociations
        valueCompositeType.namedAssociations().forEach( namedAssociation -> {
            try
            {
                String namedAssociationName = namedAssociation.qualifiedName().name();
                if( objectHasField( inputNode, namedAssociationName ) )
                {
                    Object value = getObjectFieldValue(
                        inputNode,
                        namedAssociationName,
                        buildDeserializeInputNodeFunction( MapType.of( String.class, EntityReference.class, Serialization.Variant.object ) ) );
                    stateMap.put( namedAssociationName, value );
                }
            }
            catch( Exception e )
            {
                throw new ValueSerializationException( "Unable to deserialize namedassociation " + namedAssociation, e );
            }
        } );

        ValueBuilder<?> valueBuilder = buildNewValueBuilderWithState( valueBuilderType, stateMap );
        return (T) valueBuilder.newInstance(); // Unchecked cast because the builder could use a type != T
    }

    private <T> Function<InputNodeType, T> buildDeserializeInputNodeFunction( final ValueType valueType )
    {
        return inputNode -> {
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
        final Class<?> type = valueType.types().findFirst().orElse( null );
        // Registered deserializers
        if( deserializers.get( type ) != null )
        {
            Object value = asSimpleValue( inputNode );
            if( value == null )
            {
                return null;
            }
            return (T) deserializers.get( type ).apply( value );
        }
        else if( complexDeserializers.get( type ) != null )
        {
            return (T) complexDeserializers.get( type ).deserializeTree( inputNode );
        }
        else // Explicit ValueComposite
            if( ValueCompositeType.class.isAssignableFrom( valueType.getClass() ) )
            {
                return (T) deserializeNodeValueComposite( valueType, inputNode );
            }
            else // Explicit Collections
                if( CollectionType.class.isAssignableFrom( valueType.getClass() ) )
                {
                    return (T) deserializeNodeCollection( (CollectionType) valueType, inputNode );
                }
                else // Explicit Map
                    if( MapType.class.isAssignableFrom( valueType.getClass() ) )
                    {
                        MapType mapType = (MapType) valueType;
                        if( mapType.variant().equals( Serialization.Variant.entry ) )
                        {
                            return (T) deserializeNodeEntryMap( (MapType) valueType, inputNode );
                        }
                        else
                        {
                            return (T) deserializeNodeObjectMap( (MapType) valueType, inputNode );
                        }
                    }
                    else // Enum
                        if( EnumType.class.isAssignableFrom( valueType.getClass() ) || type.isEnum() )
                        {
                            Object value = asSimpleValue( inputNode );
                            if( value == null )
                            {
                                return null;
                            }
                            return (T) Enum.valueOf( (Class) type, value.toString() );
                        }
        // Guessed deserialization
        return (T) deserializeNodeGuessed( valueType, inputNode );
    }

    private ValueBuilder<?> buildNewValueBuilderWithState( Class<?> type, final Map<String, Object> stateMap )
    {
        return valuesModule().newValueBuilderWithState(
            type,
            property -> stateMap.get( property.qualifiedName().name() ),
            association -> {
                Object entityRef = stateMap.get( association.qualifiedName().name() );
                if( entityRef == null )
                {
                    return null;
                }
                return (EntityReference) entityRef;
            },
            manyAssociation -> {
                Object entityRefs = stateMap.get( manyAssociation.qualifiedName().name() );
                if( entityRefs == null )
                {
                    return empty();
                }
                //noinspection unchecked
                return (Iterable<EntityReference>) entityRefs;
            },
            namedAssociation -> {
                Object entityRefs = stateMap.get( namedAssociation.qualifiedName().name() );
                if( entityRefs == null )
                {
                    return Collections.emptyMap();
                }
                //noinspection unchecked
                return (Map<String, EntityReference>) entityRefs;
            } );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T deserializeGuessed( ValueType valueType, InputType input )
        throws Exception
    {
        InputNodeType inputNode = readObjectTree( input );
        if( inputNode == null )
        {
            return null;
        }
        return deserializeNodeGuessed( valueType, inputNode );
    }

    private <T> Collection<T> deserializeNodeCollection( CollectionType collectionType, InputNodeType inputNode )
        throws Exception
    {
        Collection<T> collection;
        Class<?> collectionMainType = collectionType.types().findFirst().orElse( null );
        if( Set.class.equals( collectionMainType ) )
        {
            collection = new LinkedHashSet<>();
        }
        else
        {
            collection = new ArrayList<>();
        }
        putArrayNodeInCollection( inputNode,
                                  this.<T>buildDeserializeInputNodeFunction( collectionType.collectedType() ),
                                  collection );
        return collection;
    }

    private <K, V> Map<K, V> deserializeNodeEntryMap( MapType mapType, InputNodeType inputNode )
        throws Exception
    {
        Map<K, V> map = new HashMap<>();
        putArrayNodeInMap( inputNode,
                           this.<K>buildDeserializeInputNodeFunction( mapType.keyType() ),
                           this.<V>buildDeserializeInputNodeFunction( mapType.valueType() ),
                           map );
        return map;
    }

    private <V> Map<String, V> deserializeNodeObjectMap( MapType mapType, InputNodeType inputNode )
        throws Exception
    {
        Map<String, V> map = new HashMap<>();
        putObjectNodeInMap( inputNode,
                            this.<V>buildDeserializeInputNodeFunction( mapType.valueType() ),
                            map );
        return map;
    }

    @SuppressWarnings( "unchecked" )
    private <T> T deserializeNodeGuessed( ValueType valueType, InputNodeType inputNode )
        throws Exception
    {
        if( isObjectValue( inputNode ) )
        {
            // Attempt ValueComposite deserialization
            ValueCompositeType valueCompositeType;
            if( objectHasField( inputNode, "_type" ) ) // with _type info
            {
                String typeInfo = this.getObjectFieldValue(
                    inputNode,
                    "_type",
                    this.<String>buildDeserializeInputNodeFunction( new ValueType( String.class ) ) );
                ValueDescriptor valueDescriptor = valuesModule().valueDescriptor( typeInfo );
                if( valueDescriptor == null )
                {
                    throw new ValueSerializationException( "Specified value type could not be resolved: " + typeInfo );
                }
                valueCompositeType = valueDescriptor.valueType();
            }
            else // without _type info
            {
                ValueDescriptor valueDescriptor = valuesModule().valueDescriptor( valueType.types()
                                                                                      .findFirst()
                                                                                      .get()
                                                                                      .getName() );
                if( valueDescriptor == null )
                {
                    throw new ValueSerializationException( "Don't know how to deserialize " + inputNode );
                }
                valueCompositeType = valueDescriptor.valueType();
            }
            Class<?> valueBuilderType = valueCompositeType.types().findFirst().orElse( null );
            return deserializeValueComposite( valueCompositeType, valueBuilderType, inputNode );
        }
        // Last resort : base64 java deserialization
        return (T) deserializeBase64Serialized( inputNode );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T deserializeBase64Serialized( InputNodeType inputNode )
        throws Exception
    {
        Object value = asSimpleValue( inputNode );
        if( value == null )
        {
            return null;
        }
        String base64 = value.toString();
        return deserializeBase64Serialized( base64 );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T deserializeBase64Serialized( String inputString )
        throws Exception
    {
        byte[] bytes = inputString.getBytes( UTF_8 );
        bytes = Base64Encoder.decode( bytes );
        Object result;
        try (ObjectInputStream oin = new ObjectInputStream( new ByteArrayInputStream( bytes ) ))
        {
            result = oin.readObject();
        }
        return (T) result;
    }

    //
    // Deserialization Extension Points
    //

    /**
     * Called by the adapter on deserialization start, after {@link #adaptInput(java.io.InputStream)}.
     *
     * @param valueType ValueType
     * @param input     Input
     *
     * @throws Exception that will be wrapped in a {@link ValueSerializationException}
     */
    @SuppressWarnings( "UnusedParameters" )
    protected void onDeserializationStart( ValueType valueType, InputType input )
        throws Exception
    {
        // NOOP
    }

    /**
     * Called by the adapter on deserialization end.
     *
     * @param valueType ValueType
     * @param input     Input
     *
     * @throws Exception that will be wrapped in a {@link ValueSerializationException}
     */
    protected void onDeserializationEnd( ValueType valueType, InputType input )
        throws Exception
    {
        // NOOP
    }

    //
    // Pull Parsing Deserialization
    //

    /**
     * This method is always called first, this is a chance to wrap the input type.
     *
     * @param input InputStream to adapt
     *
     * @return Adapted input
     *
     * @throws Exception that will be wrapped in a {@link ValueSerializationException}
     */
    protected abstract InputType adaptInput( InputStream input )
        throws Exception;

    /**
     * @param input Input
     *
     * @return a Plain Value read from the input
     *
     * @throws Exception that will be wrapped in a {@link ValueSerializationException}
     */
    protected abstract Object readPlainValue( InputType input )
        throws Exception;

    /**
     * @param <T>          Parameterized collection type
     * @param input        Input
     * @param deserializer Deserialization function
     * @param collection   Collection
     *
     * @return The filled collection or null if no array
     *
     * @throws Exception that will be wrapped in a {@link ValueSerializationException}
     */
    protected abstract <T> Collection<T> readArrayInCollection( InputType input,
                                                                Function<InputType, T> deserializer,
                                                                Collection<T> collection
    )
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
     * This allow to use any type as keys and values while keeping the Map order at the cost of having
     * non-predictible order of key/value inside an entry object.
     * </p>
     *
     * @param <K>               Parameterized map key type
     * @param <V>               Parameterized map value type
     * @param input             Input
     * @param keyDeserializer   Map key deserialization function
     * @param valueDeserializer Map value deserialization function
     * @param map               Map
     *
     * @return The filled map or null if no array
     *
     * @throws Exception that will be wrapped in a {@link ValueSerializationException}
     */
    protected abstract <K, V> Map<K, V> readMapInMap( InputType input,
                                                      Function<InputType, K> keyDeserializer,
                                                      Function<InputType, V> valueDeserializer,
                                                      Map<K, V> map
    )
        throws Exception;

    /**
     * @param input Input
     *
     * @return an InputNodeType or null if the value was null
     *
     * @throws Exception that will be wrapped in a {@link ValueSerializationException}
     */
    protected abstract InputNodeType readObjectTree( InputType input )
        throws Exception;

    //
    // Tree Parsing Deserialization
    //
    protected abstract Object asSimpleValue( InputNodeType inputNode )
        throws Exception;

    protected abstract boolean isObjectValue( InputNodeType inputNode )
        throws Exception;

    protected abstract boolean objectHasField( InputNodeType inputNode, String key )
        throws Exception;

    /**
     * Return null if the field do not exists.
     *
     * @param <T>               Parameterized object field value type
     * @param inputNode         Input Node
     * @param key               Object key
     * @param valueDeserializer Deserialization function
     *
     * @return The value of the field.
     *
     * @throws Exception that will be wrapped in a {@link ValueSerializationException}
     */
    protected abstract <T> T getObjectFieldValue( InputNodeType inputNode,
                                                  String key,
                                                  Function<InputNodeType, T> valueDeserializer
    )
        throws Exception;

    protected abstract <T> void putArrayNodeInCollection( InputNodeType inputNode,
                                                          Function<InputNodeType, T> deserializer,
                                                          Collection<T> collection
    )
        throws Exception;

    protected abstract <K, V> void putArrayNodeInMap( InputNodeType inputNode,
                                                      Function<InputNodeType, K> keyDeserializer,
                                                      Function<InputNodeType, V> valueDeserializer,
                                                      Map<K, V> map
    )
        throws Exception;

    protected abstract <V> void putObjectNodeInMap( InputNodeType inputNode,
                                                    Function<InputNodeType, V> valueDeserializer,
                                                    Map<String, V> map
    )
        throws Exception;
}
