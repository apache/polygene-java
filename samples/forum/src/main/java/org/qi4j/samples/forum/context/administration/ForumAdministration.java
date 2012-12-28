package org.qi4j.samples.forum.context.administration;

import org.qi4j.api.constraint.Name;
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
public class ForumAdministration
    implements ResourceIndex<Query<Board>>
{
    @Structure
    Module module;

    ForumAdmin forumAdmin = new ForumAdmin();
    Administrator administrator = new Administrator();

    public ForumAdministration bind( @Uses Forum forum, @Uses User user )
    {
        forumAdmin.bind( forum );
        administrator.bind( user );
        return this;
    }

    public Query<Board> index()
    {
        return forumAdmin.boards();
    }

    public Board createBoard( @Name( "name" ) String name )
    {
        return forumAdmin.createBoard( name );
    }

    protected class ForumAdmin
        extends Role<Forum>
    {
        @Structure
        Module module;

        public Query<Board> boards()
        {
            return module.newQueryBuilder( Board.class ).newQuery( self.boards() );
        }

        public Board createBoard( String name )
        {
            Board board = module.currentUnitOfWork().newEntity( Board.class );
            board.name().set( name );
            administrator.makeModerator( board );
            return board;
        }
    }

    protected class Administrator
        extends Role<User>
    {
        public void makeModerator( Board board )
        {
            board.moderators().add( self );
        }
    }
}
