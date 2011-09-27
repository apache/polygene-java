package org.qi4j.samples.forum.context.view;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.domain.User;
import org.qi4j.samples.forum.domain.query.BoardView;
import org.qi4j.samples.forum.domain.query.ForumView;

/**
 * TODO
 */
public class ViewForumContext
{
    private ViewForum viewForum;

    public ViewForumContext bind(ForumView forum, User user)
    {
        viewForum = (ViewForum) forum;
        return this;
    }

    public Query<BoardView> boards()
    {
        return viewForum.boards();
    }

    interface ViewForum
        extends ForumView
    {
        Query<BoardView> boards();

        abstract class Mixin
            implements ViewForum
        {
            @Structure
            Module module;

            @State
            ManyAssociation<BoardView> boards;

            public Query<BoardView> boards()
            {
                return module.newQueryBuilder( BoardView.class ).newQuery( boards );
            }
        }
    }
}
