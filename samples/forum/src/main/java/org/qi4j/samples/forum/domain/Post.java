package org.qi4j.samples.forum.domain;

import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
@Mixins(Post.Mixin.class)
public interface Post
{
    enum Status
    {
        POSTED,
        MODERATION,
        DELETED,
        SPAM
    }

    void changedReplyTo(Post replyTo);

    void changedMessage( String message );

    void changedPoster(User user);

    void changedStatus( Status status );

    boolean hasStatus(Status status);

    class Mixin
        implements Post
    {
        @State
        @Optional
        Association<Post> replyTo;

        @State
        Property<String> message;

        @State
        Association<User> poster;

        @State
        Property<Status> status;

        @Override
        public void changedReplyTo( Post replyTo )
        {
            this.replyTo.set( replyTo );
        }

        @Override
        public void changedMessage( String message )
        {
            this.message.set( message );
        }

        @Override
        public void changedPoster( User user )
        {
            this.poster.set( user );
        }

        @Override
        public void changedStatus( Status status )
        {
            this.status.set( status );
        }

        @Override
        public boolean hasStatus( Status status )
        {
            return this.status.get().equals( status );
        }
    }
}
