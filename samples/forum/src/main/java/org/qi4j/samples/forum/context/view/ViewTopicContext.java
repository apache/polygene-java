package org.qi4j.samples.forum.context.view;

import org.qi4j.api.property.Numbers;
import org.qi4j.samples.forum.domain.Post;
import org.qi4j.samples.forum.domain.Topic;
import org.qi4j.samples.forum.domain.query.TopicView;

/**
 * TODO
 */
public class ViewTopicContext
{
    private TopicView topicView;

    public ViewTopicContext bind( Topic topic )
    {
        topicView = (TopicView) topic;
        return this;
    }

    public Post createdPost()
    {
        Numbers.add( topicView.postCount(), 1);

        Post post = topicView.posts().get( 0 );

        topicView.lastPost().set( post );

        new ViewPostContext().bind( topicView, post ).createdPost( );

        return post;
    }

}
