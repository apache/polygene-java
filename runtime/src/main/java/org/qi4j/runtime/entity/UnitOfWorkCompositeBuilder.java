/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.InstantiationException;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeAlreadyExistsException;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.Lifecycle;
import org.qi4j.entity.UnitOfWorkException;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.runtime.entity.association.AssociationContext;
import org.qi4j.runtime.entity.association.ListAssociationInstance;
import org.qi4j.runtime.entity.association.SetAssociationInstance;
import org.qi4j.runtime.structure.CompositeBuilderImpl;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationBinding;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;

/**
 * TODO
 */
public final class UnitOfWorkCompositeBuilder<T>
    extends CompositeBuilderImpl<T>
{
    private static final Method IDENTITY_METHOD;
    private UnitOfWorkInstance uow;
    private EntityStore store;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: UnitOfWorkCompositeBuilder" );
        }
    }

    public UnitOfWorkCompositeBuilder( ModuleInstance moduleInstance, CompositeContext compositeContext, UnitOfWorkInstance uow, EntityStore store )
    {
        super( moduleInstance, compositeContext );
        this.uow = uow;
        this.store = store;
    }

    public CompositeBuilder<T> use( Object... usedObjects )
    {
        throw new ObjectAccessException( "Entities may not use other objects" );
    }

    public T newInstance()
    {
        // Figure out whether to use given or generated identity
        boolean prototypePattern = false;
        Property<String> identityProperty = getProperties().get( IDENTITY_METHOD );
        String identity;
        if( identityProperty == null )
        {
            Class compositeType = context.getCompositeModel().getCompositeType();
            IdentityGenerator identityGenerator = uow.stateServices.getIdentityGenerator( compositeType );
            if( identityGenerator == null )
            {
                throw new UnitOfWorkException( "No identity generator found for type " + compositeType.getName() );
            }
            identity = identityGenerator.generate( compositeType );
            identityProperty = context.getPropertyContext( IDENTITY_METHOD ).newInstance( moduleInstance, identity );
            prototypePattern = true;
            propertyValues.put( IDENTITY_METHOD, identityProperty );
        }
        else
        {
            identity = identityProperty.get();
        }

        // Create state holder for this entity
        EntityState state;
        Class<? extends Composite> entityType = context.getCompositeModel().getCompositeType();
        try
        {
            state = store.newEntityState( context.getCompositeResolution().getCompositeDescriptor(), new QualifiedIdentity( identity, entityType.getName() ) );
        }
        catch( EntityAlreadyExistsException e )
        {
            throw new EntityCompositeAlreadyExistsException( identity, entityType );
        }
        catch( EntityStoreException e )
        {
            throw new InstantiationException( "Could not create new entity in store", e );
        }

        // Populate state
        Map<Method, Property> propertyValues = getProperties();
        Iterable<PropertyBinding> propertyBindings = context.getCompositeBinding().getPropertyBindings();
        for( PropertyBinding propertyBinding : propertyBindings )
        {
            PropertyModel propertyModel = propertyBinding.getPropertyResolution().getPropertyModel();
            Method accessor = propertyModel.getAccessor();
            if( propertyValues.containsKey( accessor ) )
            {
                Property propertyValue = propertyValues.get( accessor );
                state.setProperty( propertyModel.getQualifiedName(), propertyValue.get() );
            }
            else
            {
                state.setProperty( propertyModel.getQualifiedName(), propertyBinding.getDefaultValue() );
            }
        }


        Map<Method, AbstractAssociation> associationValues = getAssociations();
        Iterable<AssociationBinding> associationBindings = context.getCompositeBinding().getAssociationBindings();
        //    Map<String, EntityId> entityAssociations = state.getAssociations();
        //    Map<String, Collection<EntityId>> entityManyAssociations = state.getManyAssociations();
        for( AssociationBinding associationBinding : associationBindings )
        {
            AssociationModel associationModel = associationBinding.getAssociationResolution().getAssociationModel();
            Method accessor = associationModel.getAccessor();
            if( associationValues.containsKey( accessor ) )
            {
                AbstractAssociation associationValue = associationValues.get( accessor );
                if( associationValue instanceof Association )
                {
                    Association<EntityComposite> association = (Association<EntityComposite>) associationValue;
                    QualifiedIdentity id;
                    if( association.get() == null )
                    {
                        id = QualifiedIdentity.NULL;
                    }
                    else
                    {
                        id = new QualifiedIdentity( association.get().identity().get(), association.get().type().getName() );
                    }
                    state.setAssociation( associationModel.getQualifiedName(), id );
                }
                else if( associationValue instanceof ListAssociation )
                {
                    ListAssociationInstance<EntityComposite> manyAssociation = (ListAssociationInstance<EntityComposite>) associationValue;
                    state.getManyAssociation( associationModel.getQualifiedName() ).addAll( manyAssociation.getAssociatedList() );
                }
                else if( associationValue instanceof SetAssociation )
                {
                    SetAssociationInstance<EntityComposite> manyAssociation = (SetAssociationInstance<EntityComposite>) associationValue;
                    state.getManyAssociation( associationModel.getQualifiedName() ).addAll( manyAssociation.getAssociatedSet() );
                }
            }
        }

        EntityCompositeInstance compositeInstance = context.newEntityCompositeInstance( uow, store, identity );
        context.newEntityMixins( uow, compositeInstance, state );
        T instance = compositeInterface.cast( compositeInstance.getProxy() );
        uow.createEntity( (EntityComposite) instance );

        // Invoke lifecycle create() method
        if( instance instanceof Lifecycle )
        {
            context.invokeCreate( instance, compositeInstance );
        }

        if( prototypePattern )
        {
            propertyValues.remove( IDENTITY_METHOD );
        }
        return instance;
    }

    public Iterator<T> iterator()
    {
        final Iterator<T> decoratedIterator = super.iterator();

        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                T instance = decoratedIterator.next();
                uow.createEntity( (EntityComposite) instance );
                return instance;
            }

            public void remove()
            {
            }
        };
    }

    @Override protected StateInvocationHandler newStateInvocationHandler()
    {
        return new EntityStateInvocationHandler();
    }

    private class EntityStateInvocationHandler
        extends StateInvocationHandler
    {
        public EntityStateInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( AbstractAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                AbstractAssociation association = getAssociations().get( method );
                if( association == null )
                {
                    AssociationContext associationContext = context.getMethodDescriptor( method ).getCompositeMethodContext().getAssociationContext();
                    association = associationContext.newInstance( uow, null );
                    getAssociations().put( method, association );
                }
                return association;

            }

            return super.invoke( o, method, objects );
        }

    }

}
