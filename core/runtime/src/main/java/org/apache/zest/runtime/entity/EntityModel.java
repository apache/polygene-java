/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.apache.zest.runtime.entity;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.zest.api.common.ConstructionException;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.CompositeInstance;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.entity.Queryable;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.property.StateHolder;
import org.apache.zest.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.apache.zest.api.util.Annotations;
import org.apache.zest.runtime.composite.CompositeMethodsModel;
import org.apache.zest.runtime.composite.CompositeModel;
import org.apache.zest.runtime.structure.ModuleUnitOfWork;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entitystore.EntityAlreadyExistsException;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.spi.module.ModuleSpi;

/**
 * JAVADOC
 */
public final class EntityModel extends CompositeModel
    implements EntityDescriptor
{
    private static final Method IDENTITY_METHOD;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Zest Core Runtime codebase is corrupted. Contact Zest team: ModuleUnitOfWork" );
        }
    }

    private final boolean queryable;

    public EntityModel( List<Class<?>> types,
                        Visibility visibility,
                        MetaInfo info,
                        EntityMixinsModel mixinsModel,
                        EntityStateModel stateModel,
                        CompositeMethodsModel compositeMethodsModel
    )
    {
        super( types, visibility, info, mixinsModel, stateModel, compositeMethodsModel );

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
        return new EntityInstance( uow, moduleInstance, this, state );
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

    public EntityState newEntityState( EntityStoreUnitOfWork store, EntityReference identity )
        throws ConstraintViolationException, EntityStoreException
    {
        try
        {
            // New EntityState
            EntityState entityState = store.newEntityState( identity, this );

            // Set identity property
            PropertyDescriptor persistentPropertyDescriptor = state().propertyModelFor( IDENTITY_METHOD );
            entityState.setPropertyValue( persistentPropertyDescriptor.qualifiedName(), identity.identity() );

            return entityState;
        }
        catch( EntityAlreadyExistsException e )
        {
            throw new EntityCompositeAlreadyExistsException( identity );
        }
        catch( EntityStoreException e )
        {
            throw new ConstructionException( "Could not create new entity in store", e );
        }
    }

    public void initState( ModuleSpi module, EntityState entityState )
    {
        // Set new properties to default value
        state().properties().forEach( propertyDescriptor -> {
            entityState.setPropertyValue( propertyDescriptor.qualifiedName(), propertyDescriptor.initialValue( module ) );
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
