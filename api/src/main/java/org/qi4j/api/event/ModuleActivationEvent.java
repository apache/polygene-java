package org.qi4j.api.event;

import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;

/**
 * Created by IntelliJ IDEA.
 * User: rickard
 * Date: 5/8/11
 * Time: 18:31
 * To change this template use File | Settings | File Templates.
 */
public class ModuleActivationEvent
    extends ActivationEvent<Module>
{
    public ModuleActivationEvent( Module source, EventType type )
    {
        super( source, type );
    }
}
