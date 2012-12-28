package org.qi4j.samples.forum.domainevent;

import org.qi4j.library.rest.server.spi.CommandResult;
import org.restlet.Request;

/**
 * TODO
 */
public class DomainCommandResult
    implements CommandResult
{
    @Override
    public Object getResult()
    {
        return Request.getCurrent().getAttributes().get( "event" );
    }
}
