package org.qi4j.api.json;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.Qi4j;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.type.*;
import org.qi4j.api.util.Dates;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.Functions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * TODO
 */
public abstract class JSONSerializer
{
    private static Map<Class, Function<Object, Object>> typeFunctions = new HashMap<Class, Function<Object, Object>>();

    public static <T> void registerSerializer( Class<T> type, Function<T, Object> typeFunction )
    {
        typeFunctions.put( type, (Function<Object, Object>) typeFunction );
    }

    static
    {
        registerSerializer( String.class, Functions.<Object, String>identity() );

        // Primitive types
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
                return bigDecimal;
            }
        } );

        registerSerializer( BigInteger.class, new Function<BigInteger, Object>()
        {
            @Override
            public Object map( BigInteger bigInteger )
            {
                return bigInteger;
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
            public Object map( EntityReference date )
            {
                return date.toString();
            }
        } );
    }

    private boolean includeTypeInformation = true;

    public void setIncludeType(boolean includeTypeInformation)
    {
        this.includeTypeInformation = includeTypeInformation;
    }

    public void serialize( ValueComposite value ) throws JSONException
    {
        ValueDescriptor valueDescriptor = (ValueDescriptor) Qi4j.DESCRIPTOR_FUNCTION.map(value);

        ValueType valueType = valueDescriptor.valueType();

        serialize( value, valueType );
    }

    public void serialize( Object value, ValueType valueType ) throws JSONException
    {
        // Check for null first
        if( value == null )
        {
            value( null );
            return;
        }

        // Try functions second
        Function<Object, Object> typeFunction = typeFunctions.get( valueType.type() );
        if( typeFunction != null )
        {
            value( typeFunction.map( value ) );
        } else if( valueType instanceof ValueCompositeType ) // Handle all other types
        {
            ValueCompositeType valueCompositeType = (ValueCompositeType) valueType;

            objectStart();
            ValueComposite valueComposite = (ValueComposite) value;

            if( !valueCompositeType.type().equals( Qi4j.DESCRIPTOR_FUNCTION.map( valueComposite ).type() ) )
            {
                // Actual value is a subtype - use it instead
                ValueDescriptor valueDescriptor = (ValueDescriptor) Qi4j.DESCRIPTOR_FUNCTION.map( valueComposite );

                valueCompositeType = valueDescriptor.valueType();

                if (includeTypeInformation)
                    key("_type").value( valueDescriptor.valueType().type().getName() );
            }

            AssociationStateHolder state = (AssociationStateHolder) Qi4j.INSTANCE_FUNCTION.map( valueComposite ).state();
            for( PropertyDescriptor persistentProperty : valueCompositeType.properties() )
            {
                Property<?> property = state.propertyFor( persistentProperty.accessor() );
                key( persistentProperty.qualifiedName().name() ).serialize( property.get(), persistentProperty.valueType() );
            }
            for( AssociationDescriptor associationDescriptor : valueCompositeType.associations() )
            {
                Association<?> association = state.associationFor( associationDescriptor.accessor() );

                Object instance = association.get();
                if (instance != null)
                    key( associationDescriptor.qualifiedName().name() ).value( ((Identity)instance).identity().get() );
            }
            for( AssociationDescriptor associationDescriptor : valueCompositeType.manyAssociations() )
            {
                ManyAssociation<?> manyAssociation = state.manyAssociationFor( associationDescriptor.accessor() );
                key( associationDescriptor.qualifiedName().name() ).serialize( manyAssociation.toList(), new CollectionType( List.class, new ValueType( String.class ) ) );
            }
            objectEnd();
        } else if( valueType instanceof CollectionType )
        {
            CollectionType collectionType = (CollectionType) valueType;

            arrayStart();

            Collection collection = (Collection) value;
            for( Object collectionValue : collection )
            {
                serialize( collectionValue, collectionType.collectedType() );
            }

            arrayEnd();

        } else if( valueType instanceof MapType )
        {
            arrayStart();

            MapType mapType = (MapType) valueType;
            Map map = (Map) value;
            Set<Map.Entry> set = map.entrySet();
            for( Map.Entry<Object, Object> entry : set )
            {
                objectStart();
                key( "key" ).serialize( entry.getKey(), mapType.getKeyType() );
                key( "value" ).serialize( entry.getValue(), mapType.getValueType() );
                objectEnd();
            }

            arrayEnd();

        } else if (valueType instanceof EnumType )
        {
            value( value.toString() );
        } else
        {
/* TODO How to handle deserialization?
            // Try for the actual value class first
            typeFunction = typeFunctions.get( value.getClass() );
            if( typeFunction != null )
            {
                value( typeFunction.map( value ) );
                return;
            }
*/

            // Check if we are serializing an Entity
            if( value instanceof EntityComposite )
            {
                // Store reference instead
                value = EntityReference.getEntityReference( value );
            } else if( value instanceof ValueComposite )
            {
                // Serialize ValueComposite JSON instead
                try
                {
                    JSONObjectSerializer JSONObjectSerializer = new JSONObjectSerializer();
                    JSONObjectSerializer.serialize( (ValueComposite) value );

                    JSONObject object = (JSONObject) JSONObjectSerializer.getRoot();

                    ValueDescriptor descriptor = (ValueDescriptor) Qi4j.DESCRIPTOR_FUNCTION.map( (Composite)  value );

                    if (includeTypeInformation)
                        object.put( "_type", descriptor.type().getName() );
                    value( object );
                    return;
                } catch( JSONException e )
                {
                    throw new IllegalStateException( "Could not JSON serialize value", e );
                }
            }

            // Serialize value
            try
            {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream( bout );
                out.writeUnshared( value );
                out.close();
                byte[] bytes = Base64Encoder.encode( bout.toByteArray(), true );
                String stringValue = new String( bytes, "UTF-8" );
                value(stringValue);
            } catch( IOException e )
            {
                throw new IllegalArgumentException( "Could not serialize value", e );
            }
        }
    }

    public abstract JSONSerializer key(String key) throws JSONException;

    public abstract JSONSerializer value(Object value) throws JSONException;

    public abstract JSONSerializer objectStart() throws JSONException;

    public abstract JSONSerializer objectEnd() throws JSONException;

    public abstract JSONSerializer arrayStart() throws JSONException;

    public abstract JSONSerializer arrayEnd() throws JSONException;
}
