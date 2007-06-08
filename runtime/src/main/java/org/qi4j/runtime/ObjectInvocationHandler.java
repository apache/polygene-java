/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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

package org.qi4j.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.Composite;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.Mixin;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.persistence.Identity;
import org.qi4j.spi.object.InvocationInstance;
import org.qi4j.spi.object.ObjectContext;

/**
 * TODO
 */
public class ObjectInvocationHandler
    implements InvocationHandler
{
    private ObjectContext context;
    private Map<Class, Object> mixins;

    public ObjectInvocationHandler( ObjectContext aContext )
    {
        this.context = aContext;
        mixins = new IdentityHashMap<Class, Object>();
    }

    public static ObjectInvocationHandler getInvocationHandler( Object aProxy )
    {
        return (ObjectInvocationHandler) Proxy.getInvocationHandler( aProxy );
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        Class proxyInterface = method.getDeclaringClass();

        Object mixin = mixins.get( proxyInterface );

        if( mixin == null )
        {
            if( proxyInterface.equals( Object.class ) )
            {
                if( method.getName().equals( "hashCode" ) )
                {
                    if( Identity.class.isAssignableFrom( context.getComposite().getCompositeClass() ) )
                    {
                        String id = ( (Identity) proxy ).getIdentity();
                        if( id != null )
                        {
                            return id.hashCode();
                        }
                        else
                        {
                            return 0;
                        }
                    }
                    else
                    {
                        return 0; // TODO ?
                    }
                }
                if( method.getName().equals( "equals" ) )
                {
                    if( Identity.class.isAssignableFrom( context.getComposite().getCompositeClass() ) )
                    {
                        String id = ( (Identity) proxy ).getIdentity();
                        return id != null && id.equals( ( (Identity) args[ 0 ] ).getIdentity() );
                    }
                    else
                    {
                        return false;
                    }
                }
                if( method.getName().equals( "toString" ) )
                {
                    if( Identity.class.isAssignableFrom( context.getComposite().getCompositeClass() ) )
                    {
                        String id = ( (Identity) proxy ).getIdentity();
                        return id != null ? id : "";
                    }
                    else
                    {
                        return "";
                    }
                }
            }

            mixin = initializeMixin( proxyInterface, proxy, getDecoratedInstance() );
        }

        // Get interface modifiers
        List<InvocationInstance> instances = context.getMethodToInvocationInstanceMap().get( method );

        if( instances == null )
        {
            instances = new ArrayList<InvocationInstance>();
            context.getMethodToInvocationInstanceMap().put( method, instances );
        }

        InvocationInstance invocationInstance;
        int size = instances.size();
        if( size > 0 )
        {
            invocationInstance = instances.remove( size - 1 );
        }
        else
        {
            invocationInstance = context.newInvocationInstance( method);
        }

        // Invoke
        try
        {
            return invocationInstance.invoke(proxy, method, args, mixin);
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
        catch( UndeclaredThrowableException e )
        {
            throw e.getUndeclaredThrowable();
        }
    }

    public Map<Class, Object> getMixins()
    {
        return mixins;
    }

    // Private -------------------------------------------------------
    protected Object initializeMixin( Class aProxyInterface, Object proxy, Object decoratedInstance )
        throws IllegalAccessException
    {
        if( aProxyInterface.isInstance( decoratedInstance ) )
        {
            return decoratedInstance;
        }

        List<Mixin> implementationClasses = context.getComposite().getMixins( aProxyInterface );

        // Check if implementation is latent in decorated object
        if( implementationClasses == null && decoratedInstance != null )
        {
            Composite decoratedComposite = context.getObjectFactory().getComposite( decoratedInstance.getClass().getInterfaces()[ 0 ] );
            implementationClasses = decoratedComposite.getMixins( aProxyInterface );
        }

        if( implementationClasses == null )
        {
            throw new ObjectInstantiationException( "Could not find implementation for " + aProxyInterface.getName() + " in composite " + context.getComposite().getCompositeClass().getName() );
        }

        Object mixin;

        ObjectInstantiationException ex = null;
        mixins:
        for( Mixin mixinClass : implementationClasses )
        {
            mixin = context.getMixinFactory().newInstance( mixinClass.getFragmentClass() );

            List<Field> usesFields = mixinClass.getUsesFields();
            for( Field usesField : usesFields )
            {
                if( usesField.getType().isInstance( proxy ) )
                {
                    usesField.set( mixin, proxy );
                }
                else if( usesField.getType().isInstance( decoratedInstance ) )
                {
                    usesField.set( mixin, decoratedInstance );
                }
                else if( context.getObjectFactory().isInstance( usesField.getType(), decoratedInstance ) )
                {
                    usesField.set( mixin, context.getObjectFactory().cast( usesField.getType(), decoratedInstance ) );
                }
                else
                {
                    ex = new ObjectInstantiationException( "@Uses field " + usesField.getName() + " in class " + mixinClass.getFragmentClass().getName() + " could not be resolved for composite " + context.getComposite().getCompositeClass().getName() + "." );
                    continue mixins;
                }
            }

            List<Field> dependencyFields = mixinClass.getDependencyFields();
            for( Field dependencyField : dependencyFields )
            {
                /* TODO: Dependency Resolver, something like;
                    Object value = dependecyResolver.get( dependencyField.getType );
                    dependencyField.set( mixin. value );
                 */
                if( dependencyField.getType().equals( ObjectFactory.class ) )
                {
                    dependencyField.set( mixin, context.getObjectFactory() );
                }
                else if( dependencyField.getType().equals( FragmentFactory.class ) )
                {
                    dependencyField.set( mixin, context.getMixinFactory() );
                }
                else
                {
                    ex = new ObjectInstantiationException( "@Dependency field " + dependencyField.getName() + " in class " + mixinClass.getFragmentClass().getName() + " could not be resolved." );
                    continue mixins;
                }
            }

            // Successfully instantiated
            mixins.put( aProxyInterface, mixin );
            return mixin;
        }

        // No mixin was successfully instantiated - throw exception
        throw ex;
    }

    protected Object getDecoratedInstance()
    {
        return null;
    }
}
