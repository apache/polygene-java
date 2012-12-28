package org.qi4j.samples.forum.rest.resource.administration;

import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.SubResource;
import org.qi4j.samples.forum.data.entity.Forums;
import org.qi4j.samples.forum.data.entity.Users;

/**
 * TODO
 */
public class AdministrationResource
    extends ContextResource
{
    @SubResource
    public void forums()
    {
        select( Forums.class, Forums.FORUMS_ID );
        subResource( ForumsResource.class );
    }

    @SubResource
    public void users()
    {
        select( Users.class, Users.USERS_ID );
        subResource( UsersResource.class );
    }
}
