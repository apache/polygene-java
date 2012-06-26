package org.qi4j.library.rest.server.spi;

import java.lang.reflect.Method;
import org.restlet.Request;
import org.restlet.resource.ResourceException;

/**
 * TODO
 */
public interface RequestReader
{
    Object[] readRequest( Request request, Method method )
        throws ResourceException;
}
