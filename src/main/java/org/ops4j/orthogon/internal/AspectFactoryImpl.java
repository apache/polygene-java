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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.AspectFactory;
import org.ops4j.orthogon.mixin.QiMixin;

public final class AspectFactoryImpl
    implements AspectFactory
{
    private static final Class[] BLANK_CLASS_ARRAY = new Class[0];

    private static final ThreadLocal<ProxyCache> m_proxyCache = new ThreadLocal<ProxyCache>();
    private final MixinFactory m_mixinFactory;
    private final InvocationStackPool m_pool;

    public AspectFactoryImpl( MixinFactory mixinFactory, InvocationStackFactory invocationStackFactory )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixinFactory, "mixinFactory" );
        NullArgumentException.validateNotNull( invocationStackFactory, "invocationStackFactory" );

        m_mixinFactory = mixinFactory;
        m_pool = new InvocationStackPool( invocationStackFactory );
    }

    @SuppressWarnings( "unchecked" )
    public <T> T newInstance( Class<T> primaryAspect )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( primaryAspect, "primaryAspect" );
        AspectRoutingHandler handler = getInvocationHandler( primaryAspect );

        ClassLoader classLoader = primaryAspect.getClassLoader();
        Class[] classes = getAllDeclaringInterfaces( primaryAspect );
        return (T) Proxy.newProxyInstance( classLoader, classes, handler );
    }

    public <T> T getInstance( String identity )
    {
        // TODO: finish implementation
        return null;
    }

    /**
     * Returns the mixing implementation instance of the specified {@code mixinInterface}. Returns {@code null} if the
     * mixin interface implementation is not registered.
     *
     * @param mixinInterface The mixin interface.
     * @param primaryAspect  The primary aspect that creates the specified {@code mixinInterface}.
     *                       This argument must not be {@code null}.
     * @param proxy          The proxy that creates this mxin. This argument must not be {@code null}.
     *
     * @return Returns the mixin implementation.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code primaryAspect} or(and) {@code proxy} arguments
     *                                  are {@code null}.
     * @since 1.0.0
     */
    final Object createMixin( Class mixinInterface, Class primaryAspect, Object proxy )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( primaryAspect, "primaryAspect" );

        return m_mixinFactory.create( mixinInterface, primaryAspect, proxy );
    }

    private <T> Class[] getAllDeclaringInterfaces( Class<T> aClass )
    {
        Set<Class> results = new HashSet<Class>();
        results.add( aClass );

        Stack<Class> stack = new Stack<Class>();
        stack.add( aClass );
        while( !stack.isEmpty() )
        {
            Class current = stack.pop();

            Class[] interfaces = current.getInterfaces();
            for( Class aInterface : interfaces )
            {
                if( !results.contains( aInterface ) )
                {
                    stack.add( aInterface );
                }
            }

            Annotation qiMixin = current.getAnnotation( QiMixin.class );
            if( qiMixin != null )
            {
                results.add( current );
            }
        }

        return results.toArray( BLANK_CLASS_ARRAY );
    }

    private AspectRoutingHandler getInvocationHandler( Class primaryAspect )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( primaryAspect, "primaryAspect" );

        return new AspectRoutingHandler( primaryAspect, this );
    }

    final InvocationStack getInvocationStack( Method invokedMethod, Object proxy )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( invokedMethod, "invokedMethod" );
        NullArgumentException.validateNotNull( proxy, "proxy" );

        Class proxyClass = proxy.getClass();
        ProxyCache localProxyCache = m_proxyCache.get();

        if( localProxyCache == null || localProxyCache.needsRefresh( proxyClass ) )
        {
            localProxyCache = new ProxyCache( proxyClass );
            m_proxyCache.set( localProxyCache );
        }

        Class[] targetClasses = localProxyCache.getInterfaces();
        JoinpointDescriptor adviceDescriptor = new JoinpointDescriptor( invokedMethod, targetClasses );
        return m_pool.getInvocationStack( adviceDescriptor );
    }

    /**
     * Returns {@code true} if the specified {@code mixinInterface} is still exists, {@code false} otherwise.
     *
     * @param mixinInterface The mixin interface class to check.
     *
     * @return A {@code boolean} indicator whether the specified {@code mixinInterface} is exists.
     *
     * @since 1.0.0
     */
    final boolean isMixinInterfaceExists( Class mixinInterface )
    {
        return m_mixinFactory.checkExistence( mixinInterface );
    }

    final void release( InvocationStack stack )
    {
        if( stack == null )
        {
            return;
        }

        m_pool.release( stack );
    }

    private static final class ProxyCache
    {
        final Class m_proxyClass;
        final Class[] m_interfaces;

        public ProxyCache( Class proxyClass )
        {
            m_proxyClass = proxyClass;
            m_interfaces = proxyClass.getInterfaces();
        }

        public boolean needsRefresh( Class proxyClass )
        {
            return !m_proxyClass.equals( proxyClass );
        }

        public Class[] getInterfaces()
        {
            return m_interfaces;
        }
    }
}
