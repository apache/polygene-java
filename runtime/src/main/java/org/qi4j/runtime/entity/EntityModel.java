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

import org.qi4j.api.common.*;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.*;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.bootstrap.ManyAssociationDeclarations;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.entity.association.EntityAssociationsModel;
import org.qi4j.runtime.entity.association.EntityManyAssociationsModel;
import org.qi4j.runtime.property.PersistentPropertyModel;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JAVADOC
 */
public final class EntityModel
    extends AbstractCompositeModel
    implements EntityDescriptor
{
    private static final Method CREATE_METHOD;
    private static final Method IDENTITY_METHOD;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
            CREATE_METHOD = Lifecycle.class.getMethod( "create" );
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
                                        List<Class<?>> mixins )
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
        stateModel.addStateFor( compositeMethodsModel.methods() );

        return new EntityModel( type,
                                visibility,
                                metaInfo,
                                mixinsModel,
                                stateModel,
                                compositeMethodsModel );
    }

    private String rdf;
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

        RDF rdfAnnotation = type.getAnnotation( RDF.class );
        this.rdf = rdfAnnotation == null ? null : rdfAnnotation.value();

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
        Set<String> mixinTypes = new LinkedHashSet<String>();
        for( Class mixinType : mixinsModel.mixinTypes() )
        {
            mixinTypes.add( mixinType.getName() );
        }

        EntityStateModel entityStateModel = (EntityStateModel) stateModel;
        entityType = new EntityType(
            TypeName.nameOf( type() ), rdf, queryable,
            mixinTypes, entityStateModel.propertyTypes(), entityStateModel.associationTypes(), entityStateModel.manyAssociationTypes()
        );

        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null );
        compositeMethodsModel.bind( resolution );
        mixinsModel.bind( resolution );
        stateModel.bind( resolution );
    }

    public EntityInstance newInstance( ModuleUnitOfWork uow, ModuleInstance moduleInstance, EntityReference identity, EntityState state )
    {
        EntityInstance instance = new EntityInstance( uow, moduleInstance, this, identity, state );
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


    public Object newMixin( Object[] mixins, EntityStateModel.EntityStateInstance entityState, EntityInstance entityInstance, Method method )
    {
        return ( (EntityMixinsModel) mixinsModel ).newMixin( entityInstance, entityState, mixins, method );
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

    public <T> T newProxy( EntityInstance entityInstance, Class<T> mixinType )
    {
        // Instantiate proxy for given mixin interface
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, entityInstance ) );
    }

    public EntityState newEntityState( EntityStoreUnitOfWork store, EntityReference identity, Qi4jSPI qi4jSPI )
        throws ConstraintViolationException, EntityStoreException
    {
        try
        {
            // New EntityState
            EntityState entityState = store.newEntityState( identity );

            // Add EntityType
            addEntityType( entityState, qi4jSPI );

            // Set identity property
            PropertyTypeDescriptor propertyDescriptor = state().getPropertyByQualifiedName( QualifiedName.fromMethod( IDENTITY_METHOD ) );
            entityState.setProperty( propertyDescriptor.propertyType().stateName(), '\"' + identity.identity() + '\"' );

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

    @Override public String toString()
    {
        return type().getName();
    }

    public void addEntityType( EntityState entityState, Qi4jSPI qi4jSPI )
    {
        entityState.addEntityTypeReference( entityType().reference() );

        {
            // Set new properties to default value
            Set<PersistentPropertyModel> entityProperties = state().properties();
            for( PersistentPropertyModel propertyDescriptor : entityProperties )
            {
                String stringValue = propertyDescriptor.toJSON( propertyDescriptor.initialValue(), qi4jSPI );
                entityState.setProperty( propertyDescriptor.propertyType().stateName(), stringValue );
            }
        }

        {
            // Set new manyAssociations to null
            Set<AssociationDescriptor> entityAssociations = state().associations();
            for( AssociationDescriptor associationDescriptor : entityAssociations )
            {
                entityState.setAssociation( associationDescriptor.associationType().stateName(), null );
            }
        }

        {
            // Set new many-manyAssociations to empty
            Set<ManyAssociationDescriptor> entityAssociations = state().manyAssociations();
            for( ManyAssociationDescriptor associationDescriptor : entityAssociations )
            {
                entityState.getManyAssociation( associationDescriptor.manyAssociationType().stateName() );
            }
        }
    }

    void removeEntityType( EntityModel entityModel, EntityState entityState )
    {
        // Remove type but keep data
        entityState.removeEntityTypeReference( entityModel.entityType().reference() );
    }

    boolean hasEntityType( EntityModel entityModel, EntityState entityState )
    {
        return entityState.hasEntityTypeReference( entityModel.entityType().reference() );
    }

    public void invokeCreate( EntityInstance instance )
    {
        // Invoke lifecycle create() method
        if( hasMixinType( Lifecycle.class ) )
        {
            try
            {
                instance.invoke( null, CREATE_METHOD, new Object[0] );
            }
            catch( LifecycleException throwable )
            {
                throw throwable;
            }
            catch( Throwable throwable )
            {
                throw new LifecycleException( throwable );
            }
        }

    }
}
