package org.qi4j.samples.forum.context.view;

import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.entity.Topic;

/**
 * TODO
 */
public class ViewTopic
    implements ResourceIndex<Topic>
{
    private TopicView topicView;

    public ViewTopic bind( Topic topic )
    {
        topicView = (TopicView) topic;
        return this;
    }

    @Override
    public Topic index()
    {
        return topicView.self();
    }

    protected class TopicView
        extends Role<Topic>
    {
    }
}
