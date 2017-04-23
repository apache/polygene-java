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
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.serialization.Serializer;
import org.apache.polygene.library.rest.common.table.Cell;
import org.apache.polygene.library.rest.common.table.Column;
import org.apache.polygene.library.rest.common.table.Row;
import org.apache.polygene.library.rest.common.table.Table;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.serialization.JsonSerializer;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class TableResponseWriter extends AbstractResponseWriter
{
    private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML,
                                                                              MediaType.APPLICATION_JSON );

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
        if( result instanceof Table )
        {
            MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
            if( MediaType.APPLICATION_JSON.equals( type ) )
            {
                response.setEntity( new WriterRepresentation( MediaType.APPLICATION_JSON )
                {
                    @Override
                    public void write( Writer writer )
                        throws IOException
                    {
                        try
                        {
                            JsonBuilderFactory jsonBuilderFactory = json.builderFactory();
                            JsonObjectBuilder builder = jsonBuilderFactory.createObjectBuilder();
                            Table tableValue = (Table) result;

                            // Parse parameters
                            String tqx = response.getRequest().getResourceRef().getQueryAsForm()
                                                 .getFirstValue( "tqx" );
                            String reqId = null;
                            if( tqx != null )
                            {
                                String[] params = tqx.split( ";" );
                                for( String param : params )
                                {
                                    String[] p = param.split( ":" );
                                    String key = p[ 0 ];
                                    String value = p[ 1 ];

                                    if( key.equals( "reqId" ) )
                                    {
                                        reqId = value;
                                    }
                                }
                            }

                            builder.add( "version", "0.6" );
                            if( reqId != null )
                            {
                                builder.add( "reqId", reqId );
                            }
                            builder.add( "status", "ok" );

                            JsonObjectBuilder tableBuilder = jsonBuilderFactory.createObjectBuilder();
                            JsonArrayBuilder colsBuilder = jsonBuilderFactory.createArrayBuilder();
                            List<Column> columnList = tableValue.cols().get();
                            for( Column columnValue : columnList )
                            {
                                colsBuilder.add( jsonBuilderFactory.createObjectBuilder()
                                                                   .add( "id", columnValue.id().get() )
                                                                   .add( "label", columnValue.label().get() )
                                                                   .add( "type", columnValue.columnType().get() )
                                                                   .build() );
                            }
                            tableBuilder.add( "cols", colsBuilder.build() );

                            JsonArrayBuilder rowsBuilder = jsonBuilderFactory.createArrayBuilder();
                            for( Row rowValue : tableValue.rows().get() )
                            {
                                JsonArrayBuilder cellsBuilder = jsonBuilderFactory.createArrayBuilder();
                                int idx = 0;
                                for( Cell cellValue : rowValue.c().get() )
                                {
                                    Object value = cellValue.v().get();
                                    if( columnList.get( idx ).columnType().get().equals( Table.DATETIME )
                                        && value != null )
                                    {
                                        value = value.toString();
                                    }
                                    else if( columnList.get( idx ).columnType().get().equals( Table.DATE )
                                             && value != null )
                                    {
                                        value = value.toString();
                                    }
                                    else if( columnList.get( idx ).columnType().get().equals( Table.TIME_OF_DAY )
                                             && value != null )
                                    {
                                        value = value.toString();
                                    }

                                    JsonObjectBuilder cellBuilder = jsonBuilderFactory.createObjectBuilder();
                                    if( value != null )
                                    {
                                        cellBuilder.add( "v", jsonSerializer.toJson( Serializer.Options.ALL_TYPE_INFO, value ) );
                                    }
                                    if( cellValue.f().get() != null )
                                    {
                                        cellBuilder.add( "f", cellValue.f().get() );
                                    }
                                    cellsBuilder.add( cellBuilder.build() );
                                    idx++;
                                }
                                JsonObjectBuilder rowBuilder = jsonBuilderFactory.createObjectBuilder();
                                rowBuilder.add( "c", cellsBuilder.build() );
                                rowsBuilder.add( rowBuilder.build() );
                            }
                            tableBuilder.add( "rows", rowsBuilder.build() );
                            builder.add( "table", tableBuilder.build() );
                            writer.write( builder.build().toString() );
                        }
                        catch( JsonException e )
                        {
                            throw new IOException( e );
                        }
                    }
                } );
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
                        Map<String, Object> context = new HashMap<String, Object>();
                        context.put( "request", response.getRequest() );
                        context.put( "response", response );

                        context.put( "result", result );
                        try
                        {
                            cfg.getTemplate( "table.htm" ).process( context, writer );
                        }
                        catch( TemplateException e )
                        {
                            throw new IOException( e );
                        }
                    }
                };
                rep.setCharacterSet( CharacterSet.UTF_8 );
                response.setEntity( rep );
                return true;
            }
        }
        return false;
    }
}
