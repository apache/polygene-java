package org.qi4j.samples.forum.domain;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

/**
 * TODO
 */
@Mixins(Topic.Mixin.class)
public interface Topic
{
    enum Status
    {
        POSTED,
        MODERATION,
        DELETED,
        SPAM
    }

    void changedSubject( String newSubject );

    Post createdPost(String id);

    void changedStatus( Status status );

    boolean hasStatus(Status status);

    class Mixin
        implements Topic
    {
        @Structure
        Module module;

        @State
        Property<String> subject;

        @State
        Property<Status> status;

        @State
        ManyAssociation<Post> posts;

        @Override
        public void changedSubject( String newSubject )
        {
            subject.set( newSubject );
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

        @Override
        public Post createdPost(String id)
        {
            Post post = module.currentUnitOfWork().newEntity( Post.class, id );
            posts.add( post );
            return post;
        }
    }
}
