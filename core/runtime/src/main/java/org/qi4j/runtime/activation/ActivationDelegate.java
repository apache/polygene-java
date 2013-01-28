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
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.service.ServiceReference;

/**
 * This class will manage Activation of a target and propagates to children.
 */
public final class ActivationDelegate
{
    private final Object target;
    private ActivatorsInstance targetActivators = null;
    private final LinkedList<Activation> activeChildren = new LinkedList<Activation>();

    public ActivationDelegate( Object target )
    {
        this.target = target;
    }

    public void activate( ActivatorsInstance targetActivators, Activation child )
            throws Exception
    {
        activate( targetActivators, Collections.singleton( child ), null );
    }

    public void activate( ActivatorsInstance targetActivators, Activation child, Runnable callback )
        throws Exception
    {
        activate( targetActivators, Collections.singleton( child ), callback );
    }

    public void activate( ActivatorsInstance targetActivators,  Iterable<? extends Activation> children )
        throws ActivationException
    {
        activate( targetActivators, children, null );
    }

    public void activate( ActivatorsInstance targetActivators,  Iterable<? extends Activation> children, Runnable callback )
        throws ActivationException
    {
        if ( this.targetActivators != null ) {
            throw new IllegalStateException( "Activation.activate() called multiple times or without calling passivate() first!" );
        }

        // Before Activation
        targetActivators.beforeActivation( target instanceof ServiceReference
                                            ? new PassiveServiceReference( ( ServiceReference ) target )
                                            : target );

        try
        {
            // Activation
            for( Activation child : children )
            {
                if( ! activeChildren.contains( child ) )
                {
                    child.activate();
                }
                activeChildren.addFirst( child );
            }

            // Internal Activation Callback
            if( callback != null )
            {
                callback.run();
            }

            // After Activation
            targetActivators.afterActivation( target );
            this.targetActivators = targetActivators;
        }
        catch( Throwable e )
        {
            // Passivate actives
            try
            {
                passivate();
            }
            catch( PassivationException e1 )
            {
                throw new ActivationException( "Passivation Exception during cleanup of Activation:" + e1, e );
            }
            if( e instanceof ActivationException )
                throw ((ActivationException)e);
            throw new ActivationException( "Unable to Activate application.", e );
        }
    }

    public void passivate()
        throws PassivationException
    {
        passivate( ( Runnable ) null );
    }

    public void passivate( Runnable callback  )
        throws PassivationException
    {
        List<Exception> exceptions = new ArrayList<Exception>();

        // Before Passivation
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

        // Passivation
        while( !activeChildren.isEmpty() )
        {
            passivateOneChild( exceptions );
        }

        // Internal Passivation Callback
        if( callback != null )
        {
            callback.run();
        }

        // After Passivation
        if ( targetActivators != null )
        {
            try
            {
                targetActivators.afterPassivation( target instanceof ServiceReference
                                                ? new PassiveServiceReference( ( ServiceReference ) target )
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

        // Error handling
        if( exceptions.isEmpty() )
        {
            return;
        }
        throw new PassivationException( exceptions );
    }

    private void passivateOneChild( List<Exception> exceptions )
    {
        Activation activeChild = activeChildren.removeFirst();
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

    private static class PassiveServiceReference
            implements ServiceReference
    {

        private final ServiceReference reference;

        private PassiveServiceReference( ServiceReference reference )
        {
            this.reference = reference;
        }

        @Override
        public String identity()
        {
            return reference.identity();
        }

        @Override
        public Object get()
        {
            throw new IllegalStateException( "Service is passive, either activating and"
                    + " cannot be used yet or passivating and cannot be used anymore." );
        }

        @Override
        public boolean isActive()
        {
            return false;
        }

        @Override
        public boolean isAvailable()
        {
            return false;
        }

        @Override
        public Iterable<Class<?>> types()
        {
            return reference.types();
        }

        @Override
        public <T> T metaInfo( Class<T> infoType )
        {
            return reference.metaInfo( infoType );
        }

        @Override
        public void registerActivationEventListener( ActivationEventListener listener )
        {
            reference.registerActivationEventListener( listener );
        }

        @Override
        public void deregisterActivationEventListener( ActivationEventListener listener )
        {
            reference.deregisterActivationEventListener( listener );
        }

        @Override
        public int hashCode()
        {
            return identity().hashCode();
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( obj == null ) {
                return false;
            }
            if ( getClass() != obj.getClass() ) {
                return false;
            }
            final ServiceReference other = ( ServiceReference ) obj;
            return identity().equals( other.identity() );
        }

    }
}
