package org.qi4j.samples.forum.context.signup;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.samples.forum.context.Context;
import org.qi4j.samples.forum.context.Events;
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

    @Service
    Events events;

    public void signup(Registration registration)
    {
        users.signup( registration );
    }

    protected class Users
        implements TransientComposite
    {
        public void signup(Registration registration)
        {
            // Check if user with this name already exists
            events.signedup( registration );
        }
    }
}
