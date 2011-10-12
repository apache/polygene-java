package org.qi4j.samples.forum.context.signup;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.samples.forum.context.Context;
import org.qi4j.samples.forum.data.entity.User;
import org.qi4j.samples.forum.domainevent.DomainEvent;

/**
 * TODO
 */
public class Signup
    extends Context
{
    @Uses
    Users users;

    public void signup(Registration registration)
    {
        users.signedUp( registration );
    }

    protected class Users
        implements TransientComposite
    {
        public void signup(Registration registration)
        {
            // Check if user with this name already exists
            signedUp( registration );
        }

        @DomainEvent
        public void signedUp(Registration registration)
        {
            EntityBuilder<User> builder = module.currentUnitOfWork().newEntityBuilder( User.class );
            builder.instance().name().set( registration.name().get() );
            builder.instance().realName().set( registration.realName().get() );
            builder.instance().email().set( registration.email().get() );
            builder.instance().password().set( builder.instance().hashPassword(registration.password().get() ));

            builder.newInstance();
        }
    }
}
