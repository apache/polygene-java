/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.property;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.functional.Function;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.runtime.value.ValueModel;

import java.lang.reflect.AccessibleObject;
import java.util.*;

/**
 * Base class for properties model
 */
public class PropertiesModel<T extends PropertyModel>
    implements VisitableHierarchy<Object, Object>
{
    protected final Set<T> propertyModels = new LinkedHashSet<T>();
    protected final Map<AccessibleObject, T> mapAccessiblePropertyModel = new HashMap<AccessibleObject,T>();

    public PropertiesModel()
    {
    }

    public void addProperty(T property)
    {
        propertyModels.add( property );
        mapAccessiblePropertyModel.put( property.accessor(), property );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor ) throws ThrowableType
    {
        if (visitor.visitEnter( this ))
        {
            for( T propertyModel : propertyModels )
            {
                if (!propertyModel.accept(visitor))
                    break;
            }
        }

        return visitor.visitLeave( this );
    }

    public Set<T> properties()
    {
        return propertyModels;
    }

    public PropertiesInstance newBuilderInstance( Function<PropertyDescriptor, Object> state)
    {
        Map<AccessibleObject, Property<?>> properties = new HashMap<AccessibleObject, Property<?>>();
        for( T propertyModel : propertyModels )
        {
            Property property;
            Object initialValue = state.map( propertyModel );

            initialValue = cloneInitialValue( initialValue, true );

            property = new PropertyInstance<Object>( propertyModel.getBuilderInfo(), initialValue );
            properties.put( propertyModel.accessor(), property );
        }

        return new PropertiesInstance( properties );
    }

    public PropertiesInstance newInitialInstance( ModuleInstance module )
    {
        Map<AccessibleObject, Property<?>> properties = new HashMap<AccessibleObject, Property<?>>();
        for( T propertyModel : propertyModels )
        {
            Property property = new PropertyInstance<Object>(propertyModel, propertyModel.initialValue( module ) );
            properties.put( propertyModel.accessor(), property );
        }

        return new PropertiesInstance( properties );
    }

    public PropertiesInstance newInstance( StateHolder state )
    {
        Map<AccessibleObject, Property<?>> properties = new HashMap<AccessibleObject, Property<?>>();
        for( PropertyModel propertyModel : propertyModels )
        {
            Property<Object> prop = state.propertyFor( propertyModel.accessor() );
            Object initialValue = prop.get();

            initialValue = cloneInitialValue( initialValue, false );

            // Create property instance
            prop = new PropertyInstance<Object>( propertyModel, initialValue );
            properties.put( propertyModel.accessor(), prop );
        }
        return new PropertiesInstance( properties );
    }

    private Object cloneInitialValue( Object initialValue, boolean isPrototype )
    {
        if( initialValue instanceof Collection )
        {
            Collection<Object> initialCollection = (Collection<Object>) initialValue;
            Collection<Object> newCollection;
            // Create new unmodifiable collection
            if( initialValue instanceof List )
            {
                newCollection = new ArrayList<Object>();
                initialValue = isPrototype ? newCollection : Collections.unmodifiableList( (List<Object>) newCollection );
            }
            else
            {
                newCollection = new HashSet<Object>();
                initialValue = isPrototype ? newCollection : Collections.unmodifiableSet( (Set<Object>) newCollection );
            }

            // Copy values, ensuring that values are cloned correctly
            for( Object value : initialCollection )
            {
                if( value instanceof ValueComposite )
                {
                    value = cloneValue( value, isPrototype );
                }

                newCollection.add( value );
            }
        }
        else if( initialValue instanceof ValueComposite )
        {
            initialValue = cloneValue( initialValue, isPrototype );
        }
        return initialValue;
    }

    private Object cloneValue( Object value, boolean isPrototype )
    {
        // Create real value
        final ValueInstance instance = ValueInstance.getValueInstance( (ValueComposite) value );

        ValueModel model = (ValueModel) instance.compositeModel();
        StateHolder state;
        if( isPrototype )
        {
            state = model.state().newBuilderInstance( new Function<PropertyDescriptor, Object>()
                    {
                        @Override
                        public Object map( PropertyDescriptor propertyDescriptor )
                        {
                            return instance.state().propertyFor( propertyDescriptor.accessor() ).get();
                        }
                        });
        }
        else
        {
            state = model.state().newInstance( instance.state() );
        }
        ValueInstance newInstance = model.newValueInstance( instance.module(), state );
        return newInstance.proxy();
    }

    public T getProperty(AccessibleObject accessor)
    {
        return mapAccessiblePropertyModel.get( accessor );
    }

    public T getPropertyByName( String name )
    {
        for( T propertyModel : propertyModels )
        {
            if( propertyModel.qualifiedName().name().equals( name ) )
            {
                return propertyModel;
            }
        }
        return null;
    }

    public T getPropertyByQualifiedName( QualifiedName name )
    {
        for( T propertyModel : propertyModels )
        {
            if( propertyModel.qualifiedName().equals( name ) )
            {
                return propertyModel;
            }
        }
        return null;
    }

    public void checkConstraints( PropertiesInstance properties )
        throws ConstraintViolationException
    {
        for( PropertyModel propertyModel : propertyModels )
        {
            propertyModel.checkConstraints( properties );
        }
    }
}