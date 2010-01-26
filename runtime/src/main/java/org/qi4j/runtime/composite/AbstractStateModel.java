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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.StateHolder;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.property.AbstractPropertiesModel;
import org.qi4j.runtime.property.AbstractPropertyModel;
import org.qi4j.runtime.property.PropertiesInstance;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;

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
        return propertiesModel.newInitialInstance();
    }

    public StateHolder newBuilderInstance()
    {
        return propertiesModel.newBuilderInstance();
    }

    public StateHolder newBuilderInstance( StateHolder state )
    {
        return propertiesModel.newBuilderInstance( state );
    }

    public StateHolder newInstance( StateHolder state )
    {
        return propertiesModel.newInstance( state );
    }

    public void addStateFor( Iterable<Method> methods, Class compositeType )
    {
        for( Method method : methods )
        {
            propertiesModel.addPropertyFor( method, compositeType );
        }
    }

    public <T extends PropertyDescriptor> T getPropertyByName( String name )
    {
        return (T) propertiesModel.getPropertyByName( name );
    }

    public <T extends PropertyDescriptor> T getPropertyByQualifiedName( QualifiedName name )
    {
        return (T) propertiesModel.getPropertyByQualifiedName( name );
    }

    public <T extends PropertyDescriptor> Set<T> properties()
    {
        return propertiesModel.properties();
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        propertiesModel.bind( resolution );
    }

    public void checkConstraints( StateHolder state )
        throws ConstraintViolationException
    {
        PropertiesInstance stateInstance = (PropertiesInstance) state;
        propertiesModel.checkConstraints( stateInstance );
    }

    public void setProperty( QualifiedName name, Object value, StateHolder valueState )
    {
        AbstractPropertyModel model = propertiesModel.getPropertyByQualifiedName( name );
        valueState.getProperty( model.accessor() ).set( value );
    }

    public void setComputedProperties( StateHolder state, CompositeInstance compositeInstance )
    {
        propertiesModel.setComputedProperties( state, compositeInstance );
    }
}