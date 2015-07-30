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
import org.apache.zest.api.association.AssociationDescriptor;
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
import org.apache.zest.functional.Iterables;
import org.apache.zest.runtime.composite.CompositeMethodsModel;
import org.apache.zest.runtime.composite.CompositeModel;
import org.apache.zest.runtime.property.PropertyModel;
import org.apache.zest.runtime.structure.ModuleUnitOfWork;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entitystore.EntityAlreadyExistsException;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.spi.module.ModuleSpi;

import static org.apache.zest.functional.Iterables.filter;
import static org.apache.zest.functional.Iterables.first;
import static org.apache.zest.functional.Iterables.flattenIterables;
import static org.apache.zest.functional.Iterables.map;

/**
 * JAVADOC
 */
public final class EntityModel
    extends CompositeModel
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

    public EntityModel( Iterable<Class<?>> types,
                        Visibility visibility,
                        MetaInfo info,
                        EntityMixinsModel mixinsModel,
                        EntityStateModel stateModel,
                        CompositeMethodsModel compositeMethodsModel
    )
    {
        super( types, visibility, info, mixinsModel, stateModel, compositeMethodsModel );

        final Queryable queryable = first( Iterables.<Queryable>cast(
            filter( Annotations.isType( Queryable.class ),
                    flattenIterables( map( Annotations.ANNOTATIONS_OF, types ) ) ) ) );
        this.queryable = queryable == null || queryable.value();
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
        EntityInstance instance = new EntityInstance( uow, moduleInstance, this, state );
        return instance;
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

    public EntityState newEntityState( EntityStoreUnitOfWork store, ModuleSpi module, EntityReference identity )
        throws ConstraintViolationException, EntityStoreException
    {
        try
        {
            // New EntityState
            EntityState entityState = store.newEntityState( module, identity, this );

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
        for( PropertyModel propertyDescriptor : state().properties() )
        {
            entityState.setPropertyValue( propertyDescriptor.qualifiedName(), propertyDescriptor.initialValue( module ) );
        }

        // Set new associations to null
        for( AssociationDescriptor associationDescriptor : state().associations() )
        {
            entityState.setAssociationValue( associationDescriptor.qualifiedName(), null );
        }

        // Set new many-associations to empty
        for( AssociationDescriptor associationDescriptor : state().manyAssociations() )
        {
            entityState.manyAssociationValueOf( associationDescriptor.qualifiedName() );
        }

        // Set new named-associations to empty
        for( AssociationDescriptor associationDescriptor : state().namedAssociations() )
        {
            entityState.namedAssociationValueOf( associationDescriptor.qualifiedName() );
        }
    }

    public void invokeLifecycle( boolean create, Object[] mixins, CompositeInstance instance, StateHolder state )
    {
        ( (EntityMixinsModel) mixinsModel ).invokeLifecycle( create, mixins, instance, state );
    }
}
