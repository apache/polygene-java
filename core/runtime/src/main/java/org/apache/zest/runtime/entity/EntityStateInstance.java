/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.runtime.entity;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.util.Classes;
import org.apache.zest.functional.Function;
import org.apache.zest.functional.Function2;
import org.apache.zest.functional.Iterables;
import org.apache.zest.runtime.association.AssociationInstance;
import org.apache.zest.runtime.association.AssociationModel;
import org.apache.zest.runtime.association.ManyAssociationInstance;
import org.apache.zest.runtime.association.ManyAssociationModel;
import org.apache.zest.runtime.association.NamedAssociationInstance;
import org.apache.zest.runtime.association.NamedAssociationModel;
import org.apache.zest.runtime.composite.ConstraintsCheck;
import org.apache.zest.runtime.property.PropertyModel;
import org.apache.zest.runtime.unitofwork.BuilderEntityState;
import org.apache.zest.spi.entity.EntityState;

/**
 * TODO
 */
public final class EntityStateInstance
    implements AssociationStateHolder
{
    private Map<AccessibleObject, Object> state;

    private final EntityStateModel stateModel;
    private EntityState entityState;
    private final Function2<EntityReference, Type, Object> entityFunction;

    public EntityStateInstance( EntityStateModel stateModel, final UnitOfWork uow, EntityState entityState )
    {
        this.stateModel = stateModel;
        this.entityState = entityState;

        entityFunction = new Function2<EntityReference, Type, Object>()
        {
            @Override
            public Object map( EntityReference entityReference, Type type )
            {
                return uow.get( Classes.RAW_CLASS.map( type ), entityReference.identity() );
            }
        };
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Property<T> propertyFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        Map<AccessibleObject, Object> state = state();

        Property<T> property = (Property<T>) state.get( accessor );

        if( property == null )
        {
            PropertyModel entityPropertyModel = stateModel.propertyModelFor( accessor );
            property = new EntityPropertyInstance<>(
                entityState instanceof BuilderEntityState
                ? entityPropertyModel.getBuilderInfo()
                : entityPropertyModel,
                entityState );
            state.put( accessor, property );
        }

        return property;
    }

    @Override
    public Iterable<Property<?>> properties()
    {
        return Iterables.map( new Function<PropertyDescriptor, Property<?>>()
        {
            @Override
            public Property<?> map( PropertyDescriptor propertyDescriptor )
            {
                return propertyFor( propertyDescriptor.accessor() );
            }
        }, stateModel.properties() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Association<T> associationFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        Map<AccessibleObject, Object> state = state();
        Association<T> association = (Association<T>) state.get( accessor );

        if( association == null )
        {
            final AssociationModel associationModel = stateModel.getAssociation( accessor );
            association = new AssociationInstance<>(
                entityState instanceof BuilderEntityState
                ? associationModel.getBuilderInfo()
                : associationModel,
                entityFunction,
                new Property<EntityReference>()
            {
                @Override
                public EntityReference get()
                {
                    return entityState.associationValueOf( associationModel.qualifiedName() );
                }

                @Override
                public void set( EntityReference newValue )
                    throws IllegalArgumentException, IllegalStateException
                {
                    entityState.setAssociationValue( associationModel.qualifiedName(), newValue );
                }
            } );
            state.put( accessor, association );
        }

        return association;
    }

    @Override
    public Iterable<Association<?>> allAssociations()
    {
        return Iterables.map( new Function<AssociationDescriptor, Association<?>>()
        {
            @Override
            public Association<?> map( AssociationDescriptor associationDescriptor )
            {
                return associationFor( associationDescriptor.accessor() );
            }
        }, stateModel.associations() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> ManyAssociation<T> manyAssociationFor( AccessibleObject accessor )
    {
        Map<AccessibleObject, Object> state = state();

        ManyAssociation<T> manyAssociation = (ManyAssociation<T>) state.get( accessor );

        if( manyAssociation == null )
        {
            ManyAssociationModel associationModel = stateModel.getManyAssociation( accessor );
            manyAssociation = new ManyAssociationInstance<>(
                entityState instanceof BuilderEntityState
                ? associationModel.getBuilderInfo()
                : associationModel,
                entityFunction,
                entityState.manyAssociationValueOf( associationModel.qualifiedName() ) );
            state.put( accessor, manyAssociation );
        }

        return manyAssociation;
    }

    @Override
    public Iterable<ManyAssociation<?>> allManyAssociations()
    {
        return Iterables.map( new Function<AssociationDescriptor, ManyAssociation<?>>()
        {
            @Override
            public ManyAssociation<?> map( AssociationDescriptor associationDescriptor )
            {
                return manyAssociationFor( associationDescriptor.accessor() );
            }
        }, stateModel.manyAssociations() );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> NamedAssociation<T> namedAssociationFor( AccessibleObject accessor )
    {
        Map<AccessibleObject, Object> state = state();

        NamedAssociation<T> namedAssociation = (NamedAssociation<T>) state.get( accessor );

        if( namedAssociation == null )
        {
            NamedAssociationModel associationModel = stateModel.getNamedAssociation( accessor );
            namedAssociation = new NamedAssociationInstance<>(
                entityState instanceof BuilderEntityState
                ? associationModel.getBuilderInfo()
                : associationModel,
                entityFunction,
                entityState.namedAssociationValueOf( associationModel.qualifiedName() ) );
            state.put( accessor, namedAssociation );
        }

        return namedAssociation;
    }

    @Override
    public Iterable<? extends NamedAssociation<?>> allNamedAssociations()
    {
        return Iterables.map( new Function<AssociationDescriptor, NamedAssociation<?>>()
        {
            @Override
            public NamedAssociation<?> map( AssociationDescriptor associationDescriptor )
            {
                return namedAssociationFor( associationDescriptor.accessor() );
            }
        }, stateModel.namedAssociations() );
    }

    public void checkConstraints()
    {
        for( PropertyDescriptor propertyDescriptor : stateModel.properties() )
        {
            ConstraintsCheck constraints = (ConstraintsCheck) propertyDescriptor;
            Property<Object> property = this.propertyFor( propertyDescriptor.accessor() );
            constraints.checkConstraints( property.get() );
        }

        for( AssociationDescriptor associationDescriptor : stateModel.associations() )
        {
            ConstraintsCheck constraints = (ConstraintsCheck) associationDescriptor;
            Association<Object> association = this.associationFor( associationDescriptor.accessor() );
            constraints.checkConstraints( association.get() );
        }

        // TODO Should ManyAssociations and NamedAssociations be checked too?
    }

    private Map<AccessibleObject, Object> state()
    {
        if( state == null )
        {
            state = new HashMap<>();
        }

        return state;
    }
}