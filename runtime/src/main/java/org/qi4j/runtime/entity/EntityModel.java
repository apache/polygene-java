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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.ManyAssociationDeclarations;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.AbstractCompositeModel;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.ConcernsDeclaration;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.SideEffectsDeclaration;
import org.qi4j.runtime.entity.association.EntityAssociationsModel;
import org.qi4j.runtime.entity.association.EntityManyAssociationsModel;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.property.PersistentPropertyModel;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.EntityAlreadyExistsException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.property.PropertyTypeDescriptor;

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
                                        List<Class<?>> mixins
    )
    {
        ConstraintsModel constraintsModel = new ConstraintsModel( type );
        boolean immutable = metaInfo.get( Immutable.class ) != null;
        EntityPropertiesModel entityPropertiesModel = new EntityPropertiesModel( constraintsModel, propertyDecs, immutable );
        EntityAssociationsModel associationsModel = new EntityAssociationsModel( constraintsModel, associationDecs );
        EntityManyAssociationsModel manyAssociationsModel = new EntityManyAssociationsModel( constraintsModel, manyAssociationDecs );
        EntityStateModel stateModel = new EntityStateModel( entityPropertiesModel, associationsModel, manyAssociationsModel );
        EntityMixinsModel mixinsModel = new EntityMixinsModel( type, mixins );
        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( type, sideEffects );
        CompositeMethodsModel compositeMethodsModel = new CompositeMethodsModel( type,
                                                                                 constraintsModel,
                                                                                 concernsDeclaration,
                                                                                 sideEffectsModel,
                                                                                 mixinsModel );
        stateModel.addStateFor( compositeMethodsModel.methods(), type );

        return new EntityModel( type,
                                visibility,
                                metaInfo,
                                mixinsModel,
                                stateModel,
                                compositeMethodsModel );
    }

    private final boolean queryable;
    private EntityType entityType;

    private EntityModel( Class<? extends EntityComposite> type,
                         Visibility visibility,
                         MetaInfo info,
                         EntityMixinsModel mixinsModel,
                         EntityStateModel stateModel,
                         CompositeMethodsModel compositeMethodsModel
    )
    {
        super( type, visibility, info, mixinsModel, stateModel, compositeMethodsModel );

        final Queryable queryable = type.getAnnotation( Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    public Class<? extends EntityComposite> type()
    {
        return (Class<? extends EntityComposite>) super.type();
    }

    @Override
    public EntityStateModel state()
    {
        return (EntityStateModel) super.state();
    }

    public EntityType entityType()
    {
        return entityType;
    }

    public boolean hasMixinType( Class<?> mixinType )
    {
        return mixinsModel.hasMixinType( mixinType );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        compositeMethodsModel.visitModel( modelVisitor );
        mixinsModel.visitModel( modelVisitor );
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        Set<String> mixinTypes = new LinkedHashSet<String>();
        for( Class mixinType : mixinsModel.mixinTypes() )
        {
            mixinTypes.add( mixinType.getName() );
        }

        EntityStateModel entityStateModel = (EntityStateModel) stateModel;
        entityType = new EntityType(
            TypeName.nameOf( type() ), queryable,
            mixinTypes, entityStateModel.propertyTypes(), entityStateModel.associationTypes(), entityStateModel.manyAssociationTypes()
        );

        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null );
        compositeMethodsModel.bind( resolution );
        mixinsModel.bind( resolution );
        stateModel.bind( resolution );
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
            return EntityComposite.class.cast( proxyClass.getConstructor( InvocationHandler.class ).newInstance( invocationHandler ) );
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
            PropertyTypeDescriptor propertyDescriptor = state().getPropertyByQualifiedName( QualifiedName.fromMethod( IDENTITY_METHOD ) );
            entityState.setProperty( propertyDescriptor.propertyType().qualifiedName(), identity.identity() );

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

    public void initState( EntityState entityState )
    {
        {
            // Set new properties to default value
            Set<PersistentPropertyModel> entityProperties = state().properties();
            for( PersistentPropertyModel propertyDescriptor : entityProperties )
            {
                entityState.setProperty( propertyDescriptor.propertyType().qualifiedName(), propertyDescriptor.initialValue() );
            }
        }

        {
            // Set new manyAssociations to null
            Set<AssociationDescriptor> entityAssociations = state().associations();
            for( AssociationDescriptor associationDescriptor : entityAssociations )
            {
                entityState.setAssociation( associationDescriptor.associationType().qualifiedName(), null );
            }
        }

        {
            // Set new many-manyAssociations to empty
            Set<ManyAssociationDescriptor> entityAssociations = state().manyAssociations();
            for( ManyAssociationDescriptor associationDescriptor : entityAssociations )
            {
                entityState.getManyAssociation( associationDescriptor.manyAssociationType().qualifiedName() );
            }
        }
    }

    boolean hasEntityType( EntityModel entityModel, EntityState entityState )
    {
        return entityState.isOfType( entityModel.entityType().type() );
    }

    public void invokeLifecycle( boolean create, Object[] mixins, CompositeInstance instance, StateHolder state )
    {
        ( (EntityMixinsModel) mixinsModel ).invokeLifecycle( create, mixins, instance, state );
    }
}
