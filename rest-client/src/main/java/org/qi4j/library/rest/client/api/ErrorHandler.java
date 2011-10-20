package org.qi4j.library.rest.client.api;

import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;
import org.qi4j.library.rest.client.spi.ResponseHandler;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * Implements a chained list of specification->handler. Add the most specific handlers first, and the most generic last.
 */
public class ErrorHandler
    implements ResponseHandler
{
    public static Specification<Response> AUTHENTICATION_REQUIRED = new Specification<Response>()
    {
        @Override
        public boolean satisfiedBy( Response item )
        {
            return item.getStatus().equals( Status.CLIENT_ERROR_UNAUTHORIZED );
        }
    };

    public static Specification<Response> RECOVERABLE_ERROR = new Specification<Response>()
    {
        @Override
        public boolean satisfiedBy( Response item )
        {
            return item.getStatus().isRecoverableError();
        }
    };

    LinkedHashMap<Specification<Response>, ResponseHandler> handlers = new LinkedHashMap<Specification<Response>, ResponseHandler>(  );

    public ErrorHandler()
    {
    }

    public ErrorHandler onError(Specification<Response> responseSpecification, ResponseHandler handler)
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
        for( Map.Entry<Specification<Response>, ResponseHandler> specificationResponseHandlerEntry : handlers.entrySet() )
        {
            if (specificationResponseHandlerEntry.getKey().satisfiedBy( response ))
            {
                return specificationResponseHandlerEntry.getValue().handleResponse( response, client );
            }
        }

        // No handlers, throw exception
        throw new ResourceException( response.getStatus() );
    }
}
