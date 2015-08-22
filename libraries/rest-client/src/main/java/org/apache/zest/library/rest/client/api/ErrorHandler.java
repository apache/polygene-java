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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.zest.functional.Specifications;
import org.apache.zest.library.rest.client.spi.ResponseHandler;
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
