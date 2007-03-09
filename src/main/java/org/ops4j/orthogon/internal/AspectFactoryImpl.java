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
package org.ops4j.orthogon.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.AspectFactory;

public final class AspectFactoryImpl
    implements AspectFactory
{

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Class[] EMPTY_ASPECTS = EMPTY_CLASS_ARRAY;
    private MixinFactory m_mixinFactory;
    private AdviceFactoryImpl m_adviceFactory;
    private AspectRegistry m_aspectRegistry;
    private InvocationStackPool m_pool;

    public AspectFactoryImpl(
        MixinFactory mixinFactory, AdviceFactoryImpl adviceFactory, AspectRegistry aspectRegistry
    )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixinFactory, "mixinFactory" );
        NullArgumentException.validateNotNull( adviceFactory, "adviceFactory" );
        NullArgumentException.validateNotNull( aspectRegistry, "aspectRegistry" );

        m_mixinFactory = mixinFactory;
        m_adviceFactory = adviceFactory;
        m_aspectRegistry = aspectRegistry;
        InvocationStackFactory invocationStackFactory = new InvocationStackFactory( aspectRegistry );
        m_pool = new InvocationStackPool( invocationStackFactory );
    }

    @SuppressWarnings( "unchecked" )
    public <T> T newInstance( ClassLoader classloader, Class<T> primaryAspect )
    {
        AspectRoutingHandler handler = getInvocationHandler( primaryAspect );
        return (T) Proxy.newProxyInstance( classloader, new Class[] { primaryAspect }, handler );
    }

    public <T> T getInstance( String identity )
    {
        return null;
    }

    /**
     * Returns {@code true} if the specified {@code mixinInterface} is still exists, {@code false} otherwise.
     *
     * @param mixinInterface The mixin interface class to check. This argument must not be {@code null}.
     *
     * @return A {@code boolean} indicator whether the specified {@code mixinInterface} is exists.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code mixinInterface} is {@code null}.
     * @since 1.0.0
     */
    final boolean isMixinInterfaceExists( Class mixinInterface )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixinInterface, "mixinInterface" );
        return m_mixinFactory.checkExistence( mixinInterface );
    }

    /**
     * Returns the mixing implementation instance of the specified {@code mixinInterface}. Returns {@code null} if the
     * mixin interface implementation is not registered.
     *
     * @param mixinInterface The mixin interface. This argument must not be {@code null}.
     *
     * @return Returns the mixin implementation.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code mixinInterface} is {@code null}.
     * @since 1.0.0
     */
    public Object createMixin( Class mixinInterface )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixinInterface, "mixinInterface" );

        return m_mixinFactory.create( mixinInterface );
    }

    public InvocationStack getInvocationStack( Method invokedMethod, Object proxy )
    {
        Class<? extends Object> proxyClass = proxy.getClass();
        Class[] targetClasses = proxyClass.getInterfaces();
        JoinpointDescriptor adviceDescriptor = new JoinpointDescriptor( invokedMethod, targetClasses );
        return m_pool.getInvocationStack( adviceDescriptor );
    }

    public void release( InvocationStack stack )
    {
        m_pool.release( stack );
    }

    public AspectRegistry getAspectRegistry()
    {
        return m_aspectRegistry;
    }

    private <T> AspectRoutingHandler getInvocationHandler( Class aspect )
    {
        return new AspectRoutingHandler( this );
    }
}
