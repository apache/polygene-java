package org.qi4j.samples.forum.context.view;

import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Numbers;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.domain.*;
import org.qi4j.samples.forum.domain.query.BoardView;

/**
 * TODO
 */
public class ViewBoardContext
    implements Moderation
{
    private Posting posting;
    private BoardViewer viewer;

    public ViewBoardContext bind(Board board, User user)
    {
        this.viewer = (BoardViewer) user;
        posting = (Posting) board;
        return this;
    }

    @Override
    public boolean isModerator( User user )
    {
        return true;
    }

    public Topic createTopic( String subject, String message)
    {
        Topic topic = posting.createTopic( subject, message, viewer, this);

        Post post = new ViewTopicContext().bind(topic).createdPost();

        if (topic.hasStatus( Topic.Status.POSTED ))
        {
            Numbers.add( posting.topicCount(), 1 );
            Numbers.add( posting.postCount(), 1 );
            posting.lastPost().set( post );
        }

        return topic;
    }

    interface BoardViewer
        extends User
    {
    }

    @Mixins(Posting.Mixin.class)
    interface Posting
        extends BoardView
    {
        Topic createTopic( String subject, String message, BoardViewer poster, Moderation moderation );

        abstract class Mixin
            implements Posting
        {
            @Structure
            Module module;

            @Service
            IdentityGenerator identityGenerator;

            @This
            Board board;

            @Override
            public Topic createTopic( String subject, String message, BoardViewer poster, Moderation moderation )
            {
                Topic topic = createdTopic( identityGenerator.generate( Topic.class ) );

                topic.changedSubject( subject );

                Post post = topic.createdPost(identityGenerator.generate( Post.class ));
                post.changedMessage( message );

                // Moderation checks
                if (moderation.isModerator( poster ))
                {
                    post.changedStatus( Post.Status.MODERATION );
                } else
                {
                    post.changedStatus( Post.Status.POSTED );
                }

                return topic;
            }
        }
    }
}
