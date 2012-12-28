package org.qi4j.samples.forum.context.view;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.entity.Board;
import org.qi4j.samples.forum.data.entity.Forum;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class ViewForum
    implements ResourceIndex<Query<Board>>
{
    private ForumView viewForum = new ForumView();

    public org.qi4j.samples.forum.context.view.ViewForum bind( @Uses Forum forum, @Uses User user )
    {
        viewForum.bind( forum );
        return this;
    }

    public Query<Board> index()
    {
        return viewForum.boards();
    }

    protected class ForumView
        extends Role<Forum>
    {
        @Structure
        Module module;

        public Query<Board> boards()
        {
            return module.newQueryBuilder( Board.class ).newQuery( self.boards() );
        }
    }
}
