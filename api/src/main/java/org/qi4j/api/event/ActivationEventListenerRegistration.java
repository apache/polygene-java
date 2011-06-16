package org.qi4j.api.event;

/**
 * Use this to register listeners for ActivationEvents.
 */
public interface ActivationEventListenerRegistration
{
    void registerActivationEventListener( ActivationEventListener listener );
    void deregisterActivationEventListener( ActivationEventListener listener );
}
