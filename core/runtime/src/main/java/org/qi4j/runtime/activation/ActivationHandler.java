/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.runtime.activation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;

/**
 * This class will manage activation of a target and propagates to children.
 */
public final class ActivationHandler
{
    private final Object target;
    private ActivatorsInstance targetActivators = null;
    private final LinkedList<Activatable> activeChildren = new LinkedList<Activatable>();

    public ActivationHandler( Object target )
    {
        this.target = target;
    }
    
    public void activate( ActivatorsInstance targetActivators, Activatable child )
            throws Exception
    {
        activate( targetActivators, Collections.singleton( child ), null );
    }
    
    public void activate( ActivatorsInstance targetActivators, Activatable child, Runnable internalActivationCallback )
        throws Exception
    {
        activate( targetActivators, Collections.singleton( child ), internalActivationCallback );
    }
    
    public void activate( ActivatorsInstance targetActivators,  Iterable<? extends Activatable> children )
        throws Exception
    {
        activate( targetActivators, children, null );
    }
    
    public void activate( ActivatorsInstance targetActivators,  Iterable<? extends Activatable> children, Runnable internalActivationCallback )
        throws Exception
    {
        if ( this.targetActivators != null ) {
            throw new IllegalStateException( "ActivationHandler.activate() called multiple times or without passivation first!" );
        }
        targetActivators.beforeActivation( target instanceof ServiceReference
                                            ? new InactiveServiceReference( ( ServiceReference ) target )
                                            : target );
        try
        {
            for( Activatable child : children )
            {
                if( ! activeChildren.contains( child ) )
                {
                    child.activate();
                }
                activeChildren.addFirst( child );
            }
            if( internalActivationCallback != null )
            {
                internalActivationCallback.run();
            }
            targetActivators.afterActivation( target );
            this.targetActivators = targetActivators;
        }
        catch( Exception e )
        {
            // Passivate actives
            passivate();
            throw e;
        }
        catch( Throwable e )
        {
            // Passivate actives
            passivate();
            throw new IllegalStateException( e );
        }
    }

    public void passivate()
        throws Exception
    {
        passivate( ( Runnable ) null );
    }
    
    public void passivate( Runnable internalPassivationCallback  )
        throws Exception
    {
        List<Exception> exceptions = new ArrayList<Exception>();
        if ( targetActivators != null )
        {
            try
            {
                targetActivators.beforePassivation( target );
            }
            catch( Exception ex )
            {
                if( ex instanceof PassivationException )
                {
                    exceptions.addAll( Arrays.asList( ( ( PassivationException ) ex ).causes() ) );
                }
                else
                {
                    exceptions.add( ex );
                }
            }
        }
        while( !activeChildren.isEmpty() )
        {
            passivateOneChild( exceptions );
        }
        if( internalPassivationCallback != null )
        {
            internalPassivationCallback.run();
        }
        if ( targetActivators != null )
        {
            try
            {
                targetActivators.afterPassivation( target instanceof ServiceReference
                                                ? new InactiveServiceReference( ( ServiceReference ) target )
                                                : target );
            }
            catch( Exception ex )
            {
                if( ex instanceof PassivationException )
                {
                    exceptions.addAll( Arrays.asList( ( ( PassivationException ) ex ).causes() ) );
                }
                else
                {
                    exceptions.add( ex );
                }
            }
        }
        targetActivators = null;
        if( exceptions.isEmpty() )
        {
            return;
        }
        if( exceptions.size() == 1 )
        {
            throw exceptions.get( 0 );
        }
        throw new PassivationException( exceptions );
    }

    private void passivateOneChild( List<Exception> exceptions )
    {
        Activatable activeChild = activeChildren.removeFirst();
        try
        {
            activeChild.passivate();
        }
        catch( Exception ex )
        {
            if( ex instanceof PassivationException )
            {
                exceptions.addAll( Arrays.asList( ( ( PassivationException ) ex ).causes() ) );
            }
            else
            {
                exceptions.add( ex );
            }
        }
    }

    private static class InactiveServiceReference
            implements ServiceReference
    {

        private final ServiceReference delegate;

        private InactiveServiceReference( ServiceReference delegate )
        {
            this.delegate = delegate;
        }

        public String identity()
        {
            return delegate.identity();
        }

        public Object get()
        {
            throw new IllegalStateException( "Service is activating and can't be used yet." );
        }

        public boolean isActive()
        {
            return false;
        }

        public boolean isAvailable()
        {
            return false;
        }

        public Iterable<Class<?>> types()
        {
            return delegate.types();
        }

        public <T> T metaInfo( Class<T> infoType )
        {
            return delegate.metaInfo( infoType );
        }

        public void registerActivationEventListener( ActivationEventListener listener )
        {
            delegate.registerActivationEventListener( listener );
        }

        public void deregisterActivationEventListener( ActivationEventListener listener )
        {
            delegate.deregisterActivationEventListener( listener );
        }

    }
}
