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
package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CompositeMethodInstance
{
    // Boolean system property for controlling compaction of stack traces. Set this property to "false" to disable compaction
    // Default value is "true"
    private static final String COMPACT_TRACE = "qi4j.compacttrace";

    private Object firstConcern;
    private Object[] sideEffects;
    private SideEffectInvocationHandlerResult sideEffectResult;
    private Method method;
    private Class mixinType;
    private FragmentInvocationHandler mixinInvocationHandler;
    private ProxyReferenceInvocationHandler proxyHandler;
    private CompositeMethodInstancePool poolComposite;
    private CompositeMethodInstance next;

    public CompositeMethodInstance( Object firstConcern, Object[] sideEffects, SideEffectInvocationHandlerResult sideEffectResult, FragmentInvocationHandler aMixinInvocationHandler, ProxyReferenceInvocationHandler aProxyHandler, CompositeMethodInstancePool poolComposite, Method method, Class mixinType )
    {
        this.sideEffectResult = sideEffectResult;
        this.sideEffects = sideEffects;
        this.mixinType = mixinType;
        this.method = method;
        method.setAccessible( true );
        if( firstConcern != aMixinInvocationHandler )
        {
            this.firstConcern = firstConcern; // No modifiers -> skip it
        }
        proxyHandler = aProxyHandler;
        mixinInvocationHandler = aMixinInvocationHandler;
        this.poolComposite = poolComposite;
    }

    public Object invoke( Object proxy, Object[] args, Object mixin )
        throws Throwable
    {
        try
        {
            Object result;
            if( firstConcern == null )
            {
                if( mixin instanceof InvocationHandler )
                {
                    result = ( (InvocationHandler) mixin ).invoke( proxy, method, args );
                }
                else
                {
                    result = method.invoke( mixin, args );
                }

                if( sideEffects.length > 0 )
                {
                    proxyHandler.setContext( proxy, mixin, mixinType );
                }
            }
            else
            {
                proxyHandler.setContext( proxy, mixin, mixinType );
                mixinInvocationHandler.setFragment( mixin );
                if( firstConcern instanceof InvocationHandler )
                {
                    result = ( (InvocationHandler) firstConcern ).invoke( proxy, method, args );
                }
                else
                {
                    result = method.invoke( firstConcern, args );
                }
            }

            // Check for side-effects
            invokeSideEffects( result, null, proxy, args );

            return result;
        }
        catch( Throwable throwable )
        {
            if( throwable instanceof InvocationTargetException )
            {
                throwable = ( (InvocationTargetException) throwable ).getTargetException();
            }

            // Clean stacktrace
            fixStackTrace( throwable, proxy, method );

            proxyHandler.setContext( proxy, mixin, mixinType );
            invokeSideEffects( null, throwable, proxy, args );

            throw throwable;
        }
        finally
        {
            proxyHandler.clearContext();
            poolComposite.returnInstance( this );
        }
    }

    /**
     * If the origin of the exception is application code,
     * then clean out the framework from the stacktrace.
     *
     * @param throwable
     * @param proxy
     * @param method
     */
    private void fixStackTrace( Throwable throwable, Object proxy, Method method )
    {
        if( !Boolean.parseBoolean( System.getProperty( COMPACT_TRACE, "true" ) ) )
        {
            return;
        }

        StackTraceElement[] trace = throwable.getStackTrace();
//        if (isApplicationClass(trace[0].getClassName()))
        {
            int count = 0;
            for( int i = 0; i < trace.length; i++ )
            {
                StackTraceElement stackTraceElement = trace[ i ];
                if( !isApplicationClass( stackTraceElement.getClassName() ) )
                {
                    trace[ i ] = null;
                    count++;
                }
                else
                if( stackTraceElement.getClassName().equals( proxy.getClass().getSimpleName() ) && stackTraceElement.getMethodName().equals( method.getName() ) )
                {
                    trace[ i ] = new StackTraceElement( proxy.getClass().getInterfaces()[ 0 ].getName(), method.getName(), null, -1 );
                    break; // Stop compacting this trace
                }
            }

            // Create new trace array
            int idx = 0;
            StackTraceElement[] newTrace = new StackTraceElement[trace.length - count];
            for( int i = 0; i < trace.length; i++ )
            {
                StackTraceElement stackTraceElement = trace[ i ];
                if( stackTraceElement != null )
                {
                    newTrace[ idx++ ] = stackTraceElement;
                }
            }
            throwable.setStackTrace( newTrace );
        }
    }

    private boolean isApplicationClass( String className )
    {
        return !( className.startsWith( "org.qi4j.runtime" ) ||
                  className.startsWith( "java.lang.reflect" ) ||
                  className.startsWith( "sun.reflect" ) );
    }

    private void invokeSideEffects( Object result, Throwable throwable, Object proxy, Object[] args )
    {
        sideEffectResult.setResult( result, throwable );
        for( Object sideEffect : sideEffects )
        {
            if( sideEffect instanceof InvocationHandler )
            {
                InvocationHandler handler = (InvocationHandler) sideEffect;
                try
                {
                    handler.invoke( proxy, method, args );
                }
                catch( Throwable e )
                {
                    // Ignore (?)
                }
            }
            else
            {
                try
                {
                    method.invoke( sideEffect, args );
                }
                catch( Throwable e )
                {
                    // Ignore (?)
                    e.printStackTrace();
                }
            }
        }
    }

    public CompositeMethodInstance getNext()
    {
        return next;
    }

    public void setNext( CompositeMethodInstance next )
    {
        this.next = next;
    }
}
