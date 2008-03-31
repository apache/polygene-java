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
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Identity;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StoreException;

public final class EntityCompositeInstance
    extends AbstractCompositeInstance
    implements CompositeInstance
{
    private Object[] mixins;

    private UnitOfWork unitOfWork;
    private String identity;
    private EntityState state;
    private EntityStore store;

    public EntityCompositeInstance( UnitOfWork unitOfWork, CompositeContext aContext, ModuleInstance moduleInstance, EntityStore store, String identity )
    {
        super( aContext, moduleInstance );
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
            CompositeBinding binding = context.getCompositeBinding();
            EntityState entityState = store.getEntityState( unitOfWork, identity, binding );
            context.newEntityMixins( moduleInstance, this, entityState );
        }

        Object mixin = mixins[ descriptor.getMixinIndex() ];

        if( mixin == null )
        {
            throw new InvalidCompositeException( "Implementation missing for method " + method.getName() + "() ",
                                                 context.getCompositeModel().getCompositeType() );
        }
        // Invoke
        CompositeMethodInstance compositeMethodInstance = context.getMethodInstance( descriptor, moduleInstance );
        return compositeMethodInstance.invoke( composite, args, mixin );
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
        throws StoreException
    {
        if( state == null )
        {
            CompositeBinding binding = context.getCompositeBinding();
            state = store.getEntityState( unitOfWork, identity, binding );
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


    @Override public String toString()
    {
        return context.getCompositeModel().getCompositeType().getName() + ":" + identity;
    }
}
