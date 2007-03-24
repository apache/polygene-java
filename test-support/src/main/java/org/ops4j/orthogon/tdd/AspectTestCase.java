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
package org.ops4j.orthogon.tdd;

import java.lang.reflect.Proxy;
import java.util.Set;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.AspectFactory;
import org.qi4j.runtime.internal.AspectFactoryImpl;
import org.qi4j.runtime.internal.AspectParser;
import org.qi4j.runtime.internal.AspectRegistry;
import org.qi4j.runtime.internal.AspectRoutingHandler;
import org.qi4j.runtime.internal.InvocationStackFactory;
import org.qi4j.runtime.internal.MixinFactory;
import org.qi4j.runtime.mixin.MixinUnavailableException;

public class AspectTestCase
{
    private static final MixinFactory MIXIN_FACTORY;
    private static final AspectRegistry ASPECT_REGISTRY;
    private static final AspectFactoryImpl ASPECT_FACTORY;

    static
    {
        MIXIN_FACTORY = new MixinFactory();
        ASPECT_REGISTRY = new AspectRegistry();
        InvocationStackFactory factory = new InvocationStackFactory( ASPECT_REGISTRY );
        ASPECT_FACTORY = new AspectFactoryImpl( MIXIN_FACTORY, factory );
    }

    public static AspectFactory getAspectFactory()
    {
        return ASPECT_FACTORY;
    }

    public static void registerMixin( Class... mixins )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixins, "mixins" );
        MIXIN_FACTORY.registerMixin( mixins );
    }

    public static void unregisterMixin( Class... mixin )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixin, "mixin" );
        MIXIN_FACTORY.unregisterMixin( mixin );
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T addMixin( Object proxy, Class<T> mixinInterface )
        throws IllegalArgumentException, MixinUnavailableException
    {
        NullArgumentException.validateNotNull( proxy, "proxy" );
        NullArgumentException.validateNotNull( mixinInterface, "mixinInterface" );

        boolean valid = MIXIN_FACTORY.checkExistence( mixinInterface );
        if( !valid )
        {
            throw new MixinUnavailableException( mixinInterface );
        }

        AspectRoutingHandler handler = (AspectRoutingHandler) Proxy.getInvocationHandler( proxy );
        handler.addMixinInterface( mixinInterface );

        Class[] currentAspects = proxy.getClass().getInterfaces();
        int numberOfCurrentAspects = currentAspects.length;

        Class[] aspects = new Class[numberOfCurrentAspects + 1];
        aspects[ 0 ] = mixinInterface;
        System.arraycopy( currentAspects, 0, aspects, 1, numberOfCurrentAspects );
        return (T) Proxy.newProxyInstance( mixinInterface.getClassLoader(), aspects, handler );
    }

    @SuppressWarnings( "unchecked" )
    public static Object removeMixin( Object proxy, Class mixinInterface )
        throws IllegalArgumentException, MixinUnavailableException
    {
        NullArgumentException.validateNotNull( proxy, "proxy" );
        NullArgumentException.validateNotNull( mixinInterface, "mixinInterface" );

        AspectRoutingHandler handler = (AspectRoutingHandler) Proxy.getInvocationHandler( proxy );
        Class[] currentAspects = proxy.getClass().getInterfaces();

        boolean valid = false;
        for( Class currentAspect : currentAspects )
        {
            if( mixinInterface.equals( currentAspect ) )
            {
                valid = true;
                break;
            }
        }

        if( !valid )
        {
            throw new MixinUnavailableException( mixinInterface );
        }

        handler.removeMixinInterface( mixinInterface );
        Set<Class> mixins = handler.getMixinInterfaces();
        Class[] aspects = mixins.toArray( new Class[0] );
        return Proxy.newProxyInstance( mixinInterface.getClassLoader(), aspects, handler );
    }

    public static void registerPointcut( Class... pointcutInterfaces )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( pointcutInterfaces, "pointcutInterfaces" );

        AspectParser aspectParser = new AspectParser( ASPECT_REGISTRY );
        aspectParser.parse( pointcutInterfaces );
    }
}
