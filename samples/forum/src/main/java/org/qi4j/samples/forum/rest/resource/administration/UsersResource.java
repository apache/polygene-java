package org.qi4j.samples.forum.rest.resource.administration;

import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.samples.forum.context.administration.UsersAdministration;

/**
 * TODO
 */
public class UsersResource
    extends ContextResource
{
    public UsersResource( )
    {
        super( UsersAdministration.class );
    }
}
