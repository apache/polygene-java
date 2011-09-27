package org.qi4j.samples.forum.context.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.domain.Forum;
import org.qi4j.samples.forum.domain.User;

import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
public class ForumAdministrationContext
{
    @Structure
    Module module;

    public ForumAdministrationContext bind(User user)
    {
        return this;
    }

    public Query<Forum> forums()
    {
        return module.currentUnitOfWork().newQuery( module.newQueryBuilder( Forum.class ) ).orderBy( templateFor( Forum.Mixin.class ).name  );
    }

    public Forum createForum(String name)
    {
        Forum forum = module.currentUnitOfWork().newEntity( Forum.class );
        forum.changedName( name );
        return forum;
    }
}
