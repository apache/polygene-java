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

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import org.ops4j.orthogon.AspectFactory;
import org.ops4j.orthogon.pointcut.AspectRegistry;

public final class AspectFactoryImpl
    implements AspectFactory
{
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Class[] EMPTY_ASPECTS = EMPTY_CLASS_ARRAY;
    private MixinFactory m_mixinFactory;
    private AdviceFactoryImpl m_adviceFactory;
    private AspectRegistry m_aspectRegistry;
    private InvocationStackPool m_pool;

    public AspectFactoryImpl( MixinFactory mixinFactory, AdviceFactoryImpl adviceFactory,
                              AspectRegistry aspectRegistry
    )
    {
        m_mixinFactory = mixinFactory;
        m_adviceFactory = adviceFactory;
        m_aspectRegistry = aspectRegistry;
        InvocationStackFactory invocationStackFactory = new InvocationStackFactory();
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

    public void checkExistence( Class invokedOn )
    {
    }

    public Object createMixin( Class invokedOn )
    {
        return null;
    }

    public InvocationStack getInvocationStack( Method invokedMethod, Object proxy )
    {
        PointcutDescriptor adviceDescriptor = m_aspectRegistry.getPointcutDescriptor( invokedMethod, proxy );
        InvocationStack stack = m_pool.getInvocationStack( adviceDescriptor );
        return stack;
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
