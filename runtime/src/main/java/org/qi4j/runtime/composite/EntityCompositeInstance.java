/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.State;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Identity;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.property.Property;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.runtime.entity.association.AssociationInstance;
import org.qi4j.runtime.entity.association.ListAssociationInstance;
import org.qi4j.runtime.entity.association.SetAssociationInstance;
import org.qi4j.runtime.property.EntityPropertyInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationModel;

public final class EntityCompositeInstance
    extends AbstractCompositeInstance
    implements CompositeInstance, State
{
    private Object[] mixins;

    private UnitOfWorkInstance unitOfWork;
    private String identity;
    private EntityState state;
    private EntityStore store;

    private Map<Method, Property> properties;
    private Map<Method, AbstractAssociation> associations;

    public EntityCompositeInstance( UnitOfWorkInstance unitOfWork, CompositeContext aContext, EntityStore store, String identity )
    {
        super( aContext );
        this.unitOfWork = unitOfWork;
        this.store = store;
        this.identity = identity;
    }

    public static <T extends EntityComposite> EntityCompositeInstance getEntityCompositeInstance( T aProxy )
    {
        return (EntityCompositeInstance) Proxy.getInvocationHandler( aProxy );
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        MethodDescriptor descriptor = context.getMethodDescriptor( method );
        if( descriptor == null )
        {
            return invokeObject( composite, method, args );
        }

        if( mixins == null ) // Check if this is a lazy-loaded reference
        {
            EntityState entityState = loadState();
            context.newEntityMixins( unitOfWork, this, entityState );
        }

        Object mixin = mixins[ descriptor.getMixinIndex() ];

        if( mixin == null )
        {
            throw new InvalidCompositeException( "Implementation missing for method " + method.getName() + "() ",
                                                 context.getCompositeModel().getCompositeType() );
        }
        // Invoke
        CompositeMethodInstance compositeMethodInstance = context.getMethodInstance( descriptor, unitOfWork.getModuleInstance() );
        return compositeMethodInstance.invoke( composite, args, mixin );
    }

    // State implementation -----------------------------------------
    public Property getProperty( Method propertyMethod )
    {
        if( properties == null )
        {
            properties = new HashMap<Method, Property>();
        }

        Property property = properties.get( propertyMethod );
        if( property == null )
        {
            property = context.getPropertyContext( propertyMethod ).newEntityInstance( unitOfWork.getModuleInstance(), loadState() );
            properties.put( propertyMethod, property );
        }
        return property;
    }

    public AbstractAssociation getAssociation( Method associationMethod )
    {
        if( associations == null )
        {
            associations = new HashMap<Method, AbstractAssociation>();
        }

        AbstractAssociation association = associations.get( associationMethod );
        if( association == null )
        {
            if( Association.class.isAssignableFrom( associationMethod.getReturnType() ) )
            {
                association = context.getAssociationContext( associationMethod ).newInstance( unitOfWork, state );
                associations.put( associationMethod, association );
            }
            else
            {
                String qualifiedName = ComputedPropertyInstance.getQualifiedName( associationMethod );
                Collection<QualifiedIdentity> associationCollection = state.getManyAssociation( qualifiedName );

                if( associationCollection == null )
                {
                    if( ListAssociation.class.isAssignableFrom( associationMethod.getReturnType() ) )
                    {
                        associationCollection = new ArrayList<QualifiedIdentity>();
                        associationCollection = state.setManyAssociation( qualifiedName, associationCollection );
                    }
                    else
                    {
                        associationCollection = new HashSet<QualifiedIdentity>();
                        associationCollection = state.setManyAssociation( qualifiedName, associationCollection );
                    }
                }

                association = context.getAssociationContext( associationMethod ).newInstance( unitOfWork, associationCollection );
                associations.put( associationMethod, association );
            }
        }
        return association;
    }

    public void refresh( EntityState newState )
    {
        state = newState;

        // Reset values
        if( properties != null )
        {
            for( Property property : properties.values() )
            {
                if( property instanceof EntityPropertyInstance )
                {
                    ( (EntityPropertyInstance) property ).refresh( state );
                }
            }
        }

        if( associations != null )
        {
            for( Map.Entry<Method, AbstractAssociation> methodAbstractAssociationEntry : associations.entrySet() )
            {
                AbstractAssociation abstractAssociation = methodAbstractAssociationEntry.getValue();

                if( abstractAssociation instanceof AssociationInstance )
                {
                    ( (AssociationInstance) abstractAssociation ).refresh( state );
                }
                else if( abstractAssociation instanceof ListAssociationInstance )
                {
                    ( (ListAssociationInstance) abstractAssociation ).refresh( (List<QualifiedIdentity>) state.getManyAssociation( AssociationModel.getQualifiedName( methodAbstractAssociationEntry.getKey() ) ) );
                }
                else if( abstractAssociation instanceof SetAssociationInstance )
                {
                    ( (SetAssociationInstance) abstractAssociation ).refresh( (Set<QualifiedIdentity>) state.getManyAssociation( AssociationModel.getQualifiedName( methodAbstractAssociationEntry.getKey() ) ) );
                }
            }
        }
    }


    public void setMixins( Object[] mixins )
    {
        this.mixins = mixins;
    }

    public Object[] getMixins()
    {
        return mixins;
    }

    public void setState( EntityState state )
    {
        this.state = state;
    }

    public EntityState getState()
    {
        return state;
    }

    public EntityState loadState()
        throws EntityStoreException
    {
        if( state == null )
        {
            state = store.getEntityState( context.getCompositeResolution().getCompositeDescriptor(), new QualifiedIdentity( identity, context.getCompositeModel().getCompositeType().getName() ) );
        }

        return state;
    }

    public UnitOfWork getUnitOfWork()
    {
        return unitOfWork;
    }

    public EntityStore getStore()
    {
        return store;
    }

    public String getIdentity()
    {
        return identity;
    }

    public boolean isReference()
    {
        return mixins == null;
    }

    protected Object onHashCode( Object proxy )
    {
        return identity.hashCode();
    }

    protected Object onEquals( Object proxy, Object[] args )
    {
        Identity other = ( (Identity) args[ 0 ] );
        return identity.equals( other.identity().get() );
    }

    protected Object onToString( Object proxy )
        throws Throwable
    {
        return identity;
    }

    public String toURI()
    {
        // TODO: Shall the URI contain the type ("entity"), or is it always understood in a larger context??
        return "urn:qi4j:entity:" + identity;
    }

    @Override public String toString()
    {
        return context.getCompositeModel().getCompositeType().getName() + ":" + identity;
    }
}
