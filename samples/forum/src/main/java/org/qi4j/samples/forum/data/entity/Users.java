package org.qi4j.samples.forum.data.entity;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.data.Administrators;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * TODO
 */
@Mixins(Users.Mixin.class)
public interface Users
    extends EntityComposite
{
    String USERS_ID = "users";

    public Query<User> users();

    abstract class Mixin
        implements Users
    {
        @Structure
        Module module;

        public Query<User> users()
        {
            return module.currentUnitOfWork().newQuery( module.newQueryBuilder( User.class ) ).orderBy( templateFor( User.class ).realName()  );
        }
    }
}
