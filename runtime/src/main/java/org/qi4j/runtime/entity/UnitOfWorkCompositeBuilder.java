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
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.Association;
import org.qi4j.association.ManyAssociation;
import org.qi4j.composite.Composite;
import org.qi4j.composite.InstantiationException;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.Lifecycle;
import org.qi4j.entity.UnitOfWorkException;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.runtime.structure.CompositeBuilderImpl;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;

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

    public void use( Object... usedObjects )
    {
        throw new InvalidApplicationException( "Entities may not use other objects" );
    }

    public T newInstance()
    {
        // Figure out whether to use given or generated identity
        boolean prototypePattern = false;
        Property<String> identityProperty = getProperties().get( IDENTITY_METHOD );
        String identity;
        if( identityProperty == null )
        {
            Class compositeType = context.getCompositeModel().getCompositeClass();
            IdentityGenerator identityGenerator = uow.stateServices.getIdentityGenerator( compositeType );
            if( identityGenerator == null )
            {
                throw new UnitOfWorkException( "No identity generator found for type " + compositeType.getName() );
            }
            identity = identityGenerator.generate( compositeType );
            identityProperty = new ImmutablePropertyInstance( context.getPropertyContext( Identity.class, "identity" ).getPropertyBinding(), identity );
            prototypePattern = true;
            propertyValues.put( IDENTITY_METHOD, identityProperty );
        }
        else
        {
            identity = identityProperty.get();
        }

        // Create state holder for this entity
        EntityState state;
        try
        {
            state = store.newEntityState( identity, context.getCompositeBinding() );
        }
        catch( StoreException e )
        {
            throw new InstantiationException( "Could not create new entity in store", e );
        }

        // Populate state
        Map<Method, Property> propertyValues = getProperties();
        Iterable<PropertyBinding> propertyBindings = context.getCompositeBinding().getPropertyBindings();
        for( PropertyBinding propertyBinding : propertyBindings )
        {
            Method accessor = propertyBinding.getPropertyResolution().getPropertyModel().getAccessor();
            Property propertyValue = propertyValues.get( accessor );
            if( propertyValue != null )
            {
                state.getProperty( accessor ).set( propertyValue.get() );
            }
            else
            {
                state.getProperty( accessor ).set( propertyBinding.getDefaultValue() );
            }
        }


        Map<Method, AbstractAssociation> associationValues = getAssociations();
        for( Map.Entry<Method, AbstractAssociation> association : associationValues.entrySet() )
        {
            AbstractAssociation associationValue = state.getAssociation( association.getKey() );
            if( associationValue instanceof ManyAssociation )
            {
                ManyAssociation manyAssociation = (ManyAssociation) associationValue;
                ManyAssociation newAssociation = (ManyAssociation) association;
                manyAssociation.addAll( newAssociation );
            }
            else
            {
                Association singleAssociation = (Association) associationValue;
                Association newAssociation = (Association) association;
                singleAssociation.set( newAssociation.get() );
            }
        }

        EntityCompositeInstance compositeInstance = context.newEntityCompositeInstance( moduleInstance, uow, store, identity );
        context.newEntityMixins( moduleInstance, compositeInstance, state );
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


}
