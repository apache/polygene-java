package org.qi4j.samples.forum.rest.resource.forum;

import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.SubResources;
import org.qi4j.samples.forum.data.entity.Forum;
import org.restlet.resource.ResourceException;

/**
 * TODO
 */
public class ForumsResource
    extends ContextResource
    implements SubResources
{
    @Override
    public void resource( String segment )
        throws ResourceException
    {
        select( Forum.class, segment );
        subResource( ForumResource.class );
    }
}
