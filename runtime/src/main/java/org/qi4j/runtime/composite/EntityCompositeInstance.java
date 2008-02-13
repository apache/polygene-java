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
import org.qi4j.entity.EntitySession;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;

public final class EntityCompositeInstance
    extends AbstractCompositeInstance
    implements CompositeInstance
{
    private Object[] mixins;

    private EntitySession session;
    private EntityState state;
    private EntityStore store;
    private String identity;

    public EntityCompositeInstance( EntitySession session, CompositeContext aContext, ModuleInstance moduleInstance, EntityStore store, String identity )
    {
        super( aContext, moduleInstance );
        this.identity = identity;
        this.session = session;
        this.store = store;

        mixins = new Object[aContext.getCompositeResolution().getMixinCount()];
    }

    public static <T extends EntityComposite> EntityCompositeInstance getEntityCompositeInstance( T aProxy )
    {
        return (EntityCompositeInstance) Proxy.getInvocationHandler( aProxy );
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        if( mixins == null ) // Check if this is a lazy-loaded reference
        {
            // Check if removed from session
            // TODO

            // Load state
            CompositeBinding binding = context.getCompositeBinding();
            if( state == null )
            {
                state = store.getEntityInstance( session, identity, binding.getCompositeResolution().getCompositeModel().getCompositeClass(), binding.getPropertyBindings(), binding.getAssociationBindings() );
            }
            context.newEntityMixins( moduleInstance, this, state );
        }

        MethodDescriptor descriptor = context.getMethodDescriptor( method );
        if( descriptor == null )
        {
            return invokeObject( composite, method, args );
        }

        Object mixin = mixins[ descriptor.getMixinIndex() ];

        if( mixin == null )
        {
            throw new InvalidCompositeException( "Implementation missing for method " + method.getName() + "() ",
                                                 context.getCompositeModel().getCompositeClass() );
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

    public EntitySession getSession()
    {
        return session;
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
}
