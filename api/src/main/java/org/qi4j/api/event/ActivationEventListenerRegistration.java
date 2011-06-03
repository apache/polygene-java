package org.qi4j.api.event;

/**
 * Created by IntelliJ IDEA.
 * User: rickard
 * Date: 5/8/11
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public interface ActivationEventListenerRegistration
{
    void registerActivationEventListener( ActivationEventListener listener );
    void deregisterActivationEventListener( ActivationEventListener listener );
}
