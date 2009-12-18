/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.util.Base64Encoder;
import org.qi4j.spi.value.ValueDescriptor;

/**
 * Serializable type. If the serialized object is an ValueComposite,
 * then use JSON format for VC's. If the serialized object is an
 * EntityReference, then use JSON format for EntityReferences.
 */
public final class SerializableType
    extends AbstractStringType
{
    public SerializableType( TypeName type )
    {
        super( type );
    }

    public void toJSON( Object value, JSONWriter json )
        throws JSONException
    {
        json.value( toJSON( value ) );
    }

    public Object toJSON( Object value )
        throws JSONException
    {
        // Check if we are serializing an Entity
        if( value instanceof EntityComposite )
        {
            // Store reference instead
            value = EntityReference.getEntityReference( value );
        }
        else if( value instanceof ValueComposite )
        {
            // Serialize ValueComposite JSON instead
            CompositeInstance instance = (CompositeInstance) Proxy.getInvocationHandler( value );
            ValueDescriptor descriptor = (ValueDescriptor) instance.descriptor();
            ValueType valueType = descriptor.valueType();
            try
            {
                JSONObject object = (JSONObject) valueType.toJSON( value );
                object.put( "_type", descriptor.type().getName() );
                return object;
            }
            catch( JSONException e )
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
            return stringValue;
        }
        catch( IOException e )
        {
            throw new IllegalArgumentException( "Could not serialize value", e );
        }
    }

    public Object fromJSON( Object json, Module module )
        throws JSONException
    {
        try
        {
            if( json instanceof JSONObject )
            {
                // ValueComposite deserialization
                JSONObject jsonObject = (JSONObject) json;
                String type = jsonObject.getString( "_type" );

                ValueDescriptor valueDescriptor = ( (ModuleSPI) module ).valueDescriptor( type );
                return valueDescriptor.valueType().fromJSON( json, module );
            }
            else
            {
                String serializedString = (String) json;
                byte[] bytes = serializedString.getBytes( "UTF-8" );
                bytes = Base64Encoder.decode( bytes );
                ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
                ObjectInputStream oin = new ObjectInputStream( bin );
                Object result = oin.readObject();
                oin.close();

                if( result instanceof EntityReference )
                {
                    EntityReference ref = (EntityReference) result;
                    if( !type.isClass( EntityReference.class ) )
                    {
                        Class mixinType = module.classLoader().loadClass( type.name() );
                        UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
                        if( unitOfWork != null )
                        {
                            result = unitOfWork.get( mixinType, ref.identity() );
                        }
                    }
                }

                return result;
            }
        }
        catch( IOException e )
        {
            throw new IllegalStateException( "Could not deserialize value", e );
        }
        catch( ClassNotFoundException e )
        {
            throw new IllegalStateException( "Could not find class for serialized value", e );
        }
    }

    @Override
    public String toQueryParameter( Object value )
    {
        String json = super.toQueryParameter( value );
        return json.substring( 1, json.length() - 1 );
    }

    @Override
    public Object fromQueryParameter( String parameter, Module module )
        throws JSONException
    {
        String json = "\"" + parameter + "\"";

        return fromJSON( json, module );
    }
}
