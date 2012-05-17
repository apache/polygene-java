package com.marcgrue.dcisample_b.infrastructure.model;

import org.json.JSONException;
import org.qi4j.api.json.JSONObjectSerializer;
import org.qi4j.api.value.ValueComposite;

/**
 * JSONModel
 *
 * Model that can serialize/de-serialize an object to/from a JSON string.
 */
public class JSONModel<T, U extends ValueComposite>
      extends ReadOnlyModel<T>
{
    private Class<U> valueCompositeClass;
    private String json;
    private transient T valueComposite;

    @SuppressWarnings( "unchecked" )
    public JSONModel( T valueComposite, Class<U> valueCompositeClass )
    {
        JSONObjectSerializer jsonSerializer = new JSONObjectSerializer();
        try {
            jsonSerializer.serialize((U) valueComposite);
        } catch (JSONException e) {
            throw new RuntimeException( "Cannot serialize ValueComposite: " + valueComposite );
        }
        json = jsonSerializer.getRoot().toString();
        this.valueCompositeClass = valueCompositeClass;
    }

    @SuppressWarnings( "unchecked" )
    public static <T, U extends ValueComposite> JSONModel<T, U> of( T value )
    {
        if (!(value instanceof ValueComposite))
            throw new RuntimeException( value + " has to be an instance of a ValueComposite." );

        // Get ValueComposite interface
        Class<U> valueCompositeClass =  (Class<U>) qi4j.getValueDescriptor(value).valueType().mainType();

        return new JSONModel<T, U>( value, valueCompositeClass);
    }

    @SuppressWarnings( "unchecked" )
    public T getObject()
    {
        if (valueComposite == null && json != null)
        {
            // De-serialize
            valueComposite = (T) module.newValueFromJSON( valueCompositeClass, json ); // Unchecked cast
        }
        return valueComposite;
    }

    public void detach()
    {
        valueComposite = null;
    }
}