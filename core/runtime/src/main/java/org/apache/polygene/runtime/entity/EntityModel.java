/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.runtime.entity;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.entity.Queryable;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.apache.polygene.api.util.Annotations;
import org.apache.polygene.runtime.composite.CompositeMethodsModel;
import org.apache.polygene.runtime.composite.CompositeModel;
import org.apache.polygene.runtime.unitofwork.ModuleUnitOfWork;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entitystore.EntityAlreadyExistsException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.polygene.spi.module.ModuleSpi;

import static org.apache.polygene.api.identity.HasIdentity.IDENTITY_METHOD;

/**
 * JAVADOC
 */
public final class EntityModel extends CompositeModel
    implements EntityDescriptor
{

    private final boolean queryable;

    public EntityModel( ModuleDescriptor module,
                        List<Class<?>> types,
                        Visibility visibility,
                        MetaInfo info,
                        EntityMixinsModel mixinsModel,
                        EntityStateModel stateModel,
                        CompositeMethodsModel compositeMethodsModel
    )
    {
        super( module, types, visibility, info, mixinsModel, stateModel, compositeMethodsModel );

        this.queryable = types.stream()
            .flatMap( Annotations.ANNOTATIONS_OF )
            .filter( Annotations.isType( Queryable.class ) )
            .map( annot -> ( (Queryable) annot ).value() )
            .findFirst()
            .orElse( true );
    }

    @Override
    public boolean queryable()
    {
        return queryable;
    }

    @Override
    public EntityStateModel state()
    {
        return (EntityStateModel) super.state();
    }

    public EntityInstance newInstance( ModuleUnitOfWork uow, ModuleSpi moduleInstance, EntityState state )
    {
        return new EntityInstance( uow, this, state );
    }

    public Object[] newMixinHolder()
    {
        return mixinsModel.newMixinHolder();
    }

    public Object newMixin( Object[] mixins,
                            EntityStateInstance entityState,
                            EntityInstance entityInstance,
                            Method method
    )
    {
        return ( (EntityMixinsModel) mixinsModel ).newMixin( entityInstance, entityState, mixins, method );
    }

    public EntityState newEntityState( EntityStoreUnitOfWork store, EntityReference reference )
        throws ConstraintViolationException, EntityStoreException
    {
        try
        {
            // New EntityState
            EntityState entityState = store.newEntityState( reference, this );

            // Set reference property
            PropertyDescriptor persistentPropertyDescriptor = state().propertyModelFor( IDENTITY_METHOD );
            entityState.setPropertyValue( persistentPropertyDescriptor.qualifiedName(), reference.identity() );

            return entityState;
        }
        catch( EntityAlreadyExistsException e )
        {
            throw new EntityCompositeAlreadyExistsException( reference );
        }
        catch( EntityStoreException e )
        {
            throw new ConstructionException( "Could not create new entity in store", e );
        }
    }

    public void initState( ModuleDescriptor module, EntityState entityState )
    {
        // Set new properties to default value
        state().properties().forEach( propertyDescriptor -> {
            entityState.setPropertyValue( propertyDescriptor.qualifiedName(), propertyDescriptor.resolveInitialValue(module) );
        } );

        // Set new associations to null
        state().associations().forEach( associationDescriptor -> {
            entityState.setAssociationValue( associationDescriptor.qualifiedName(), null );
        } );

        // Set new many-associations to empty
        state().manyAssociations().forEach( associationDescriptor -> {
            entityState.manyAssociationValueOf( associationDescriptor.qualifiedName() );
        } );

        // Set new named-associations to empty
        state().namedAssociations().forEach( associationDescriptor -> {
            entityState.namedAssociationValueOf( associationDescriptor.qualifiedName() );
        } );
    }

    public void invokeLifecycle( boolean create, Object[] mixins, CompositeInstance instance, StateHolder state )
    {
        ( (EntityMixinsModel) mixinsModel ).invokeLifecycle( create, mixins, instance, state );
    }
}
