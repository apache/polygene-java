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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.spi.property.DefaultValues;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;

/**
 * ValueComposite type
 */
public final class ValueCompositeType
    extends AbstractStringType
{
    public static boolean isValueComposite( Type type )
    {
        return ValueComposite.class.isAssignableFrom( Classes.getRawClass( type ) );
    }

    private List<PropertyType> types;

    public ValueCompositeType( TypeName type, List<PropertyType> types )
    {
        super( type );
        this.types = types;
    }

    public List<PropertyType> types()
    {
        return types;
    }

    @Override
    public boolean isValue()
    {
        return true;
    }

    public void toJSON( Object value, JSONWriter json )
        throws JSONException
    {
        if( value == null )
        {
            json.value( null );
            return;
        }

        json.object();
        ValueComposite valueComposite = (ValueComposite) value;
        StateHolder state = valueComposite.state();
        final Map<QualifiedName, Object> values = new HashMap<QualifiedName, Object>();
        state.visitProperties( new StateHolder.StateVisitor()
        {
            public void visitProperty( QualifiedName name, Object value )
            {
                values.put( name, value );
            }
        } );

        List<PropertyType> actualTypes = types;
        if( !value.getClass().getInterfaces()[ 0 ].getName().equals( type.name() ) )
        {
            // Actual value is a subtype - use it instead
            ValueModel valueModel = (ValueModel) ValueInstance.getValueInstance( (ValueComposite) value )
                .compositeModel();

            actualTypes = valueModel.valueType().types();
            json.key( "_type" ).value( valueModel.valueType().type().name() );
        }

        for( PropertyType propertyType : actualTypes )
        {
            json.key( propertyType.qualifiedName().name() );

            Object propertyValue = values.get( propertyType.qualifiedName() );
            if( propertyValue == null )
            {
                json.value( null );
            }
            else
            {
                propertyType.type().toJSON( propertyValue, json );
            }
        }
        json.endObject();
    }

    public Object toJSON( Object value )
        throws JSONException
    {
        if( value == null )
        {
            return null;
        }

        JSONObject object = new JSONObject();
        ValueComposite valueComposite = (ValueComposite) value;
        StateHolder state = valueComposite.state();
        final Map<QualifiedName, Object> values = new HashMap<QualifiedName, Object>();
        state.visitProperties( new StateHolder.StateVisitor()
        {
            public void visitProperty( QualifiedName name, Object value )
            {
                values.put( name, value );
            }
        } );

        List<PropertyType> actualTypes = types;
        if( !value.getClass().getInterfaces()[ 0 ].getName().equals( type.name() ) )
        {
            // Actual value is a subtype - use it instead
            ValueModel valueModel = (ValueModel) ValueInstance.getValueInstance( (ValueComposite) value )
                .compositeModel();

            actualTypes = valueModel.valueType().types();
            object.put( "_type", valueModel.valueType().type().name() );
        }

        for( PropertyType propertyType : actualTypes )
        {

            Object propertyValue = values.get( propertyType.qualifiedName() );
            if( propertyValue == null )
            {
                object.put( propertyType.qualifiedName().name(), JSONObject.NULL );
            }
            else
            {
                object.put( propertyType.qualifiedName().name(), propertyType.type().toJSON( propertyValue ) );
            }
        }
        return object;
    }

    public Object fromJSON( Object json, Module module )
        throws JSONException
    {
        JSONObject jsonObject = (JSONObject) json;

        ValueCompositeType actualValueType = this;
        List<PropertyType> actualTypes = types;
        String actualType = jsonObject.optString( "_type" );
        if( !actualType.equals( "" ) )
        {
            actualValueType = (ValueCompositeType) ( (ModuleSPI) module ).valueDescriptor( actualType ).valueType();
            actualTypes = actualValueType.types();
        }

        final Map<QualifiedName, Object> values = new HashMap<QualifiedName, Object>();
        for( PropertyType propertyType : actualTypes )
        {
            Object valueJson = null;
            try
            {
                valueJson = jsonObject.get( propertyType.qualifiedName().name() );

                Object value = null;
                if( valueJson != null && !valueJson.equals( JSONObject.NULL ) )
                {
                    value = propertyType.type().fromJSON( valueJson, module );
                }

                values.put( propertyType.qualifiedName(), value );
            }
            catch( JSONException e )
            {
                // Not found in JSON or wrong format - try defaulting it
                try
                {
                    Object defaultValue = DefaultValues.getDefaultValue( module.classLoader().loadClass( propertyType.type()
                        .type().name() ) );
                    values.put( propertyType.qualifiedName(), defaultValue );
                }
                catch( ClassNotFoundException e1 )
                {
                    // Didn't work, throw exception
                    throw e;
                }
            }
        }

        try
        {
            ValueBuilder valueBuilder = module.valueBuilderFactory()
                .newValueBuilder( module.classLoader().loadClass( actualValueType.type().name() ) );
            valueBuilder.withState( new StateHolder()
            {
                public <T> Property<T> getProperty( Method propertyMethod )
                {
                    return null;
                }

                public <T> Property<T> getProperty( QualifiedName name )
                {
                    return null;
                }

                public void visitProperties( StateVisitor visitor )
                {
                    for( Map.Entry<QualifiedName, Object> qualifiedNameObjectEntry : values.entrySet() )
                    {
                        visitor.visitProperty( qualifiedNameObjectEntry.getKey(), qualifiedNameObjectEntry.getValue() );
                    }
                }
            } );

            return valueBuilder.newInstance();
        }
        catch( ClassNotFoundException e )
        {
            throw new IllegalStateException( "Could not deserialize value", e );
        }
    }
}
