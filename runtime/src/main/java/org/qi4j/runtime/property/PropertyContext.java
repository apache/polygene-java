/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

import java.lang.reflect.Type;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyInfo;
import org.qi4j.runtime.composite.ConstraintsContext;
import org.qi4j.runtime.composite.ConstraintsInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;

/**
 * TODO
 */
public final class PropertyContext
    implements PropertyInfo
{
    private PropertyBinding propertyBinding;
    private ConstraintsInstance constraintsInstance;

    public PropertyContext( PropertyBinding propertyBinding, ConstraintsContext constraintsContext )
    {
        this.propertyBinding = propertyBinding;
        if( constraintsContext != null )
        {
            this.constraintsInstance = constraintsContext.newInstance();
        }
    }

    public PropertyBinding getPropertyBinding()
    {
        return propertyBinding;
    }

    public Property newInstance( ModuleInstance moduleInstance, Object value )
    {
        try
        {
            Class propertyType = propertyBinding.getPropertyResolution().getPropertyModel().getAccessor().getReturnType();

            if( Composite.class.isAssignableFrom( propertyType ) )
            {
                Class<? extends Composite> propertyCompositeType = (Class<? extends Composite>) propertyType;
                CompositeBuilder<? extends Composite> cb = moduleInstance.getStructureContext().getCompositeBuilderFactory().newCompositeBuilder( propertyCompositeType );
                cb.use( value );
                cb.use( propertyBinding );
                return Property.class.cast( cb.newInstance() );
            }
            else
            {
                Property instance;
                if( ImmutableProperty.class.isAssignableFrom( propertyType ) )
                {
                    instance = new ImmutablePropertyInstance<Object>( this, value );
                }
                else
                {
                    instance = new PropertyInstance<Object>( this, value );

                }

                return instance;
            }
        }
        catch( Exception e )
        {
            throw new InvalidPropertyException( "Could not instantiate property", e );
        }
    }

    public Property newEntityInstance( ModuleInstance moduleInstance, EntityState entityState )
    {
        try
        {
            Class propertyType = propertyBinding.getPropertyResolution().getPropertyModel().getAccessor().getReturnType();

            if( Composite.class.isAssignableFrom( propertyType ) )
            {
                Class<? extends Composite> propertyCompositeType = (Class<? extends Composite>) propertyType;
                CompositeBuilder<? extends Composite> cb = moduleInstance.getStructureContext().getCompositeBuilderFactory().newCompositeBuilder( propertyCompositeType );
                cb.use( entityState );
                cb.use( propertyBinding );
                return Property.class.cast( cb.newInstance() );
            }
            else
            {
                Property instance;
                if( ImmutableProperty.class.isAssignableFrom( propertyType ) )
                {
                    instance = new ImmutablePropertyInstance<Object>( this, entityState.getProperty( qualifiedName() ) );
                }
                else
                {
                    instance = new EntityPropertyInstance<Object>( this, entityState );
                }

                return instance;
            }
        }
        catch( Exception e )
        {
            throw new InvalidPropertyException( "Could not instantiate property", e );
        }
    }

    public ConstraintsInstance getConstraintsInstance()
    {
        return constraintsInstance;
    }

    public <T> T metaInfo( Class<T> infoClass )
    {
        return infoClass.cast( propertyBinding.metaInfo( infoClass ) );
    }

    public String name()
    {
        return propertyBinding.getPropertyResolution().getPropertyModel().getName();
    }

    public String qualifiedName()
    {
        return propertyBinding.getPropertyResolution().getPropertyModel().getQualifiedName();
    }

    public Type type()
    {
        return propertyBinding.getPropertyResolution().getPropertyModel().getType();
    }
}
