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

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.library.rest.common.link.Link;
import org.qi4j.library.rest.common.link.LinksUtil;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class LinksResponseWriter
    extends AbstractResponseWriter
{
    private static final List<MediaType> supportedLinkMediaTypes = Arrays.asList( MediaType.APPLICATION_JSON );
    private static final List<MediaType> supportedLinksMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM );

    @Service
    Configuration cfg;

    @Override
    public boolean writeResponse( final Object result, final Response response )
        throws ResourceException
    {
        if( result instanceof Link )
        {
            MediaType type = getVariant( response.getRequest(), ENGLISH, supportedLinkMediaTypes ).getMediaType();
            if( MediaType.APPLICATION_JSON.equals( type ) )
            {
                response.setEntity( new StringRepresentation( ( (Link) result ).toString(), MediaType.APPLICATION_JSON ) );
                return true;
            }
            else
            {
                response.setStatus( Status.REDIRECTION_TEMPORARY );
                Link link = (Link) result;
                Reference reference = new Reference( response.getRequest().getResourceRef(), link.href().get() );
                response.setLocationRef( reference );
                return true;
            }
        }
        else if( result instanceof LinksUtil )
        {
            MediaType type = getVariant( response.getRequest(), ENGLISH, supportedLinksMediaTypes ).getMediaType();
            Representation rep;
            if( MediaType.APPLICATION_JSON.equals( type ) )
            {
                rep = createJsonRepresentation( (LinksUtil) result );
            }
            else if( MediaType.TEXT_HTML.equals( type ) )
            {
                rep = createTextHtmlRepresentation( result, response );
            }
            else if( MediaType.APPLICATION_ATOM.equals( type ) )
            {
                rep = createAtomRepresentation( result, response );
            }
            else
            {
                return false;
            }
            response.setEntity( rep );
            return true;
        }
        return false;
    }

    private StringRepresentation createJsonRepresentation( LinksUtil result )
    {
        return new StringRepresentation( ( (LinksUtil) result ).toString(), MediaType.APPLICATION_JSON );
    }

    private Representation createTextHtmlRepresentation( final Object result, final Response response )
    {
        return new WriterRepresentation( MediaType.TEXT_HTML )
                    {
                        @Override
                        public void write( Writer writer )
                            throws IOException
                        {
                            Map<String, Object> context = new HashMap<String, Object>();
                            context.put( "request", response.getRequest() );
                            context.put( "response", response );
                            context.put( "result", result );
                            try
                            {
                                cfg.getTemplate( "links.htm" ).process( context, writer );
                            }
                            catch( TemplateException e )
                            {
                                throw new IOException( e );
                            }
                        }
                    };
    }

    private Representation createAtomRepresentation( final Object result, final Response response )
    {
        return new WriterRepresentation( MediaType.APPLICATION_ATOM )
        {
            @Override
            public void write( Writer writer )
                throws IOException
            {
                Map<String, Object> context = new HashMap<String, Object>();
                context.put( "request", response.getRequest() );
                context.put( "response", response );
                context.put( "result", result );
                try
                {
                    cfg.getTemplate( "links.atom" ).process( context, writer );
                }
                catch( TemplateException e )
                {
                    throw new IOException( e );
                }
            }
        };
    }
}
