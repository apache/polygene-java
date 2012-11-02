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

package org.qi4j.library.rest.server.restlet.responsewriter;

import java.util.Arrays;
import java.util.List;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

/**
 * Handles simple types and serialize to JSON
 */
public class DefaultResponseWriter
    extends AbstractResponseWriter
{
    private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.APPLICATION_JSON );

    @Override
    public boolean writeResponse( final Object result, final Response response )
        throws ResourceException
    {
        MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
        if( MediaType.APPLICATION_JSON.equals( type ) )
        {
            if( result instanceof String || result instanceof Number || result instanceof Boolean )
            {
                StringRepresentation representation = new StringRepresentation( result.toString(),
                                                                                MediaType.APPLICATION_JSON );

                response.setEntity( representation );

                return true;
            }
        }

        return false;
    }
}
