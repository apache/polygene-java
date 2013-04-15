/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.infrastructure.model;

import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueSerializer;

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
        json = module.findService( ValueSerializer.class ).get().serialize( (U) valueComposite );;
        this.valueCompositeClass = valueCompositeClass;
    }

    @SuppressWarnings( "unchecked" )
    public static <T, U extends ValueComposite> JSONModel<T, U> of( T value )
    {
        if( !( value instanceof ValueComposite ) )
        {
            throw new RuntimeException( value + " has to be an instance of a ValueComposite." );
        }

        // Get ValueComposite interface
        Class<U> valueCompositeClass = (Class<U>) qi4j.valueDescriptorFor( value ).valueType().mainType();

        return new JSONModel<T, U>( value, valueCompositeClass );
    }

    @SuppressWarnings( "unchecked" )
    public T getObject()
    {
        if( valueComposite == null && json != null )
        {
            // De-serialize
            valueComposite = (T) module.newValueFromSerializedState( valueCompositeClass, json ); // Unchecked cast
        }
        return valueComposite;
    }

    public void detach()
    {
        valueComposite = null;
    }
}
