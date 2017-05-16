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

package org.apache.polygene.library.rest.server.restlet.responsewriter;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonException;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.serialization.JsonSerializer;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.TEXT_HTML;

/**
 * JAVADOC
 */
public class ValueDescriptorResponseWriter extends AbstractResponseWriter
{
    private static final List<MediaType> supportedMediaTypes = Arrays.asList( TEXT_HTML, APPLICATION_JSON );

    @Structure
    private ModuleDescriptor module;

    @Service
    private JsonSerializer jsonSerializer;

    @Service
    private JavaxJsonFactories json;

    @Service
    private Configuration cfg;

    @Override
    public boolean writeResponse( final Object result, final Response response )
        throws ResourceException
    {
        if( result instanceof ValueDescriptor )
        {
            MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
            if( APPLICATION_JSON.equals( type ) )
            {
                ValueDescriptor vd = (ValueDescriptor) result;
                JsonObjectBuilder builder = json.builderFactory().createObjectBuilder();
                vd.state().properties().forEach(
                    property ->
                    {
                        try
                        {
                            Object o = property.resolveInitialValue( module );
                            if( o == null )
                            {
                                builder.add( property.qualifiedName().name(), JsonValue.NULL );
                            }
                            else
                            {
                                builder.add( property.qualifiedName().name(), jsonSerializer.toJson( o ) );
                            }
                        }
                        catch( JsonException ex )
                        {
                            throw new RestResponseException( "Unable to serialize " + vd, ex );
                        }
                    } );
                StringRepresentation representation = new StringRepresentation( builder.build().toString(),
                                                                                APPLICATION_JSON );
                response.setEntity( representation );
                return true;
            }
            else if( TEXT_HTML.equals( type ) )
            {
                Representation rep = new WriterRepresentation( TEXT_HTML )
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
