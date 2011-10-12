package org.qi4j.samples.forum.rest.resource.signup;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.samples.forum.context.signup.Signup;

/**
 * TODO
 */
public class SignupResource
    extends ContextResource
{
    public SignupResource( )
    {
        super( Signup.class );
    }
}
