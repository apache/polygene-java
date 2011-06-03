package org.qi4j.runtime.structure;

import org.qi4j.api.event.ActivationEvent;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.event.ActivationEventListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal helper for managing registrations and firing events
 */
public class ActivationEventListenerSupport
    implements ActivationEventListenerRegistration, ActivationEventListener
{
    List<ActivationEventListener> listeners = new ArrayList<ActivationEventListener>(  );

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        List<ActivationEventListener> newListeners = new ArrayList<ActivationEventListener>(  );
        newListeners.addAll( listeners );
        newListeners.add( listener );
        listeners = newListeners;
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        List<ActivationEventListener> newListeners = new ArrayList<ActivationEventListener>(  );
        newListeners.addAll( listeners );
        newListeners.remove( listener );
        listeners = newListeners;
    }

    public void fireEvent(ActivationEvent event)
    {
        for( ActivationEventListener listener : listeners )
        {
            listener.onEvent( event );
        }
    }

    @Override
    public void onEvent( ActivationEvent event )
    {
        fireEvent( event );
    }
}
