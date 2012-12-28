package org.qi4j.samples.forum.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.entity.Board;
import org.qi4j.samples.forum.data.entity.Forum;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class BoardAdministration
    implements ResourceIndex<Board>
{
    @Structure
    Module module;

    BoardAdmin boardAdmin = new BoardAdmin();

    public BoardAdministration bind( @Uses Forum forum, @Uses Board board, @Uses User user )
    {
        boardAdmin.bind( board );
        return this;
    }

    public Board index()
    {
        return boardAdmin.self();
    }

    public void updateName( @Name( "name" ) String name )
    {
        boardAdmin.updateName( name );
    }

    protected class BoardAdmin
        extends Role<Board>
    {
        @Structure
        Module module;

        public void updateName( String name )
        {
            self().name().set( name );
        }
    }
}
