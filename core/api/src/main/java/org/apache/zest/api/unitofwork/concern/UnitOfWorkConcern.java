/*  Copyright 2008 Edward Yakop.
 *  Copyright 2009 Niclas Hedhman.
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
package org.apache.zest.api.unitofwork.concern;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.concern.GenericConcern;
import org.apache.zest.api.injection.scope.Invocation;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.usecase.UsecaseBuilder;

/**
 * {@code UnitOfWorkConcern} manages the unit of work complete, discard and retry policy.
 *
 * @see UnitOfWorkPropagation
 * @see UnitOfWorkDiscardOn
 */
@AppliesTo( UnitOfWorkPropagation.class )
public class UnitOfWorkConcern
    extends GenericConcern
{
    private static final Class<?>[] DEFAULT_DISCARD_CLASSES = new Class[]{ Throwable.class };

    @Structure
    Module module;

    @Invocation
    UnitOfWorkPropagation propagation;

    /**
     * Handles method with {@code UnitOfWorkPropagation} annotation.
     *
     * @param proxy  The object.
     * @param method The invoked method.
     * @param args   The method arguments.
     *
     * @return The returned value of method invocation.
     *
     * @throws Throwable Thrown if the method invocation throw exception.
     */
    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        UnitOfWorkPropagation.Propagation propagationPolicy = propagation.value();
        if( propagationPolicy == UnitOfWorkPropagation.Propagation.REQUIRED )
        {
            if( module.isUnitOfWorkActive() )
            {
                //noinspection ConstantConditions
                return next.invoke( proxy, method, args );
            }
            else
            {
                Usecase usecase = usecase();
                return invokeWithCommit( proxy, method, args, module.newUnitOfWork( usecase ) );
            }
        }
        else if( propagationPolicy == UnitOfWorkPropagation.Propagation.MANDATORY )
        {
            if( !module.isUnitOfWorkActive() )
            {
                throw new IllegalStateException( "UnitOfWork was required but there is no available unit of work." );
            }
        }
        else if( propagationPolicy == UnitOfWorkPropagation.Propagation.REQUIRES_NEW )
        {
            Usecase usecase = usecase();
            return invokeWithCommit( proxy, method, args, module.newUnitOfWork( usecase ) );
        }
        //noinspection ConstantConditions
        return next.invoke( proxy, method, args );
    }

    private Usecase usecase()
    {
        String usecaseName = propagation.usecase();
        Usecase usecase;
        if( usecaseName == null )
        {
            usecase = Usecase.DEFAULT;
        }
        else
        {
            usecase = UsecaseBuilder.newUsecase( usecaseName );
        }
        return usecase;
    }

    protected Object invokeWithCommit( Object proxy, Method method, Object[] args, UnitOfWork currentUnitOfWork )
        throws Throwable
    {
        try
        {
            UnitOfWorkRetry retryAnnot = method.getAnnotation( UnitOfWorkRetry.class );
            int maxTries = 0;
            long delayFactor = 0;
            long initialDelay = 0;
            if( retryAnnot != null )
            {
                maxTries = retryAnnot.retries();
                initialDelay = retryAnnot.initialDelay();
                delayFactor = retryAnnot.delayFactor();
            }
            int retry = 0;
            while( true )
            {
                //noinspection ConstantConditions
                Object result = next.invoke( proxy, method, args );
                try
                {
                    currentUnitOfWork.complete();
                    return result;
                }
                catch( UndeclaredThrowableException e)
                {
                    Throwable undeclared = e.getUndeclaredThrowable();
                    if( undeclared instanceof ConcurrentEntityModificationException )
                    {
                        ConcurrentEntityModificationException ceme = (ConcurrentEntityModificationException) undeclared;
                        currentUnitOfWork = checkRetry( maxTries, delayFactor, initialDelay, retry, ceme );
                        retry++;
                    }
                    else
                    {
                        throw e;
                    }
                }
                catch( ConcurrentEntityModificationException e )
                {
                    currentUnitOfWork = checkRetry( maxTries, delayFactor, initialDelay, retry, e );
                    retry++;
                }
            }
        }
        catch( Throwable throwable )
        {
            // Discard only if this concern create a unit of work
            discardIfRequired( method, currentUnitOfWork, throwable );
            throw throwable;
        }
    }

    private UnitOfWork checkRetry( int maxTries,
                                   long delayFactor,
                                   long initialDelay,
                                   int retry,
                                   ConcurrentEntityModificationException e
    )
        throws ConcurrentEntityModificationException, InterruptedException
    {
        if( retry >= maxTries )
        {
            throw e;
        }
        module.currentUnitOfWork().discard();
        Thread.sleep( initialDelay + retry * delayFactor );
        return module.newUnitOfWork( usecase() );
    }

    /**
     * Discard unit of work if the discard policy match.
     *
     * @param aMethod     The invoked method. This argument must not be {@code null}.
     * @param aUnitOfWork The current unit of work. This argument must not be {@code null}.
     * @param aThrowable  The exception thrown. This argument must not be {@code null}.
     */
    protected void discardIfRequired( Method aMethod, UnitOfWork aUnitOfWork, Throwable aThrowable )
    {
        UnitOfWorkDiscardOn discardPolicy = aMethod.getAnnotation( UnitOfWorkDiscardOn.class );
        Class<?>[] discardClasses;
        if( discardPolicy != null )
        {
            discardClasses = discardPolicy.value();
        }
        else
        {
            discardClasses = DEFAULT_DISCARD_CLASSES;
        }

        Class<? extends Throwable> aThrowableClass = aThrowable.getClass();
        for( Class<?> discardClass : discardClasses )
        {
            if( discardClass.isAssignableFrom( aThrowableClass ) )
            {
                aUnitOfWork.discard();
            }
        }
    }
}