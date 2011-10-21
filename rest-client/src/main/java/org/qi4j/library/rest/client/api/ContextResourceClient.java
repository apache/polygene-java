/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.client.api;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rest.client.spi.NullResponseHandler;
import org.qi4j.library.rest.client.spi.ResponseHandler;
import org.qi4j.library.rest.client.spi.ResultHandler;
import org.qi4j.library.rest.common.Resource;
import org.qi4j.library.rest.common.link.Link;
import org.qi4j.library.rest.common.link.LinksUtil;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

/**
 * Client-side context resources
 */
public class ContextResourceClient
{
    @Uses
    private ContextResourceClientFactory contextResourceFactory;

    @Uses
    private Reference reference;

    private Resource resource;

    // Response handlers
    private ResponseHandler errorHandler = NullResponseHandler.INSTANCE;
    private ResponseHandler resourceHandler = NullResponseHandler.INSTANCE;
    private ResponseHandler deleteHandler = NullResponseHandler.INSTANCE;
    private Map<String, ResponseHandler> queryHandlers = new HashMap<String, ResponseHandler>(  );
    private Map<String, ResponseHandler> commandHandlers = new HashMap<String, ResponseHandler>(  );
    private Map<String, ResponseHandler> processingErrorHandlers = new HashMap<String, ResponseHandler>();

    // DSL for registering rules
    public ContextResourceClient onError(ResponseHandler handler)
    {
        errorHandler = handler;
        return this;
    }

