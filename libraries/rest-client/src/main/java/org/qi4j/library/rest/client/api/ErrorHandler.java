package org.qi4j.library.rest.client.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.qi4j.functional.Specifications;
import org.qi4j.library.rest.client.spi.ResponseHandler;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * Implements a chained list of specification-&gt;handler. Add the most specific handlers first, and the most generic last.
 */
public class ErrorHandler
    implements ResponseHandler
{
    public static Predicate<Response> AUTHENTICATION_REQUIRED = new Predicate<Response>()
    {
        @Override
        public boolean test( Response item )
        {
            return item.getStatus().equals( Status.CLIENT_ERROR_UNAUTHORIZED );
        }
    };

    public static Predicate<Response> RECOVERABLE_ERROR = new Predicate<Response>()
    {
        @Override
        public boolean test( Response item )
        {
            return item.getStatus().isRecoverableError();
        }
    };

    LinkedHashMap<Predicate<Response>, ResponseHandler> handlers = new LinkedHashMap<Predicate<Response>, ResponseHandler>(  );

    public ErrorHandler()
    {
    }

    public ErrorHandler onError(Predicate<Response> responseSpecification, ResponseHandler handler)
    {
        handlers.put( responseSpecification, handler );
        return this;
    }

    public ErrorHandler onError(ResponseHandler handler)
    {
        handlers.put( Specifications.<Response>TRUE(), handler );
        return this;
    }

    @Override
    public HandlerCommand handleResponse( Response response, ContextResourceClient client )
    {
        for( Map.Entry<Predicate<Response>, ResponseHandler> specificationResponseHandlerEntry : handlers.entrySet() )
        {
            if (specificationResponseHandlerEntry.getKey().test( response ))
            {
                return specificationResponseHandlerEntry.getValue().handleResponse( response, client );
            }
        }

        // No handlers, throw exception
        throw new ResourceException( response.getStatus() );
    }
}
