package org.qi4j.samples.forum.context.view;

import java.util.Date;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Numbers;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.entity.Post;
import org.qi4j.samples.forum.data.entity.Topic;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class ViewPost
    implements ResourceIndex<Post>
{
    PostView viewPost = new PostView();
    ReplyTopic replyTopic = new ReplyTopic();
    Poster poster = new Poster();

    public ViewPost bind( @Uses Topic topic, @Uses Post post, @Uses User user )
    {
        viewPost.bind( post );
        replyTopic.bind( topic );
        poster.bind( user );
        return this;
    }

    @Override
    public Post index()
    {
        return viewPost.self();
    }

    public Post reply( String message )
    {
        return replyTopic.reply( message, viewPost );
    }

    protected class ReplyTopic
        extends Role<Topic>
    {
        @Structure
        Module module;

        public Post reply( String message, PostView viewPost )
        {
            Post post = module.currentUnitOfWork().newEntity( Post.class );
            post.message().set( message );
            post.createdBy().set( poster.self() );
            post.createdOn().set( new Date( module.currentUnitOfWork().currentTime() ) );
            post.replyTo().set( viewPost.self() );

            self().lastPost().set( post );
            Numbers.add( self().postCount(), 1 );

            return post;
        }
    }

    protected class PostView
        extends Role<Post>
    {
    }

    protected class Poster
        extends Role<User>
    {
    }
}
