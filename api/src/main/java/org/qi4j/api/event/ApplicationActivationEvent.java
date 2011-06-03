package org.qi4j.api.event;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;

/**
 * Created by IntelliJ IDEA.
 * User: rickard
 * Date: 5/8/11
 * Time: 18:31
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationActivationEvent
    extends ActivationEvent<Application>
{
    public ApplicationActivationEvent( Application source, EventType type )
    {
        super( source, type );
    }
}
