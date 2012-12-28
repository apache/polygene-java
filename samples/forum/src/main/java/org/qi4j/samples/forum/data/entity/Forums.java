package org.qi4j.samples.forum.data.entity;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.data.Administrators;

import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
@Mixins( Forums.Mixin.class )
public interface Forums
    extends Administrators, EntityComposite
{
    String FORUMS_ID = "forums";

    public Query<Forum> forums();

    abstract class Mixin
        implements Forums
    {
        @Structure
        Module module;

        public Query<Forum> forums()
        {
            return module.currentUnitOfWork()
                .newQuery( module.newQueryBuilder( Forum.class ) )
                .orderBy( templateFor( Forum.class ).name() );
        }
    }
}
