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
import java.util.LinkedList;
import java.util.List;

import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.PassivationException;
import org.qi4j.api.service.ServiceReference;

/**
 * This class will manage a set of Activatable instances and their Activators.
 */
public final class ActivationHandler
{
    private LinkedList<Activatable> active = new LinkedList<Activatable>();
    private LinkedList<ActivatorsInstance> activatorsInstances = new LinkedList<ActivatorsInstance>();
    
    public void activate( Object target, ActivatorsInstance targetActivators,  List<? extends Activatable> children )
        throws Exception
    {
        try
        {
            for( Activatable child : children )
            {
                activate( target, targetActivators, child);
            }
        }
        catch( Exception e )
        {
            // Passivate actives
            passivate( target );
            throw e;
        }
        catch( Throwable e )
        {
            // Passivate actives
            passivate( target );
            throw new IllegalStateException( e );
        }
    }

    public void activate( Object target, ActivatorsInstance targetActivators, Activatable child )
            throws Exception
    {
        activate( target, targetActivators, child, null );
    }
    
    public void activate( Object target, ActivatorsInstance targetActivators, Activatable child, Runnable internalActivationCallback )
        throws Exception
    {
        if( !active.contains( child ) )
        {
            targetActivators.beforeActivation( target instanceof ServiceReference
                                               ? new InactiveServiceReference( ( ServiceReference ) target )
                                               : target );
            child.activate();
            if( internalActivationCallback != null )
            {
                internalActivationCallback.run();
            }
            targetActivators.afterActivation( target );
            active.addFirst( child );
            activatorsInstances.addFirst( targetActivators );
        }
    }
    
    public void passivate( Object target )
        throws Exception
    {
        passivate( target, ( Runnable ) null );
    }
    
    public void passivate( Object target, Runnable internalActivationCallback  )
        throws Exception
    {
        ArrayList<Exception> exceptions = new ArrayList<Exception>();
        while( !active.isEmpty() )
        {
            passivate( target, exceptions,internalActivationCallback );
        }
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

    private void passivate( Object target, ArrayList<Exception> exceptions, Runnable internalActivationCallback )
    {
        Activatable activatable = active.removeFirst();
        ActivatorsInstance activators = activatorsInstances.removeFirst();
        try
        {
            activators.beforePassivation( target );
            activatable.passivate();
            if( internalActivationCallback != null )
            {
                internalActivationCallback.run();
            }
            activators.afterPassivation( target instanceof ServiceReference
                                         ? new InactiveServiceReference( ( ServiceReference ) target )
                                         : target );
        }
        catch( Exception e )
        {
            if( e instanceof PassivationException )
            {
                exceptions.addAll( Arrays.asList( ( (PassivationException) e ).causes() ) );
            }
            else
            {
                exceptions.add( e );
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
            throw new IllegalStateException( "Service is not activated and can't be used." );
        }

        public boolean isActive()
        {
            return delegate.isActive();
        }

        public boolean isAvailable()
        {
            throw new IllegalStateException( "Service is not activated and can't be used." );
        }

        public Iterable<Class<?>> types()
        {
            return delegate.types();
        }

        public void registerActivationEventListener( ActivationEventListener listener )
        {
            delegate.registerActivationEventListener( listener );
        }

        public void deregisterActivationEventListener( ActivationEventListener listener )
        {
            delegate.deregisterActivationEventListener( listener );
        }

        public <T> T metaInfo( Class<T> infoType )
        {
            return delegate.metaInfo( infoType );
        }

    }
}
