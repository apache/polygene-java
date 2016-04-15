/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.library.rest.server.restlet.responsewriter;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.value.ValueDescriptor;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class ValueDescriptorResponseWriter extends AbstractResponseWriter
{
    private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON );

    @Structure
    private ModuleDescriptor module;

    @Service
    private Configuration cfg;

    @Override
    public boolean writeResponse( final Object result, final Response response )
        throws ResourceException
    {
        if( result instanceof ValueDescriptor )
        {
            MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
            if( MediaType.APPLICATION_JSON.equals( type ) )
            {
                JSONObject json = new JSONObject();
                ValueDescriptor vd = (ValueDescriptor) result;
                vd.state().properties().forEach( propertyDescriptor -> {
                    try
                    {
                        Object o = propertyDescriptor.initialValue( module );
                        if( o == null )
                        {
                            json.put( propertyDescriptor.qualifiedName().name(), JSONObject.NULL );
                        }
                        else
                        {
                            json.put( propertyDescriptor.qualifiedName().name(), o.toString() );
                        }
                    }
                    catch( JSONException e )
                    {
                        throw new RestResponseException( "Unable to serialize " + vd, e);
                    }
                } );
                StringRepresentation representation
                    = new StringRepresentation( json.toString(), MediaType.APPLICATION_JSON );
                response.setEntity( representation );
                return true;
            }
            else if( MediaType.TEXT_HTML.equals( type ) )
            {
                Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
                {
                    @Override
                    public void write( Writer writer )
                        throws IOException
                    {
                        Map<String, Object> context = new HashMap<>();
                        context.put( "request", response.getRequest() );
                        context.put( "response", response );
                        context.put( "result", result );
                        try
                        {
                            cfg.getTemplate( "form.htm" ).process( context, writer );
                        }
                        catch( TemplateException e )
                        {
                            throw new IOException( e );
                        }
                    }
                };
                response.setEntity( rep );
                return true;
            }
        }
        return false;
    }
}
