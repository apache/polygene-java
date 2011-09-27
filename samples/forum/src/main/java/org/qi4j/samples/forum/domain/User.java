package org.qi4j.samples.forum.domain;

import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
@Mixins(User.Mixin.class)
public interface User
{
    void changedName(String newName);

    void changedRealName(String newRealName);

    void changedEmail(String newEmail);

    class Mixin
        implements User
    {
        @State
        public Property<String> name;

        @State
        public Property<String> realName;

        @State
        public Property<String> email;

        @Override
        public void changedName( String newName )
        {
            name.set( newName );
        }

        @Override
        public void changedRealName( String newRealName )
        {
            realName.set( newRealName );
        }

        @Override
        public void changedEmail( String newEmail )
        {
            email.set( newEmail );
        }
    }
}
