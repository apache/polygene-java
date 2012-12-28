package org.qi4j.samples.forum.context.view;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.Moderators;
import org.qi4j.samples.forum.data.entity.Board;
import org.qi4j.samples.forum.data.entity.Forum;
import org.qi4j.samples.forum.data.entity.Post;
import org.qi4j.samples.forum.data.entity.Topic;
import org.qi4j.samples.forum.data.entity.User;

import static org.qi4j.api.property.Numbers.add;

/**
 * TODO
 */
public class ViewBoard
    implements ResourceIndex<Board>
{
    private Posting posting;
    private BoardViewer viewer;
    private BoardModeration boardModeration;
    private ForumModeration forumModeration;

    public ViewBoard bind( @Uses Forum forum, @Uses Board board, @Uses User user )
    {
        posting.bind( board );
        viewer.bind( user );
        boardModeration.bind( board );
        forumModeration.bind( forum );
        return this;
    }

    @Override
    public Board index()
    {
        return posting.self();
    }

    public Topic createTopic( String subject, String message )
    {
        return posting.createTopic( subject, message, viewer );
    }

    protected class BoardViewer
        extends Role<User>
    {
        boolean isModerator( Moderators moderators )
        {
            return moderators.moderators().contains( self );
        }
    }

    protected class BoardModeration
        extends Role<Moderators>
    {
        boolean isModerator( BoardViewer user )
        {
            return user.isModerator( self ) || forumModeration.isModerator( user );
        }
    }

    protected class ForumModeration
        extends Role<Moderators>
    {
        boolean isModerator( BoardViewer user )
        {
            return user.isModerator( self );
        }
    }

    protected class Posting
        extends Role<Board>
    {
        @Structure
        Module module;

        Topic createTopic( String subject, String message, BoardViewer poster )
        {
            Topic topic = module.currentUnitOfWork().newEntity( Topic.class );

            topic.subject().set( subject );

            Post post = module.currentUnitOfWork().newEntity( Post.class );
            post.message().set( message );

            // Moderation checks
            if( boardModeration.isModerator( poster ) )
            {
                post.status().set( Post.Status.POSTED );

                self.lastPost().set( post );
                add( self.topicCount(), 1 );
                add( self.postCount(), 1 );

                return topic;
            }
            else
            {
                post.status().set( Post.Status.MODERATION );
            }

            return topic;
        }
    }
}
