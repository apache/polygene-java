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
import org.qi4j.api.composite.StateDescriptor;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Fields;
import org.qi4j.functional.*;
import org.qi4j.runtime.property.AbstractPropertiesModel;
import org.qi4j.runtime.property.PropertiesInstance;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Base model for Composite state
 */
public abstract class AbstractStateModel<T extends AbstractPropertiesModel>
    implements StateDescriptor, VisitableHierarchy<Object, Object>
{
    protected final T propertiesModel;

    public AbstractStateModel( T propertiesModel )
    {
        this.propertiesModel = propertiesModel;
    }

    public StateHolder newInitialInstance( ModuleInstance module )
    {
        return propertiesModel.newInitialInstance( module );
    }

    public StateHolder newBuilderInstance( final ModuleInstance module )
    {
        return propertiesModel.newBuilderInstance( new Function<PropertyDescriptor, Object>()
        {
            @Override
            public Object map( PropertyDescriptor propertyDescriptor )
            {
                return propertyDescriptor.initialValue( module );
            }
        } );
    }

    public StateHolder newBuilderInstance( Function<PropertyDescriptor, Object> state )
    {
        return propertiesModel.newBuilderInstance( state );
    }

    public StateHolder newInstance( StateHolder state )
    {
        return propertiesModel.newInstance( state );
    }

    public void addStateFor( Iterable<Method> methods, AbstractMixinsModel mixins )
    {
        // Methods
        for( Method method : methods )
        {
            if (method.getParameterTypes().length == 0)
                addStateFor( method );
        }

        // Fields
        mixins.accept( new HierarchicalVisitor<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited ) throws RuntimeException
            {
                if (visited instanceof MixinModel)
                {
                    MixinModel model = (MixinModel) visited;
                    ForEach.forEach( Fields.FIELDS_OF.map( model.mixinClass() ) ).
                            filter( Annotations.hasAnnotation( State.class ) ).
                            visit( new Visitor<Field, RuntimeException>()
                            {
                                @Override
                                public boolean visit( Field visited ) throws RuntimeException
                                {
                                    addStateFor( visited );
                                    return true;
                                }
                            } );
                    return false;
                }

                return true;
            }
        } );
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

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor ) throws ThrowableType
    {
        if (visitor.visitEnter( this ))
        {
            ((VisitableHierarchy<Object, Object>)propertiesModel).accept(visitor);
        }

        return visitor.visitLeave( this );
    }

    public void checkConstraints( StateHolder state )
        throws ConstraintViolationException
    {
        PropertiesInstance stateInstance = (PropertiesInstance) state;
        propertiesModel.checkConstraints( stateInstance );
    }

    protected void addStateFor(AccessibleObject accessor)
    {
        if( Property.class.isAssignableFrom( Classes.RAW_CLASS.map( Classes.TYPE_OF.map( accessor ) )))
        {
            propertiesModel.addPropertyFor( accessor );
        }
    }
}