/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.rest.common.link.Link;
import org.qi4j.library.rest.common.link.Links;
import org.qi4j.library.rest.server.restlet.velocity.ValueCompositeContext;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * JAVADOC
 */
public class LinksResponseWriter
   extends AbstractResponseWriter
{
   private static final List<MediaType> supportedLinkMediaTypes = Arrays.asList( MediaType.APPLICATION_JSON);
   private static final List<MediaType> supportedLinksMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM );
   private Template linksHtmlTemplate;
   private Template linksAtomTemplate;

   public LinksResponseWriter(@Service VelocityEngine velocity) throws Exception
   {
      linksHtmlTemplate = velocity.getTemplate( "rest/template/links.htm" );
      linksAtomTemplate = velocity.getTemplate( "rest/template/links.atom" );
   }

   public boolean writeResponse( final Object result, final Response response ) throws ResourceException
   {
      if (result instanceof Link )
      {
         MediaType type = getVariant( response.getRequest(), ENGLISH, supportedLinkMediaTypes ).getMediaType();
         if (MediaType.APPLICATION_JSON.equals(type))
         {
            response.setEntity( new StringRepresentation(((Link) result).toString(), MediaType.APPLICATION_JSON));
            return true;
         } else
         {
            response.setStatus( Status.REDIRECTION_TEMPORARY );
            Link link = (Link) result;
            Reference reference = new Reference( response.getRequest().getResourceRef(), link.href().get() );

            response.setLocationRef( reference );
            return true;
         }
      } else if (result instanceof Links )
      {
         MediaType type = getVariant( response.getRequest(), ENGLISH, supportedLinksMediaTypes ).getMediaType();
         if (MediaType.APPLICATION_JSON.equals( type ))
         {
            response.setEntity( new StringRepresentation(((Links) result).toString(), MediaType.APPLICATION_JSON));
            return true;
         } else if (MediaType.TEXT_HTML.equals( type ))
         {
            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  context.put( "result", new ValueCompositeContext((ValueComposite) result) );
                  linksHtmlTemplate.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;
         } else if (MediaType.APPLICATION_ATOM.equals( type ))
         {
            Representation rep = new WriterRepresentation( MediaType.APPLICATION_ATOM )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  context.put( "result", new ValueCompositeContext((ValueComposite) result) );
                  linksAtomTemplate.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;
         }
      }

      return false;
   }
}
