package org.qi4j.samples.forum.context.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.Moderators;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class ModeratorsAdministration
    implements ResourceIndex<Query<User>>
{
    @Structure
    Module module;

    ModeratorsAdmin moderatorsAdmin = new ModeratorsAdmin();

    public ModeratorsAdministration bind( @Uses Moderators moderators )
    {
        moderatorsAdmin.bind( moderators );
        return this;
    }

    public Query<User> index()
    {
        return moderatorsAdmin.moderators();
    }

    public void addModerator( User user )
    {
        moderatorsAdmin.addModerator( user );
    }

    public void removeModerator( User user )
    {
        moderatorsAdmin.removeModerator( user );
    }

    protected class ModeratorsAdmin
        extends Role<Moderators>
    {
        @Structure
        Module module;

        public Query<User> moderators()
        {
            return module.newQueryBuilder( User.class ).newQuery( self.moderators() );
        }

        public void addModerator( User user )
        {
            self().moderators().add( user );
        }

        public void removeModerator( User user )
        {
            self().moderators().remove( user );
        }
    }
}
