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
import freemarker.template.Template;
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
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

/**
 * Handles Restlet Form
 */
public class FormResponseWriter
    extends AbstractResponseWriter
{
    private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML,
                                                                              MediaType.APPLICATION_JSON );

    @Service
    private Configuration cfg;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Override
    public boolean writeResponse( final Object result, final Response response )
        throws ResourceException
    {
        if( result instanceof Form )
        {
            MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
            if( MediaType.APPLICATION_JSON.equals( type ) )
            {
                JsonObjectBuilder builder = jsonFactories.builderFactory().createObjectBuilder();
                Form form = (Form) result;
                try
                {
                    for( Parameter parameter : form )
                    {
                        String value = parameter.getValue();
                        if( value == null )
                        {
                            builder.add( parameter.getName(), JsonValue.NULL );
                        }
                        else
                        {
                            builder.add( parameter.getName(), value );
                        }
                    }
                }
                catch( JsonException e )
                {
                    e.printStackTrace();
                }

                StringRepresentation representation = new StringRepresentation( builder.build().toString(),
                                                                                MediaType.APPLICATION_JSON );
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
                        Map<String, Object> root = new HashMap<>();
                        root.put( "request", response.getRequest() );
                        root.put( "response", response );
                        root.put( "result", result );
                        try
                        {
                            Template formHtmlTemplate = cfg.getTemplate( "form.htm" );
                            formHtmlTemplate.process( root, writer );
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
