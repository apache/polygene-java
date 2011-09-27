package org.qi4j.samples.forum.rest.administration;

import org.qi4j.library.rest.server.api.CommandQueryResource;
import org.qi4j.library.rest.server.api.SubResource;

/**
 * TODO
 */
public class AdministrationResource
    extends CommandQueryResource
{
    @SubResource
    public void forums()
    {
        subResource( ForumsResource.class );
    }
}
