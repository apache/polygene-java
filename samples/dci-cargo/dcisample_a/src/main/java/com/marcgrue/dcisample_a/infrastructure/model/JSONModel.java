package com.marcgrue.dcisample_a.infrastructure.model;

import org.qi4j.api.value.ValueComposite;

/**
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
        json = ( (U) valueComposite ).toJSON();   // Unchecked cast
        this.valueCompositeClass = valueCompositeClass;
    }

    @SuppressWarnings( "unchecked" )
    public static <T, U extends ValueComposite> JSONModel<T, U> of( T value )
    {
        if (!(value instanceof ValueComposite))
            throw new RuntimeException( value + " has to be an instance of a ValueComposite." );

        // Get ValueComposite interface
        Class<U> valueCompositeClass =  (Class<U>) ( (ValueComposite) value ).type();   // Unchecked cast

        return new JSONModel<T, U>( value, valueCompositeClass);
    }

    @SuppressWarnings( "unchecked" )
    public T getObject()
    {
        if (valueComposite == null && json != null)
        {
            // De-serialize
            valueComposite = (T) vbf.newValueFromJSON( valueCompositeClass, json ); // Unchecked cast
        }
        return valueComposite;
    }

    public void detach()
    {
        valueComposite = null;
    }
}