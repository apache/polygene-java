package org.qi4j.api.event;

/**
 * Use this to register listeners for ActivationEvents. This is implemented by Application, Layer, Module, for example.
 */
public interface ActivationEventListenerRegistration
{
    void registerActivationEventListener( ActivationEventListener listener );
    void deregisterActivationEventListener( ActivationEventListener listener );
}
