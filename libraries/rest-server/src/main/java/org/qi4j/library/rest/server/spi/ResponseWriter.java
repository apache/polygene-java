package org.qi4j.library.rest.server.spi;

import org.restlet.Response;
import org.restlet.resource.ResourceException;

/**
 * TODO
 */
public interface ResponseWriter
{
    boolean writeResponse( final Object result, final Response response )
        throws ResourceException;
}
