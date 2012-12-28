package org.qi4j.samples.forum.rest.resource.login;

import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.samples.forum.context.login.Login;
import org.restlet.Response;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

/**
 * TODO
 */
public class LoginResource
    extends ContextResource
{
    public Representation login( String name, String password )
    {
        context( Login.class ).login( name, password );

        EmptyRepresentation rep = new EmptyRepresentation();
        Response.getCurrent().getCookieSettings().add( "user", name );
        return rep;
    }
}
