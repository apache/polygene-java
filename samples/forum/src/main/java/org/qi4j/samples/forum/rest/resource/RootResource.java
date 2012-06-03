package org.qi4j.samples.forum.rest.resource;

import java.util.Collections;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.SubResource;
import org.qi4j.library.rest.server.api.constraint.Requires;
import org.qi4j.samples.forum.data.entity.User;
import org.qi4j.samples.forum.data.entity.Users;
import org.qi4j.samples.forum.rest.resource.administration.AdministrationResource;
import org.qi4j.samples.forum.rest.resource.forum.ForumResource;
import org.qi4j.samples.forum.rest.resource.signup.SignupResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import static org.qi4j.library.rest.server.api.ObjectSelection.current;

/**
 * TODO
 */
public class RootResource
    extends ContextResource
{
    @SubResource
    public void signup()
    {
        select( Users.class, Users.USERS_ID );
        subResource( SignupResource.class );
    }

    @SubResource
    public void administration()
    {
        ChallengeResponse challenge = Request.getCurrent().getChallengeResponse();
        if( challenge == null )
        {
            Response.getCurrent()
                .setChallengeRequests( Collections.singletonList( new ChallengeRequest( ChallengeScheme.HTTP_BASIC, "Forum" ) ) );
            throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED );
        }

        User user = module.currentUnitOfWork().newQuery( module.newQueryBuilder( User.class ).where( QueryExpressions
                                                                                                         .eq( QueryExpressions
                                                                                                                  .templateFor( User.class )
                                                                                                                  .name(), challenge
                                                                                                             .getIdentifier() ) ) )
            .find();
        if( user == null || !user.isCorrectPassword( new String( challenge.getSecret() ) ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED );
        }

        current().select( user );

        subResource( AdministrationResource.class );
    }

    @SubResource
    @Requires( User.class )
    public void forum()
    {
        subResource( ForumResource.class );
    }
}
