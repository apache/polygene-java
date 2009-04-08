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

package org.qi4j.runtime.composite;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.property.AbstractPropertiesModel;
import org.qi4j.runtime.property.PropertiesInstance;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Base model for Composite state
 */
public abstract class AbstractStateModel<T extends AbstractPropertiesModel>
    implements StateDescriptor, Serializable, Binder
{
    protected final T propertiesModel;

    public AbstractStateModel( T propertiesModel )
    {
        this.propertiesModel = propertiesModel;
    }

    public StateHolder newInitialInstance()
    {
        PropertiesInstance properties = propertiesModel.newInitialInstance();
        return new StateInstance( properties );
    }

    public StateHolder newBuilderInstance()
    {
        PropertiesInstance properties = propertiesModel.newBuilderInstance();
        return new StateInstance( properties );
    }

    public StateHolder newBuilderInstance( StateHolder state )
    {
        PropertiesInstance properties = propertiesModel.newBuilderInstance( state );
        return new StateInstance( properties );
    }

    public StateHolder newInstance( StateHolder state )
    {
        PropertiesInstance properties = propertiesModel.newInstance( state );
        return new StateInstance( properties );
    }

    public void addStateFor( Iterable<Method> methods )
    {
        for( Method method : methods )
        {
            propertiesModel.addPropertyFor( method );
        }
    }

    public PropertyDescriptor getPropertyByName( String name )
    {
        return propertiesModel.getPropertyByName( name );
    }

    public PropertyDescriptor getPropertyByQualifiedName( QualifiedName name )
    {
        return propertiesModel.getPropertyByQualifiedName( name );
    }

    public List<? extends PropertyDescriptor> properties()
    {
        return propertiesModel.properties();
    }

    public void bind( Resolution resolution ) throws BindingException
    {
        propertiesModel.bind( resolution );
    }

    public List<AssociationDescriptor> associations()
    {
        return Collections.EMPTY_LIST;
    }

    public void checkConstraints( StateHolder state, boolean allowNull )
        throws ConstraintViolationException
    {
        StateInstance stateInstance = (AbstractStateModel.StateInstance) state;
        stateInstance.checkConstraints( allowNull );
    }

    public final class StateInstance
        implements StateHolder
    {
        private final PropertiesInstance properties;

        public StateInstance( PropertiesInstance properties )
        {
            this.properties = properties;
        }

        public Property<?> getProperty( Method propertyMethod )
        {
            return properties.propertyFor( propertyMethod );
        }

        public void checkConstraints( boolean allowNull )
            throws ConstraintViolationException
        {
            propertiesModel.checkConstraints( properties, allowNull );
        }

        @Override
        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            StateInstance that = (StateInstance) o;

            if( !properties.equals( that.properties ) )
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return properties.hashCode();
        }
    }
}