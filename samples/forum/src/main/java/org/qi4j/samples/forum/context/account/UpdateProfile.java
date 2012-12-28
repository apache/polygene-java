package org.qi4j.samples.forum.context.account;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.context.Context;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class UpdateProfile
    extends Context
    implements ResourceIndex<UpdateProfile.Profile>
{
    Profile profile;

    public UpdateProfile bind( @Uses User user )
    {
        profile = role( Profile.class, user );
        return this;
    }

    public Profile index()
    {
        return profile;
    }

    public void updateProfile( @Name( "name" ) String name,
                               @Name( "realname" ) String realName,
                               @Name( "email" ) String email
    )
    {
        profile.updateProfile( name, realName, email );
    }

    protected class Profile
        extends Role<User>
    {
        public void updateProfile( String name, String realName, String email )
        {
            self.name().set( name );
            self.realName().set( realName );
            self.email().set( email );
        }
    }
}
