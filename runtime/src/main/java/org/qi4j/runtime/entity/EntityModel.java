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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.ConcernsDeclaration;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.MixinsInstance;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.composite.SideEffectsDeclaration;
import org.qi4j.runtime.composite.AbstractCompositeModel;
import org.qi4j.runtime.composite.AbstractStateModel;
import org.qi4j.runtime.entity.association.EntityAssociationsModel;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateDescriptor;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.UnknownEntityTypeException;

/**
 * JAVADOC
 */
public final class EntityModel
    extends AbstractCompositeModel
    implements EntityDescriptor
{
    public static EntityModel newModel( Class<? extends EntityComposite> type,
                                        Visibility visibility,
                                        MetaInfo metaInfo,
                                        PropertyDeclarations propertyDecs,
                                        AssociationDeclarations associationDecs,
                                        Iterable<Class<?>> concerns,
                                        Iterable<Class<?>> sideEffects,
                                        List<Class<?>> mixins )
    {
        ConstraintsModel constraintsModel = new ConstraintsModel( type );
        boolean immutable = metaInfo.get( Immutable.class ) != null;
        EntityPropertiesModel entityPropertiesModel = new EntityPropertiesModel( constraintsModel, propertyDecs, immutable );
        EntityAssociationsModel associationsModel = new EntityAssociationsModel( constraintsModel, associationDecs );
        EntityStateModel stateModel = new EntityStateModel( entityPropertiesModel, associationsModel );
        EntityMixinsModel mixinsModel = new EntityMixinsModel( type, mixins );
        ConcernsDeclaration concernsDeclaration = new ConcernsDeclaration( type, concerns );
        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( type, sideEffects );
        CompositeMethodsModel compositeMethodsModel = new CompositeMethodsModel( type,
                                                                                 constraintsModel,
                                                                                 concernsDeclaration,
                                                                                 sideEffectsModel,
                                                                                 mixinsModel );
        stateModel.addStateFor( compositeMethodsModel.methods() );

        return new EntityModel( type,
                                visibility,
                                metaInfo,
                                mixinsModel,
                                stateModel,
                                compositeMethodsModel );
    }

    private final String uri;
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
        super(type, visibility, info, mixinsModel, stateModel, compositeMethodsModel);

        this.uri = Classes.toURI(type);

        final Queryable queryable = type.getAnnotation( Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    public Class<? extends EntityComposite> type()
    {
        return (Class<? extends EntityComposite>) super.type();
    }

    @Override public EntityStateModel state()
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

    public void bind( Resolution resolution ) throws BindingException
    {
        List<String> mixinTypes = new ArrayList<String>();
        for( Class mixinType : mixinsModel.mixinTypes() )
        {
            mixinTypes.add( mixinType.getName() );
        }

        EntityStateModel entityStateModel = (EntityStateModel) stateModel;
        entityType = new EntityType(
            type().getName(), toURI(), queryable,
            mixinTypes, entityStateModel.propertyTypes(), entityStateModel.associationTypes(), entityStateModel.manyAssociationTypes()
        );

        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null );
        compositeMethodsModel.bind( resolution );
        mixinsModel.bind( resolution );
    }

    public QualifiedIdentity newQualifiedIdentity( String identity )
    {
        return new QualifiedIdentity( identity, type() );
    }

    public EntityInstance getInstance( UnitOfWorkInstance unitOfWorkInstance, EntityStore store, QualifiedIdentity qid, ModuleInstance moduleInstance )
    {
        return loadInstance( unitOfWorkInstance, store, qid, moduleInstance, null );
    }

    public EntityInstance loadInstance( UnitOfWorkInstance uow, EntityStore entityStore, QualifiedIdentity identity, ModuleInstance moduleInstance, EntityState state )
    {
        EntityInstance instance = new EntityInstance( uow, entityStore, this, moduleInstance, identity, EntityStatus.LOADED, state );
        return instance;
    }

    public Object[] initialize( UnitOfWorkInstance uow, EntityState entityState, EntityInstance entityInstance )
    {
        Object[] mixins = mixinsModel.newMixinHolder();
        entityInstance.setMixins( mixins );
        EntityStateModel.EntityStateInstance state = ((EntityStateModel)stateModel).newInstance( uow, entityState );
        entityInstance.setEntityState( state );
//        mixinsModel.newMixins( entityInstance, state, mixins );
        return mixins;
    }


    public Object newMixin( Object[] mixins, EntityStateModel.EntityStateInstance entityState, EntityInstance entityInstance, Method method )
    {
        return ((EntityMixinsModel)mixinsModel).newMixin( entityInstance, entityState, mixins, method );
    }

    public EntityComposite newProxy( EntityInstance entityInstance )
    {
        // Instantiate proxy for given composite interface
        try
        {
            return EntityComposite.class.cast( proxyClass.getConstructor( InvocationHandler.class ).newInstance( entityInstance ) );
        }
        catch( Exception e )
        {
            throw new ConstructionException( e );
        }
    }

    public EntityStateHolder newBuilderState()
    {
        return ((EntityStateModel)stateModel).newBuilderInstance();
    }

    private Class<? extends Composite> createProxyClass( Class<? extends Composite> compositeType )
    {
        ClassLoader proxyClassloader = compositeType.getClassLoader();
        Class<?>[] interfaces = new Class<?>[]{ compositeType };
        return (Class<? extends Composite>) Proxy.getProxyClass( proxyClassloader, interfaces );
    }

    public EntityState newEntityState( EntityStore store, String identity, StateHolder state )
        throws ConstraintViolationException, EntityStoreException
    {
        QualifiedIdentity qid = newQualifiedIdentity( identity );
        try
        {
            EntityState entityState = null;
            do
            {
                try
                {
                    entityState = store.newEntityState( qid );
                }
                catch( UnknownEntityTypeException e )
                {
                    // Check if it is this type that the store doesn't understand
                    EntityType entityType = entityType();
                    if( e.getMessage().equals( entityType.type() ) )
                    {
                        store.registerEntityType( entityType );
                        // Try again
                    }
                    else
                    {
                        throw e; // ???
                    }
                }
            }
            while( entityState == null );

            ((EntityStateModel)stateModel).setState( state, entityState );
            return entityState;
        }
        catch( EntityAlreadyExistsException e )
        {
            throw new EntityCompositeAlreadyExistsException( identity, qid.type() );
        }
        catch( EntityStoreException e )
        {
            throw new ConstructionException( "Could not create new entity in store", e );
        }
    }

    public EntityState getEntityState( EntityStore entityStore, QualifiedIdentity qid )
        throws EntityStoreException
    {
        EntityState entityState = null;
        do
        {
            try
            {
                entityState = entityStore.getEntityState( qid );
            }
            catch( UnknownEntityTypeException e )
            {
                // Check if it is this type that the store doesn't understand
                EntityType entityType = entityType();
                if( e.getMessage().equals( entityType.type() ) )
                {
                    entityStore.registerEntityType( entityType );
                    // Try again
                }
                else
                {
                    throw e; // ???
                }
            }
        }
        while( entityState == null );

        return entityState;
    }

    public String toURI()
    {
        return uri;
    }

    @Override public String toString()
    {
        return type().getName();
    }
}
