/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.library.rest.client.api;

import java.lang.reflect.ParameterizedType;
import org.apache.zest.api.util.Classes;
import org.apache.zest.library.rest.client.spi.ResponseHandler;
import org.apache.zest.library.rest.client.spi.ResultHandler;
import org.apache.zest.library.rest.common.link.Link;
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
        final Class<T> resultType = (Class<T>) Classes.RAW_CLASS.apply(( (ParameterizedType) resultHandler.getClass().getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[0]);
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
        final Class<T> resultType = (Class<T>) Classes.RAW_CLASS.apply(( (ParameterizedType) resultHandler.getClass().getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[0]);
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
