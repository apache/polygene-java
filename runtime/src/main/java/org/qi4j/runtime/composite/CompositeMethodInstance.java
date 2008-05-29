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
import java.lang.reflect.Method;

public final class CompositeMethodInstance
{
    // Boolean system property for controlling compaction of stack traces. Set this property to "false" to disable compaction
    // Default value is "true"
    private static final String COMPACT_TRACE = "qi4j.compacttrace";
    private static CompactLevel compactLevel = CompactLevel.proxy;

    private Object firstConcern;
    private Object[] sideEffects;
    private SideEffectInvocationHandlerResult sideEffectResult;
    private Method method;
    private Class mixinType;
    private FragmentInvocationHandler mixinInvocationHandler;
    private ProxyReferenceInvocationHandler proxyHandler;
    private CompositeMethodInstancePool poolComposite;
    private CompositeMethodInstance next;
    private InvocationType invocationType;

    static
    {
        compactLevel = CompactLevel.valueOf( System.getProperty( COMPACT_TRACE, "proxy" ) );
    }

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

        if( this.firstConcern == null )
        {
            if( aMixinInvocationHandler instanceof GenericFragmentInvocationHandler )
            {
                invocationType = InvocationType.NoConcerns_InvocationHandler;
            }
            else
            {
                invocationType = InvocationType.NoConcerns_TypedMixin;
            }
        }
        else
        {
            if( this.firstConcern instanceof InvocationHandler )
            {
                invocationType = InvocationType.Concerns_InvocationHandler;
            }
            else
            {
                invocationType = InvocationType.Concerns_TypedMixin;
            }
        }
    }

    public Method getMethod()
    {
        return method;
    }

    public Object invoke( Object proxy, Object[] params, Object mixin )
        throws Throwable
    {
/*
        try
        {
            Object result = null;
            switch( invocationType )
            {
            case NoConcerns_InvocationHandler:
            {
                result = ( (InvocationHandler) mixin ).invoke( proxy, method, params );
                if( sideEffects.length > 0 )
                {
                    proxyHandler.setContext( proxy, mixin, mixinType );
                }
                break;
            }
            case NoConcerns_TypedMixin:
            {
                result = method.invoke( mixin, params );
                if( sideEffects.length > 0 )
                {
                    proxyHandler.setContext( proxy, mixin, mixinType );
                }
                break;
            }
            case Concerns_InvocationHandler:
            {
                proxyHandler.setContext( proxy, mixin, mixinType );
                mixinInvocationHandler.setFragment( mixin );

                result = ( (InvocationHandler) firstConcern ).invoke( proxy, method, params );
                break;
            }
            case Concerns_TypedMixin:
            {
                proxyHandler.setContext( proxy, mixin, mixinType );
                mixinInvocationHandler.setFragment( mixin );

                result = method.invoke( firstConcern, params );
                break;
            }
            }

            // Check for side-effects
//            invokeSideEffects( result, null, proxy, args );

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
//            invokeSideEffects( null, throwable, proxy, args );

            throw throwable;
        }
        finally
        {
            proxyHandler.clearContext();
            poolComposite.returnInstance( this );
        }
*/
        return null;
    }

    /**
     * If the origin of the exception is application code,
     * then clean out the framework from the stacktrace.
     *
     * @param throwable TODO
     * @param proxy     TODO
     * @param method    TODO
     */
    private void fixStackTrace( Throwable throwable, Object proxy, Method method )
    {
        if( compactLevel == CompactLevel.off )
        {
            return;
        }

        StackTraceElement[] trace = throwable.getStackTrace();
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
            {
                boolean classOrigin = stackTraceElement.getClassName().equals( proxy.getClass().getSimpleName() );
                boolean methodOrigin = stackTraceElement.getMethodName().equals( method.getName() );
                if( classOrigin && methodOrigin && compactLevel == CompactLevel.proxy )
                {
                    // Stop removing if the originating method call has been located in the stack.
                    // For 'semi' and 'extensive' compaction, we don't and do the entire stack instead.
                    trace[ i ] = new StackTraceElement( proxy.getClass().getInterfaces()[ 0 ].getName(), method.getName(), null, -1 );
                    break; // Stop compacting this trace
                }
            }
        }

        // Create new trace array
        int idx = 0;
        StackTraceElement[] newTrace = new StackTraceElement[trace.length - count];
        for( StackTraceElement stackTraceElement : trace )
        {
            if( stackTraceElement != null )
            {
                newTrace[ idx++ ] = stackTraceElement;
            }
        }
        throwable.setStackTrace( newTrace );

        Throwable nested = throwable.getCause();
        if( nested != null )
        {
            fixStackTrace( nested, proxy, method );
        }
    }

    private boolean isApplicationClass( String className )
    {
        if( compactLevel == CompactLevel.semi )
        {
            return !isJdkInternals( className );
        }
        return !( className.startsWith( "org.qi4j.runtime" ) ||
                  isJdkInternals( className ) );
    }

    private boolean isJdkInternals( String className )
    {
        return className.startsWith( "java.lang.reflect" ) ||
               className.startsWith( "sun.reflect" );
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

enum InvocationType
{
    NoConcerns_InvocationHandler, NoConcerns_TypedMixin,
    Concerns_InvocationHandler, Concerns_TypedMixin
}

/**
 * Compaction Level of the StackTrace clenaup operation.
 *
 * <pre>
 * <b>off</b>       = Do not modify the stack trace.
 * <b>proxy</b>     = Remove all Qi4j internal classes and all JDK internal classes from
 *             the originating method call.
 * <b>semi</b>      = Remove all JDK internal classes on the entire stack.
 * <b>extensive</b> = Remove all Qi4j internal and JDK internal classes from the entire stack.
 * </pre>
 *
 * <p>
 * The Compaction is set through the System Property "<code><b>qi4j.compacttrace</b></code>" to
 * any of the above values.
 * </p>
 */
enum CompactLevel
{
    off, proxy, semi, extensive
}