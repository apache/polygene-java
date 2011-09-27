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
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueDescriptor;
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
public class ValueDescriptorResponseWriter
      extends AbstractResponseWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON );

   private Template formHtmlTemplate;

    private @Structure
    Module module;

   public ValueDescriptorResponseWriter(@Service VelocityEngine velocity) throws Exception
   {
      formHtmlTemplate = velocity.getTemplate( "rest/template/form.htm" );
   }

   public boolean writeResponse( final Object result, final Response response ) throws ResourceException
   {
      if (result instanceof ValueDescriptor)
      {
         MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes).getMediaType();
         if (MediaType.APPLICATION_JSON.equals(type))
         {
            JSONObject json = new JSONObject();

            ValueDescriptor vd = (ValueDescriptor) result;

            try
            {
               for (PropertyDescriptor propertyDescriptor : vd.state().properties())
               {
                  Object o = propertyDescriptor.initialValue(module);
                  if (o == null)
                     json.put( propertyDescriptor.qualifiedName().name(), JSONObject.NULL );
                  else
                     json.put( propertyDescriptor.qualifiedName().name(), o.toString() );
               }
            } catch (JSONException e)
            {
               e.printStackTrace();
            }

            StringRepresentation representation = new StringRepresentation( json.toString(),
                  MediaType.APPLICATION_JSON );

            response.setEntity( representation );

            return true;
         } else if (MediaType.TEXT_HTML.equals(type))
         {
            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  context.put( "result", result );
                  formHtmlTemplate.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;
         }
      }

      return false;
   }
}
