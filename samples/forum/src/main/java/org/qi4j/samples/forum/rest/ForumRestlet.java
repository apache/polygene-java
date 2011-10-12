package org.qi4j.samples.forum.rest;

import org.qi4j.library.rest.server.api.ContextRestlet;
import org.qi4j.samples.forum.rest.resource.RootResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;

/**
 * TODO
 */
public class ForumRestlet
    extends ContextRestlet
{
    @Override
    protected Uniform createRoot( Request request, Response response )
    {
        return module.newObject( RootResource.class, this );
    }
}