    public <T> ContextResourceClient onResource( final ResultHandler<T> handler)
    {
        resourceHandler = new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                final Class<T> resultType = (Class<T>) Classes.RAW_CLASS.map(( (ParameterizedType) handler.getClass().getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[0]);
                T result = contextResourceFactory.readResponse( response, resultType );

                if (result instanceof Resource)
                {
                    resource = (Resource) result;
                }

                return handler.handleResult( result, client );
            }
        };
        return this;
    }

    public ContextResourceClient onQuery( String relation, ResponseHandler handler )
    {
        queryHandlers.put( relation, handler );
        return this;
    }

    public <T> ContextResourceClient onQuery( String relation, final ResultHandler<T> handler
    )
    {
        final Class<T> resultType = (Class<T>) Classes.RAW_CLASS.map(( (ParameterizedType) handler.getClass().getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[0]);

        queryHandlers.put( relation,  new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                T result = contextResourceFactory.readResponse( response, resultType );
                return handler.handleResult( result, client );
            }
        });

        return this;
    }

    public ContextResourceClient onCommand( String relation, ResponseHandler handler )
    {
        commandHandlers.put( relation, handler);
        return this;
    }

    public <T> ContextResourceClient onCommand( String relation, final ResultHandler<T> handler )
    {
        final Class<T> resultType = (Class<T>) Classes.RAW_CLASS.map(( (ParameterizedType) handler.getClass().getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[0]);

        commandHandlers.put( relation,  new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                T result = contextResourceFactory.readResponse( response, resultType );
                return handler.handleResult( result, client );
            }
        });

        return this;
    }

    public ContextResourceClient onProcessingError( String relation, ResponseHandler handler )
    {
        processingErrorHandlers.put( relation, handler);
        return this;
    }

    public <T> ContextResourceClient onProcessingError( String relation, final ResultHandler<T> handler)
    {
        final Class<T> resultType = (Class<T>) Classes.RAW_CLASS.map(( (ParameterizedType) handler.getClass().getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[0]);

        processingErrorHandlers.put( relation,  new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                T result = contextResourceFactory.readResponse( response, resultType );
                return handler.handleResult( result, client );
            }
        });

        return this;
    }

    public ContextResourceClient onDelete(ResponseHandler handler)
    {
        deleteHandler = handler;
        return this;
    }

    public ContextResourceClientFactory getContextResourceClientFactory()
    {
        return contextResourceFactory;
    }

    public Reference getReference()
    {
        return reference;
    }

    public Resource getResource()
    {
        return resource;
    }

    public void start()
    {
        HandlerCommand command = refresh();
        while (command != null)
            command = command.execute( this );
    }

    // Callable from HandlerCommand
    HandlerCommand refresh()
    {
        if (resourceHandler == null)
            throw new IllegalStateException( "No handler set for resources" );

        return invokeQuery( reference, null, resourceHandler, null );
    }

    HandlerCommand query( String relation, Object queryRequest, ResponseHandler handler, ResponseHandler processingErrorHandler )
    {
        return query( resource.query( relation ), queryRequest, handler, processingErrorHandler );
    }

    HandlerCommand query( Link link, Object queryRequest, ResponseHandler handler, ResponseHandler processingErrorHandler )
    {
        if (handler == null)
            handler = queryHandlers.get( link.rel().get() );

        if (handler == null)
            throw new IllegalArgumentException( "No handler set for relation "+link.rel().get() );

        if (processingErrorHandler == null)
            processingErrorHandler = processingErrorHandlers.get( link.rel().get() );

        Reference linkRef = new Reference(link.href().get());
        if (linkRef.isRelative())
            linkRef = new Reference( reference.toUri().toString() + link.href().get() );
        return invokeQuery( linkRef, queryRequest, handler, processingErrorHandler );
    }

    private HandlerCommand invokeQuery( Reference ref, Object queryRequest, ResponseHandler resourceHandler, ResponseHandler processingErrorHandler )
    {
        Request request = new Request( Method.GET, ref );

        if( queryRequest != null )
        {
            contextResourceFactory.writeRequest( request, queryRequest );
        }

        contextResourceFactory.updateQueryRequest( request );

        User user = request.getClientInfo().getUser();
        if ( user != null)
            request.setChallengeResponse( new ChallengeResponse( ChallengeScheme.HTTP_BASIC, user.getName(), user.getSecret() ) );

        Response response = new Response( request );

        contextResourceFactory.getClient().handle( request, response );

        if( response.getStatus().isSuccess() )
        {
            contextResourceFactory.updateCache( response );

            return resourceHandler.handleResponse( response, this );
        } else if (response.getStatus().isRedirection())
        {
            Reference redirectedTo = response.getLocationRef();
            return invokeQuery( redirectedTo, queryRequest, resourceHandler, processingErrorHandler );
        } else
        {
            if (response.getStatus().equals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY) && processingErrorHandler != null)
            {
                return processingErrorHandler.handleResponse( response, this );
            } else
            {
                // TODO This needs to be expanded to allow custom handling of all the various cases
                return errorHandler.handleResponse( response, this );
            }
        }
    }

    // Commands
    HandlerCommand command( Link link, Object commandRequest, ResponseHandler handler, ResponseHandler processingErrorHandler )
    {
        if (handler == null)
            handler = commandHandlers.get( link.rel().get() );

        if (processingErrorHandler == null)
            processingErrorHandler = processingErrorHandlers.get( link.rel().get() );

        // Check if we should do POST or PUT
        Method method;
        if( LinksUtil.withClass( "idempotent" ).satisfiedBy( link ) )
        {
            method = Method.PUT;
        }
        else
        {
            method = Method.POST;
        }

        Reference ref = new Reference( reference.toUri().toString() + link.href().get() );
        return invokeCommand( ref, method, commandRequest, handler, processingErrorHandler );
    }

    private HandlerCommand invokeCommand( Reference ref, Method method, Object requestObject, ResponseHandler responseHandler, ResponseHandler processingErrorHandler )
    {
        Request request = new Request( method, ref );

        if (requestObject == null)
            requestObject = new EmptyRepresentation();

        contextResourceFactory.writeRequest( request, requestObject );

        contextResourceFactory.updateCommandRequest( request );

        User user = request.getClientInfo().getUser();
        if ( user != null)
            request.setChallengeResponse( new ChallengeResponse( ChallengeScheme.HTTP_BASIC, user.getName(), user.getSecret() ) );

        Response response = new Response( request );
        contextResourceFactory.getClient().handle( request, response );

        try
        {
            if( response.getStatus().isSuccess() )
            {
                contextResourceFactory.updateCache( response );

                if (responseHandler != null)
                    return responseHandler.handleResponse( response, this );
            }
            else
            {
                if (response.getStatus().equals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY) && processingErrorHandler != null)
                {
                    return processingErrorHandler.handleResponse( response, this );
                } else
                {
                    // TODO This needs to be expanded to allow custom handling of all the various cases
                    return errorHandler.handleResponse( response, this );
                }
            }

            return null; // No handler found
        }
        finally
        {
            try
            {
                response.getEntity().exhaust();
            }
            catch( Throwable e )
            {
                // Ignore
            }
        }
    }

    // Delete
    public HandlerCommand delete( ResponseHandler responseHandler, ResponseHandler processingErrorHandler )
        throws ResourceException
    {
        if (responseHandler == null)
            responseHandler = deleteHandler;

        Request request = new Request( Method.DELETE, new Reference( reference.toUri() ).toString() );
        contextResourceFactory.updateCommandRequest( request );

        int tries = 3;
        while( true )
        {
            Response response = new Response( request );
            try
            {
                contextResourceFactory.getClient().handle( request, response );
                if( !response.getStatus().isSuccess() )
                {
                    return errorHandler.handleResponse( response, this );
                }
                else
                {
                    // Reset modification date
                    contextResourceFactory.updateCache( response );

                    return responseHandler.handleResponse( response, this );
                }
            }
            catch( ResourceException e )
            {
                if( e.getStatus().equals( Status.CONNECTOR_ERROR_COMMUNICATION ) ||
                    e.getStatus().equals( Status.CONNECTOR_ERROR_CONNECTION ) )
                {
                    if( tries == 0 )
                    {
                        throw e; // Give up
                    }
                    else
                    {
                        // Try again
                        tries--;
                        continue;
                    }
                }
                else
                {
                    // Abort
                    throw e;
                }
            }
            finally
            {
                try
                {
                    response.getEntity().exhaust();
                }
                catch( Throwable e )
                {
                    // Ignore
                }
            }
        }
    }

    // Browse to other resources
    public synchronized ContextResourceClient newClient( Link link )
    {
        if( link == null )
        {
            throw new NullPointerException( "No link specified" );
        }

        return newClient( link.href().get() );
    }

    public synchronized ContextResourceClient newClient( String relativePath )
    {
        if( relativePath.startsWith( "http://" ) )
        {
            return contextResourceFactory.newClient( new Reference( relativePath ) );
        }

        Reference reference = this.reference.clone();
        if( relativePath.startsWith( "/" ) )
        {
            reference.setPath( relativePath );
        }
        else
        {
            reference.setPath( reference.getPath() + relativePath );
            reference = reference.normalize();
        }

        return contextResourceFactory.newClient( reference );
    }

    // Internal
    private Object handlxeError( Response response )
        throws ResourceException
    {
        if( response.getStatus().equals( Status.SERVER_ERROR_INTERNAL ) )
        {
            if( MediaType.APPLICATION_JAVA_OBJECT.equals( response.getEntity().getMediaType() ) )
            {
                try
                {
                    Object exception = new ObjectRepresentation( response.getEntity() ).getObject();
                    throw new ResourceException( (Throwable) exception );
                }
                catch( IOException e )
                {
                    throw new ResourceException( e );
                }
                catch( ClassNotFoundException e )
                {
                    throw new ResourceException( e );
                }
            }

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, response.getEntityAsText() );
        }
        else
        {
            if( response.getEntity() != null )
            {
                String text = response.getEntityAsText();
                throw new ResourceException( response.getStatus().getCode(), response.getStatus()
                    .getName(), text, response.getRequest().getResourceRef().toUri().toString() );
            }
            else
            {
                throw new ResourceException( response.getStatus().getCode(), response.getStatus()
                    .getName(), response.getStatus().getDescription(), response.getRequest()
                    .getResourceRef()
                    .toUri()
                    .toString() );
            }
        }
    }

    @Override
    public String toString()
    {
        return reference.toString();
    }
}