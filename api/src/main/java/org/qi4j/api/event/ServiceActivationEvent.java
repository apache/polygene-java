package org.qi4j.api.event;

import org.qi4j.api.service.ServiceReference;

/**
 * Created by IntelliJ IDEA.
 * User: rickard
 * Date: 5/8/11
 * Time: 18:31
 * To change this template use File | Settings | File Templates.
 */
public class ServiceActivationEvent
    extends ActivationEvent<ServiceReference>
{
    public ServiceActivationEvent( ServiceReference source, EventType type )
    {
        super( source, type );
    }
}
