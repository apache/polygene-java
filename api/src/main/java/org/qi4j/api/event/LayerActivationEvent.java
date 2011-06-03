package org.qi4j.api.event;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;

/**
 * Created by IntelliJ IDEA.
 * User: rickard
 * Date: 5/8/11
 * Time: 18:31
 * To change this template use File | Settings | File Templates.
 */
public class LayerActivationEvent
    extends ActivationEvent<Layer>
{
    public LayerActivationEvent( Layer source, EventType type )
    {
        super( source, type );
    }
}
