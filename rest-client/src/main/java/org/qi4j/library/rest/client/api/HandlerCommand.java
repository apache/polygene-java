package org.qi4j.library.rest.client.api;

import java.lang.reflect.ParameterizedType;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rest.client.spi.ResponseHandler;
import org.qi4j.library.rest.client.spi.ResultHandler;
import org.qi4j.library.rest.common.link.Link;
import org.restlet.Response;

/**
 * TODO
 */
public abstract class HandlerCommand
{
    public static HandlerCommand refresh()
    {
        return new RefreshCommand();
    }

    public static HandlerCommand query(String relation)
    {
        return new QueryCommand( relation, null);
    }

    public static HandlerCommand query(String relation, Object requestObject)
    {
        return new QueryCommand( relation, requestObject);
    }

    public static HandlerCommand query(Link relation)
    {
        return new QueryLinkCommand( relation );
    }

    public static HandlerCommand command(String relation)
    {
        return new CommandRelationCommand( relation, null );
    }

    public static HandlerCommand command(String relation, Object requestObject)
    {
        return new CommandRelationCommand( relation, requestObject );
    }

    public static HandlerCommand command(Link link)
    {
        return new CommandLinkCommand( link, null );
    }

    public static HandlerCommand command(Link link, Object requestObject)
    {
        return new CommandLinkCommand( link, requestObject );
    }

    public static HandlerCommand delete()
    {
        return new DeleteCommand();
    }

    protected ResponseHandler responseHandler;
    protected ResponseHandler processingErrorHandler;

    public HandlerCommand onSuccess(ResponseHandler responseHandler)
    {
        this.responseHandler = responseHandler;
        return this;
    }

    public <T> HandlerCommand onSuccess(final ResultHandler<T> resultHandler)
    {
        final Class<T> resultType = (Class<T>) Classes.RAW_CLASS.map(( (ParameterizedType) resultHandler.getClass().getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[0]);
        this.responseHandler = new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                T result = client.getContextResourceClientFactory().readResponse( response, resultType );
                return resultHandler.handleResult( result, client );
            }
        };
        return this;
    }

    public HandlerCommand onProcessingError(ResponseHandler processingErrorHandler)
    {
        this.processingErrorHandler = processingErrorHandler;
        return this;
    }

    public <T> HandlerCommand onProcessingError(final ResultHandler<T> resultHandler)
    {
        final Class<T> resultType = (Class<T>) Classes.RAW_CLASS.map(( (ParameterizedType) resultHandler.getClass().getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[0]);
        this.processingErrorHandler = new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                T result = client.getContextResourceClientFactory().readResponse( response, resultType );
                return resultHandler.handleResult( result, client );
            }
        };
        return this;
    }

    abstract HandlerCommand execute( ContextResourceClient client);

    private static class RefreshCommand
        extends HandlerCommand
    {
        @Override
        HandlerCommand execute( ContextResourceClient client )
        {
            return client.refresh();
        }
    }

    private static class QueryCommand
        extends HandlerCommand
    {
        private String relation;
        private Object requestObject;

        private QueryCommand( String relation, Object requestObject)
        {
            this.relation = relation;
            this.requestObject = requestObject;
        }

        @Override
        HandlerCommand execute( ContextResourceClient client )
        {
            return client.query( client.getResource()
                                     .query( relation ), requestObject, responseHandler, processingErrorHandler );
        }
    }

    private static class QueryLinkCommand
        extends HandlerCommand
    {
        private Link link;

        private QueryLinkCommand( Link link)
        {
            this.link = link;
        }

        @Override
        HandlerCommand execute( ContextResourceClient client )
        {
            return client.query( link, null, responseHandler, processingErrorHandler );
        }
    }

    private static class CommandRelationCommand
        extends HandlerCommand
    {
        private String relation;
        private Object requestObject;

        private CommandRelationCommand( String relation, Object requestObject )
        {
            this.relation = relation;
            this.requestObject = requestObject;
        }

        @Override
        HandlerCommand execute( ContextResourceClient client )
        {
            return client.command( client.getResource()
                                       .command( relation ), requestObject, responseHandler, processingErrorHandler );
        }
    }

    private static class CommandLinkCommand
        extends HandlerCommand
    {
        private Link link;
        private Object requestObject;

        private CommandLinkCommand( Link link, Object requestObject )
        {
            this.link = link;
            this.requestObject = requestObject;
        }

        @Override
        HandlerCommand execute( ContextResourceClient client )
        {
            return client.command( link, requestObject, responseHandler, processingErrorHandler );
        }
    }

    private static class DeleteCommand
        extends HandlerCommand
    {
        @Override
        HandlerCommand execute( ContextResourceClient client )
        {
            return client.delete(responseHandler, processingErrorHandler);
        }
    }

}
