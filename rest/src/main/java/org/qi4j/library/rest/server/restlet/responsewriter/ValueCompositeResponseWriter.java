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
import org.qi4j.library.rest.server.restlet.velocity.ValueCompositeContext;
import org.restlet.Response;
import org.restlet.data.MediaType;
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
public class ValueCompositeResponseWriter
      extends AbstractResponseWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON );
   private Template valueHtmlTemplate;
   private final VelocityEngine velocity;

   public ValueCompositeResponseWriter(@Service VelocityEngine velocity) throws Exception
   {
      this.velocity = velocity;
      valueHtmlTemplate = velocity.getTemplate( "rest/template/value.htm" );
   }

   public boolean writeResponse( final Object result, final Response response ) throws ResourceException
   {
      if (result instanceof ValueComposite)
      {
         MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
         if (MediaType.APPLICATION_JSON.equals(type))
         {
            StringRepresentation representation = new StringRepresentation( result.toString(),
                  MediaType.APPLICATION_JSON );

            response.setEntity( representation );
            return true;
         } else if (MediaType.TEXT_HTML.equals(type))
         {
            // Look for type specific template
            Template template;
            try
            {
               template = velocity.getTemplate( "rest/template/" + result.getClass().getInterfaces()[0].getSimpleName() + ".htm" );
            } catch (Exception e)
            {
               // Use default
               template = valueHtmlTemplate;
            }


            final Template finalTemplate = template;
            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  context.put( "result", new ValueCompositeContext( (ValueComposite) result) );
                  finalTemplate.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;
         }
      }

      return false;
   }
}
