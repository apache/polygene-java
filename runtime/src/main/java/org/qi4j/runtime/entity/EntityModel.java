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

package org.qi4j.runtime.entity;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.bootstrap.ManyAssociationDeclarations;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.bootstrap.AssemblyHelper;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.entity.association.AssociationsModel;
import org.qi4j.runtime.entity.association.ManyAssociationsModel;
import org.qi4j.runtime.property.PersistentPropertyModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.EntityAlreadyExistsException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.property.PersistentPropertyDescriptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * JAVADOC
 */
public final class EntityModel
    extends AbstractCompositeModel
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
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: ModuleUnitOfWork" );
        }
    }

    public static EntityModel newModel( Class<? extends EntityComposite> type,
                                        Visibility visibility,
                                        MetaInfo metaInfo,
                                        PropertyDeclarations propertyDecs,
                                        AssociationDeclarations associationDecs,
                                        ManyAssociationDeclarations manyAssociationDecs,
                                        ConcernsDeclaration concernsDeclaration,
                                        Iterable<Class<?>> sideEffects,
                                        List<Class<?>> mixins,
                                        List<Class<?>> roles, AssemblyHelper helper
    )
    {
        ConstraintsModel constraintsModel = new ConstraintsModel( type );
        boolean immutable = metaInfo.get( Immutable.class ) != null;
        EntityPropertiesModel entityPropertiesModel = new EntityPropertiesModel( constraintsModel, propertyDecs, immutable );
        AssociationsModel associationsModel = new AssociationsModel( constraintsModel, associationDecs );
        ManyAssociationsModel manyAssociationsModel = new ManyAssociationsModel( constraintsModel, manyAssociationDecs );
        EntityStateModel stateModel = new EntityStateModel( entityPropertiesModel, associationsModel, manyAssociationsModel );
        EntityMixinsModel mixinsModel = new EntityMixinsModel( type, roles, mixins );
        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( type, sideEffects );
        CompositeMethodsModel compositeMethodsModel = new CompositeMethodsModel( type,
                                                                                 constraintsModel,
                                                                                 concernsDeclaration,
                                                                                 sideEffectsModel,
                                                                                 mixinsModel, helper );
        stateModel.addStateFor( compositeMethodsModel.methods(), mixinsModel );

        return new EntityModel( type,
                                roles,
                                visibility,
                                metaInfo,
                                mixinsModel,
                                stateModel,
                                compositeMethodsModel );
    }

    private final boolean queryable;

    private EntityModel( Class<? extends EntityComposite> type,
                         List<Class<?>> roles,
                         Visibility visibility,
                         MetaInfo info,
                         EntityMixinsModel mixinsModel,
                         EntityStateModel stateModel,
                         CompositeMethodsModel compositeMethodsModel
    )
    {
        super( type, roles, visibility, info, mixinsModel, stateModel, compositeMethodsModel );

        final Queryable queryable = Classes.getAnnotationOfTypeOrAnyOfSuperTypes( type, Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    public Class<? extends EntityComposite> type()
    {
        return (Class<? extends EntityComposite>) super.type();
    }

    public boolean queryable()
    {
        return queryable;
    }

    @Override
    public EntityStateModel state()
    {
        return (EntityStateModel) super.state();
    }

    public boolean hasRole( Class roleType )
    {
        return roleType.isAssignableFrom( proxyClass );
    }

    public EntityInstance newInstance( ModuleUnitOfWork uow, ModuleInstance moduleInstance, EntityState state )
    {
        EntityInstance instance = new EntityInstance( uow, moduleInstance, this, state );
        return instance;
    }

    public Object[] newMixinHolder()
    {
        return mixinsModel.newMixinHolder();
    }

    public EntityStateModel.EntityStateInstance newStateHolder( ModuleUnitOfWork uow, EntityState entityState )
    {
        return ( (EntityStateModel) stateModel ).newInstance( uow, entityState );
    }

    public Object newMixin( Object[] mixins,
                            EntityStateModel.EntityStateInstance entityState,
                            EntityInstance entityInstance,
                            Method method
    )
    {
        return ( (EntityMixinsModel) mixinsModel ).newMixin( entityInstance, entityState, mixins, method );
    }

    public EntityComposite newProxy( InvocationHandler invocationHandler )
    {
        // Instantiate proxy for given composite interface
        try
        {
            return EntityComposite.class.cast( proxyClass.getConstructor( InvocationHandler.class )
                                                   .newInstance( invocationHandler ) );
        }
        catch( Exception e )
        {
            throw new ConstructionException( e );
        }
    }

    public EntityState newEntityState( EntityStoreUnitOfWork store, EntityReference identity )
        throws ConstraintViolationException, EntityStoreException
    {
        try
        {
            // New EntityState
            EntityState entityState = store.newEntityState( identity, this );

            // Set identity property
            PersistentPropertyDescriptor persistentPropertyDescriptor = state().getPropertyByQualifiedName( QualifiedName.fromAccessor( IDENTITY_METHOD ) );
            entityState.setProperty( persistentPropertyDescriptor.qualifiedName(), identity.identity() );

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

    @Override
    public String toString()
    {
        return type().getName();
    }

    public void initState( ModuleInstance module, EntityState entityState )
    {
        {
            // Set new properties to default value
            Set<PersistentPropertyModel> entityProperties = state().properties();
            for( PersistentPropertyModel propertyDescriptor : entityProperties )
            {
                entityState.setProperty( propertyDescriptor.qualifiedName(), propertyDescriptor.initialValue( module ) );
            }
        }

        {
            // Set new manyAssociations to null
            Set<AssociationDescriptor> entityAssociations = state().associations();
            for( AssociationDescriptor associationDescriptor : entityAssociations )
            {
                entityState.setAssociation( associationDescriptor.qualifiedName(), null );
            }
        }

        {
            // Set new many-manyAssociations to empty
            Set<ManyAssociationDescriptor> entityAssociations = state().manyAssociations();
            for( ManyAssociationDescriptor associationDescriptor : entityAssociations )
            {
                entityState.getManyAssociation( associationDescriptor.qualifiedName() );
            }
        }
    }

    public void invokeLifecycle( boolean create, Object[] mixins, CompositeInstance instance, StateHolder state )
    {
        ( (EntityMixinsModel) mixinsModel ).invokeLifecycle( create, mixins, instance, state );
    }
}
