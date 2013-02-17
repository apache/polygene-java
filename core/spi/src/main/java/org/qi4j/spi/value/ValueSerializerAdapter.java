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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.qi4j.api.Qi4j;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.util.Base64Encoder;
import org.qi4j.api.util.Dates;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.api.value.ValueSerializer;
import org.qi4j.functional.Function;
import org.qi4j.functional.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.functional.Iterables.*;

/**
 * Adapter for pull-parsing capable ValueSerializers.
 *
 * <p>
 *     Among Plain values (see {@link ValueSerializer}) some are considered primitives to underlying serialization
 *     mechanisms and by so handed/come without conversion to/from implementations. Primitive values can be one of:
 * </p>
 * <ul>
 *     <li>String,</li>
 *     <li>Character or char,</li>
 *     <li>Boolean or boolean,</li>
 *     <li>Integer or int,</li>
 *     <li>Long or long,</li>
 *     <li>Short or short,</li>
 *     <li>Byte or byte,</li>
 *     <li>Float or float,</li>
 *     <li>Double or double.</li>
 * </ul>
 *
 * @param <OutputType> Implementor output type
 */
public abstract class ValueSerializerAdapter<OutputType>
    implements ValueSerializer
{

    private static final Logger LOG = LoggerFactory.getLogger( ValueSerializerAdapter.class );
    private static final String UTF_8 = "UTF-8";
    private final Map<Class<?>, Function<Object, Object>> serializers = new HashMap<Class<?>, Function<Object, Object>>();

    /**
     * Register a Plain Value type serialization Function.
     *
     * @param <T> Plain Value parametrized Type
     * @param type Plain Value Type
     * @param deserializer Serialization Function
     */
    @SuppressWarnings( "unchecked" )
    protected final <T> void registerSerializer( Class<T> type, Function<T, Object> serializer )
    {
        serializers.put( type, (Function<Object, Object>) serializer );
    }

    public ValueSerializerAdapter()
    {
        // Primitive Value types
        registerSerializer( String.class, Functions.<Object, String>identity() );
        registerSerializer( Character.class, Functions.<Object, Character>identity() );
        registerSerializer( Boolean.class, Functions.<Object, Boolean>identity() );
        registerSerializer( Integer.class, Functions.<Object, Integer>identity() );
        registerSerializer( Long.class, Functions.<Object, Long>identity() );
        registerSerializer( Short.class, Functions.<Object, Short>identity() );
        registerSerializer( Byte.class, Functions.<Object, Byte>identity() );
        registerSerializer( Float.class, Functions.<Object, Float>identity() );
        registerSerializer( Double.class, Functions.<Object, Double>identity() );

        // Number types
        registerSerializer( BigDecimal.class, new Function<BigDecimal, Object>()
        {
            @Override
            public Object map( BigDecimal bigDecimal )
            {
                return bigDecimal.toString();
            }
        } );
        registerSerializer( BigInteger.class, new Function<BigInteger, Object>()
        {
            @Override
            public Object map( BigInteger bigInteger )
            {
                return bigInteger.toString();
            }
        } );

        // Date types
        registerSerializer( Date.class, new Function<Date, Object>()
        {
            @Override
            public Object map( Date date )
            {
                return Dates.toUtcString( date );
            }
        } );
        registerSerializer( DateTime.class, new Function<DateTime, Object>()
        {
            @Override
            public Object map( DateTime date )
            {
                return date.toString();
            }
        } );
        registerSerializer( LocalDateTime.class, new Function<LocalDateTime, Object>()
        {
            @Override
            public Object map( LocalDateTime date )
            {
                return date.toString();
            }
        } );
        registerSerializer( LocalDate.class, new Function<LocalDate, Object>()
        {
            @Override
            public Object map( LocalDate date )
            {
                return date.toString();
            }
        } );

        // Other supported types
        registerSerializer( EntityReference.class, new Function<EntityReference, Object>()
        {
            @Override
            public Object map( EntityReference ref )
            {
                return ref.toString();
            }
        } );
    }

    @Override
    public final <T> Function<T, String> serialize()
    {
        return new Function<T, String>()
        {
            @Override
            public String map( T object )
            {
                return serialize( object );
            }
        };
    }

    @Override
    public final <T> Function<T, String> serialize( final boolean includeTypeInfo )
    {
        return new Function<T, String>()
        {
            @Override
            public String map( T object )
            {
                return serialize( object, includeTypeInfo );
            }
        };
    }

    @Override
    public final String serialize( Object object )
        throws ValueSerializationException
    {
        return serialize( object, true );
    }

    @Override
    public final String serialize( Object object, boolean includeTypeInfo )
        throws ValueSerializationException
    {
        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            serializeRoot( object, output, includeTypeInfo );
            return output.toString( UTF_8 );
        }
        catch( ValueSerializationException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new ValueSerializationException( "Could not serialize value", ex );
        }
    }

    @Override
    public final void serialize( Object object, OutputStream output )
        throws ValueSerializationException
    {
        serialize( object, output, true );
    }

    @Override
    public final void serialize( Object object, OutputStream output, boolean includeTypeInfo )
        throws ValueSerializationException
    {
        try
        {
            serializeRoot( object, output, includeTypeInfo );
        }
        catch( ValueSerializationException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new ValueSerializationException( "Could not serialize value", ex );
        }
    }

    private void serializeRoot( Object object, OutputStream output, boolean includeTypeInfo )
        throws Exception
    {
        if( object != null )
        {
            // System.out.println( ">>>>>>>>>>>> " + ( object == null ? "null" : object.getClass() ) );
            if( serializers.get( object.getClass() ) != null )
            {
                // Plain Value
                Object serialized = serializers.get( object.getClass() ).map( object );
                output.write( serialized.toString().getBytes( UTF_8 ) );
            }
            else if( object.getClass().isEnum() )
            {
                // Enum Value
                output.write( object.toString().getBytes( UTF_8 ) );
            }
            else if( object.getClass().isArray() )
            {
                // Array Value
                output.write( serializeBase64Serializable( object ).getBytes( UTF_8 ) );
            }
            else
            {
                // Complex Value
                OutputType adaptedOutput = adaptOutput( output );
                onSerializationStart( object, adaptedOutput );
                doSerialize( object, adaptedOutput, includeTypeInfo, true );
                onSerializationEnd( object, adaptedOutput );
            }
        }
    }

    private void doSerialize( Object object, OutputType output, boolean includeTypeInfo, boolean rootPass )
        throws Exception
    {
        // Null
        if( object == null )
        {
            LOG.trace( "Null object -> onValue( null )" );
            onValue( output, null );
        }
        else // Registered serializer
        if( serializers.get( object.getClass() ) != null )
        {
            LOG.trace( "Registered serializer matches -> onValue( serialized )" );
            onValue( output, serializers.get( object.getClass() ).map( object ) );
        }
        else // ValueComposite
        if( ValueComposite.class.isAssignableFrom( object.getClass() ) )
        {
            LOG.trace( "ValueComposite assignable -> serializeValueComposite( object )" );
            serializeValueComposite( object, output, includeTypeInfo, rootPass );
        }
        else // EntityComposite
        if( EntityComposite.class.isAssignableFrom( object.getClass() ) )
        {
            LOG.trace( "EntityComposite assignable -> serializeEntityComposite( object )" );
            serializeEntityComposite( object, output );
        }
        else // Collection - Iterable
        if( Iterable.class.isAssignableFrom( object.getClass() ) )
        {
            LOG.trace( "Iterable assignable -> serializeIterable( object )" );
            serializeIterable( object, output, includeTypeInfo );
        }
        else // Array - QUID Remove this and use java serialization for arrays?
        if( object.getClass().isArray() )
        {
            LOG.trace( "Object isArray -> serializeBase64Serializable( object )" );
            serializeBase64Serializable( object, output );
        }
        else // Map
        if( Map.class.isAssignableFrom( object.getClass() ) )
        {
            LOG.trace( "Map assignable -> serializeMap( object )" );
            serializeMap( object, output, includeTypeInfo );
        }
        else // Enum
        if( object.getClass().isEnum() )
        {
            LOG.trace( "Object is an enum -> onValue( object.toString() )" );
            onValue( output, object.toString() );
        }
        else // Fallback to Base64 encoded Java Serialization
        {
            LOG.trace( "Unknown object type -> serializeBase64Serializable( object )" );
            serializeBase64Serializable( object, output );
        }
    }

    private void serializeValueComposite( Object object, OutputType output, boolean includeTypeInfo, boolean rootPass )
        throws Exception
    {
        CompositeInstance valueInstance = Qi4j.FUNCTION_COMPOSITE_INSTANCE_OF.map( (ValueComposite) object );
        ValueDescriptor descriptor = (ValueDescriptor) valueInstance.descriptor();
        AssociationStateHolder state = (AssociationStateHolder) valueInstance.state();

        onObjectStart( output );

        if( includeTypeInfo && !rootPass )
        {
            onFieldStart( output, "_type" );
            onValueStart( output );
            onValue( output, first( descriptor.valueType().types() ).getName() );
            onValueEnd( output );
            onFieldEnd( output );
        }

        for( PropertyDescriptor persistentProperty : descriptor.valueType().properties() )
        {
            Property<?> property = state.propertyFor( persistentProperty.accessor() );
            onFieldStart( output, persistentProperty.qualifiedName().name() );
            onValueStart( output );
            doSerialize( property.get(), output, includeTypeInfo, false );
            onValueEnd( output );
            onFieldEnd( output );
        }
        for( AssociationDescriptor associationDescriptor : descriptor.valueType().associations() )
        {
            Association<?> association = state.associationFor( associationDescriptor.accessor() );
            Object instance = association.get();
            onFieldStart( output, associationDescriptor.qualifiedName().name() );
            onValueStart( output );
            if( instance == null )
            {
                onValue( output, null );
            }
            else
            {
                onValue( output, ( (Identity) instance ).identity().get() );
            }
            onValueEnd( output );
            onFieldEnd( output );
        }
        for( AssociationDescriptor associationDescriptor : descriptor.valueType().manyAssociations() )
        {
            ManyAssociation<?> manyAssociation = state.manyAssociationFor( associationDescriptor.accessor() );
            onFieldStart( output, associationDescriptor.qualifiedName().name() );
            onValueStart( output );
            onArrayStart( output );
            for( Object instance : manyAssociation )
            {
                onValueStart( output );
                onValue( output, ( (Identity) instance ).identity().get() );
                onValueEnd( output );
            }
            onArrayEnd( output );
            onValueEnd( output );
            onFieldEnd( output );
        }

        onObjectEnd( output );
    }

    private void serializeEntityComposite( Object object, OutputType output )
        throws Exception
    {
        onValue( output, EntityReference.entityReferenceFor( object ) );
    }

    private void serializeIterable( Object object, OutputType output, boolean includeTypeInfo )
        throws Exception
    {
        @SuppressWarnings( "unchecked" )
        Iterable<Object> collection = (Iterable<Object>) object;
        onArrayStart( output );
        for( Object item : collection )
        {
            onValueStart( output );
            doSerialize( item, output, includeTypeInfo, false );
            onValueEnd( output );
        }
        onArrayEnd( output );
    }

    private void serializeMap( Object object, OutputType output, boolean includeTypeInfo )
        throws Exception
    {
        @SuppressWarnings( "unchecked" )
        Map<Object, Object> map = (Map<Object, Object>) object;
        onArrayStart( output );
        for( Map.Entry<Object, Object> entry : map.entrySet() )
        {
            onObjectStart( output );

            onFieldStart( output, "key" );
            onValueStart( output );
            onValue( output, entry.getKey().toString() );
            onValueEnd( output );
            onFieldEnd( output );

            onFieldStart( output, "value" );
            onValueStart( output );
            doSerialize( entry.getValue(), output, includeTypeInfo, false );
            onValueEnd( output );
            onFieldEnd( output );

            onObjectEnd( output );
        }
        onArrayEnd( output );
    }

    private void serializeBase64Serializable( Object object, OutputType output )
        throws Exception
    {
        onValue( output, serializeBase64Serializable( object ) );
    }

    private String serializeBase64Serializable( Object object )
        throws Exception
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( bout );
        out.writeUnshared( object );
        out.close();
        byte[] bytes = Base64Encoder.encode( bout.toByteArray(), true );
        return new String( bytes, UTF_8 );
    }

    protected abstract OutputType adaptOutput( OutputStream output )
        throws Exception;

    protected void onSerializationStart( Object object, OutputType output )
        throws Exception
    {
        // NOOP
    }

    protected void onSerializationEnd( Object object, OutputType output )
        throws Exception
    {
        // NOOP
    }

    protected abstract void onArrayStart( OutputType output )
        throws Exception;

    protected abstract void onArrayEnd( OutputType output )
        throws Exception;

    protected abstract void onObjectStart( OutputType output )
        throws Exception;

    protected abstract void onObjectEnd( OutputType output )
        throws Exception;

    protected abstract void onFieldStart( OutputType output, String fieldName )
        throws Exception;

    protected void onFieldEnd( OutputType output )
        throws Exception
    {
        // NOOP
    }

    protected void onValueStart( OutputType output )
        throws Exception
    {
        // NOOP
    }

    protected abstract void onValue( OutputType output, Object value )
        throws Exception;

    protected void onValueEnd( OutputType output )
        throws Exception
    {
        // NOOP
    }
}
