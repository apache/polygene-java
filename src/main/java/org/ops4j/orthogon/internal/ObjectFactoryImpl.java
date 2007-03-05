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
import java.util.Set;
import java.util.HashSet;
import org.ops4j.orthogon.ObjectFactory;
import org.ops4j.orthogon.pointcut.AspectRegistry;

public final class ObjectFactoryImpl
    implements ObjectFactory
{
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Class[] EMPTY_ASPECTS = EMPTY_CLASS_ARRAY;
    private MixinFactory m_mixinFactory;
    private AdviceFactoryImpl m_adviceFactory;
    private AspectRegistry m_aspectRegistry;

    public ObjectFactoryImpl( MixinFactory mixinFactory, AdviceFactoryImpl adviceFactory, AspectRegistry aspectRegistry )
    {
        m_mixinFactory = mixinFactory;
        m_adviceFactory = adviceFactory;
        m_aspectRegistry = aspectRegistry;
    }

    public <T> T newInstance( ClassLoader classloader, Class<T> primaryAspect )
    {
        return newInstance( classloader, primaryAspect, EMPTY_ASPECTS );
    }

    @SuppressWarnings( "unchecked" )
    public <T> T newInstance( ClassLoader classloader, Class<T> primaryAspect, Class... secondaries )
    {
        Set<Class> aspects = new HashSet<Class>();
        aspects.add( primaryAspect );
        for( Class secondary : secondaries )
        {
            aspects.add( secondary );
        }
        AspectRoutingHandler handler = getInvocationHandler( aspects );
        return (T) Proxy.newProxyInstance( classloader, aspects.toArray( EMPTY_CLASS_ARRAY ), handler );
    }

    private <T> AspectRoutingHandler getInvocationHandler( Set<Class> aspects )
    {

        return new AspectRoutingHandler( m_mixinFactory, aspects, m_adviceFactory );
    }
}
