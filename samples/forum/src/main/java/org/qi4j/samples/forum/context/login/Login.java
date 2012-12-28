package org.qi4j.samples.forum.context.login;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class Login
{
    @Structure
    Module module;

    public void login( @Name( "name" ) String name, @Name( "password" ) String password )
    {
        User user = module.currentUnitOfWork()
            .newQuery( module.newQueryBuilder( User.class )
                           .where( QueryExpressions.eq( QueryExpressions.templateFor( User.class ).name(), name ) ) )
            .find();

        if( user == null || !user.isCorrectPassword( password ) )
        {
            throw new IllegalArgumentException( "Login incorrect" );
        }
    }
}
