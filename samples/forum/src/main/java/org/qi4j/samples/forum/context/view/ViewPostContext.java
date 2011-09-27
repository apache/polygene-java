package org.qi4j.samples.forum.context.view;

import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.domain.*;
import org.qi4j.samples.forum.domain.query.PostView;

import java.util.Date;

/**
 * TODO
 */
public class ViewPostContext
{
    ViewPost viewPost;
    ReplyTopic replyTopic;

    public ViewPostContext bind(Topic topic, Post post)
    {
        viewPost = (ViewPost) post;
        replyTopic = (ReplyTopic) topic;
        return this;
    }

    public void createdPost()
    {
        viewPost.created();
    }

    public Post reply(String message)
    {
        return replyTopic.reply( message, viewPost );
    }

    interface ReplyTopic
        extends Topic
    {
        Post reply( String message, ViewPost viewPost );

        abstract class Mixin
            implements ReplyTopic
        {
            @Service
            IdentityGenerator identityGenerator;

            @Override
            public Post reply( String message, ViewPost viewPost )
            {
                Post post = createdPost( identityGenerator.generate( Post.class ) );
                post.changedMessage( message );
                post.changedReplyTo( viewPost );
                ((ViewPost)post).created();
                return post;
            }
        }
    }

    interface ViewPost
        extends PostView
    {
        void created();

        abstract class Mixin
            implements ViewPost
        {
            @Structure
            Module module;

            @Override
            public void created()
            {
                createdBy().set( module.currentUnitOfWork().metaInfo().get( User.class ) );
                createdOn().set( new Date(module.currentUnitOfWork().currentTime()) );
            }
        }
    }
}
