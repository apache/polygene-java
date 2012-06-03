package org.qi4j.samples.forum.rest.resource.administration;

import org.qi4j.api.query.Query;
import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.samples.forum.data.entity.User;
import org.qi4j.samples.forum.data.entity.Users;

/**
 * TODO
 */
public class UsersResource
    extends ContextResource
    implements ResourceIndex<Query<User>>
{
    @Override
    public Query<User> index()
    {
        return ObjectSelection.current().get( Users.class ).users();
    }
}
