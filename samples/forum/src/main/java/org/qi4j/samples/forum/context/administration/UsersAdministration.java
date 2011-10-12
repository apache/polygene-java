package org.qi4j.samples.forum.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.dci.IndexContext;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.entity.Forum;
import org.qi4j.samples.forum.data.entity.Forums;
import org.qi4j.samples.forum.data.entity.User;
import org.qi4j.samples.forum.data.entity.Users;

/**
 * TODO
 */
public class UsersAdministration
    implements IndexContext<Query<User>>
{
    @Structure
    Module module;

    UsersAdmin usersAdmin = new UsersAdmin();
    Administrator administrator = new Administrator();

    public UsersAdministration bind(@Uses Users users, @Uses User user)
    {
        usersAdmin.bind( users );
        administrator.bind( user );
        return this;
    }

    public Query<User> index()
    {
        return usersAdmin.users();
    }

    protected class UsersAdmin
        extends Role<Users>
    {
            public Query<User> users()
            {
                return self.users();
            }
    }

    protected class Administrator
        extends Role<User>
    {
    }
}
