package org.qi4j.samples.forum.rest;

import org.qi4j.library.rest.server.api.CommandQueryResource;
import org.qi4j.library.rest.server.api.SubResource;
import org.qi4j.samples.forum.rest.administration.AdministrationResource;
import org.qi4j.samples.forum.rest.forum.ForumResource;

/**
 * TODO
 */
public class RootResource
    extends CommandQueryResource
{
    @SubResource
    void administration()
    {
        subResource( AdministrationResource.class );
    }

    @SubResource
    void forum()
    {
        subResource( ForumResource.class );
    }
}
