package org.qi4j.samples.forum.context.account;

import org.qi4j.samples.forum.domain.User;

/**
 * TODO
 */
public class UpdateProfileContext
{
    Profile profile;

    public UpdateProfileContext bind(User user)
    {
        profile = (Profile) user;
        return this;
    }

    public void updateProfile(String name, String realName, String email)
    {
        profile.updateProfile(name, realName, email);
    }

    interface Profile
        extends User
    {
        void updateProfile( String name, String realName, String email );

        abstract class Mixin
            implements Profile
        {
            @Override
            public void updateProfile( String name, String realName, String email )
            {
                changedName( name );
                changedRealName( realName );
                changedEmail( email );
            }
        }
    }
}
