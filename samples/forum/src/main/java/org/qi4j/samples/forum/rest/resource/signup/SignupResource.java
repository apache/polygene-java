package org.qi4j.samples.forum.rest.resource.signup;

import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.samples.forum.context.signup.Registration;
import org.qi4j.samples.forum.context.signup.Signup;
import org.restlet.data.Form;

/**
 * TODO
 */
public class SignupResource
    extends ContextResource
{
    public void signup( Registration registration )
    {
        context( Signup.class ).signup( registration );
    }

    public Form signup()
    {
        Form form = new Form();
        form.set( "name", "Rickard" );
        form.set( "realName", "Rickard Öberg" );
        form.set( "password", "rickard" );
        return form;
    }
}
