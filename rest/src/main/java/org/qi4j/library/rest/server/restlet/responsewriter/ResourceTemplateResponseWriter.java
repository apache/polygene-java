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
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.library.rest.server.restlet.velocity.ValueCompositeContext;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JAVADOC
 */
public class ResourceTemplateResponseWriter
      extends AbstractResponseWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_ATOM );

   @Service VelocityEngine velocity;

   @Service MetadataService metadataService;

   Set<String> skip = new HashSet<String>(  );

   public boolean writeResponse( final Object result, final Response response ) throws ResourceException
   {
      MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
      if (type != null)
      {
         // Try to find template for this specific resource
         StringBuilder templateBuilder = (StringBuilder) response.getRequest().getAttributes().get( "template" );
         String templateName = templateBuilder.toString();

         if (result instanceof ValueDescriptor )
            templateName += "_form";

         final String extension = metadataService.getExtension( type );
         templateName += "."+extension;

         // Have we failed on this one before, then don't try again
         if (skip.contains( templateName ))
            return false;

         try
         {
            final Template template = velocity.getTemplate( templateName );
            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  Object res = result;
                  if (res instanceof ValueComposite)
                  {
                     res = new ValueCompositeContext( (ValueComposite) res );
                  }

                  context.put( "result", res );
                  template.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;

         } catch (Exception e)
         {
            skip.add( templateName );
            // Ignore
         }
      }

      return false;
   }
}
