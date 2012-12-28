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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.util.Dates;
import org.qi4j.library.rest.common.table.Cell;
import org.qi4j.library.rest.common.table.Column;
import org.qi4j.library.rest.common.table.Row;
import org.qi4j.library.rest.common.table.Table;
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
    private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON );

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
                            JSONWriter json = new JSONWriter( writer );
                            Table tableValue = (Table) result;

                            // Parse parameters
                            String tqx = response.getRequest().getResourceRef().getQueryAsForm().getFirstValue( "tqx" );
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

                            json.object().key( "version" ).value( "0.6" );
                            if( reqId != null )
                            {
                                json.key( "reqId" ).value( reqId );
                            }
                            json.key( "status" ).value( "ok" );
                            json.key( "table" ).object();
                            List<Column> columnList = tableValue.cols().get();
                            json.key( "cols" ).array();
                            for( Column columnValue : columnList )
                            {
                                json.object().
                                    key( "id" ).value( columnValue.id().get() ).
                                    key( "label" ).value( columnValue.label().get() ).
                                    key( "type" ).value( columnValue.columnType().get() ).
                                    endObject();
                            }
                            json.endArray();

                            json.key( "rows" ).array();
                            for( Row rowValue : tableValue.rows().get() )
                            {
                                json.object();
                                json.key( "c" ).array();
                                int idx = 0;
                                for( Cell cellValue : rowValue.c().get() )
                                {
                                    json.object();
                                    Object value = cellValue.v().get();
                                    if( columnList.get( idx )
                                            .columnType()
                                            .get()
                                            .equals( Table.DATETIME ) && value != null )
                                    {
                                        value = Dates.toUtcString( (Date) value );
                                    }
                                    else if( columnList.get( idx )
                                                 .columnType()
                                                 .get()
                                                 .equals( Table.DATE ) && value != null )
                                    {
                                        value = new SimpleDateFormat( "yyyy-MM-dd" ).format( (Date) value );
                                    }
                                    else if( columnList.get( idx )
                                                 .columnType()
                                                 .get()
                                                 .equals( Table.TIME_OF_DAY ) && value != null )
                                    {
                                        value = new SimpleDateFormat( "HH:mm:ss" ).format( (Date) value );
                                    }

                                    if( value != null )
                                    {
                                        json.key( "v" ).value( value );
                                    }
                                    if( cellValue.f().get() != null )
                                    {
                                        json.key( "f" ).value( cellValue.f().get() );
                                    }
                                    json.endObject();

                                    idx++;
                                }
                                json.endArray();
                                json.endObject();
                            }
                            json.endArray();
                            json.endObject();
                            json.endObject();
                        }
                        catch( JSONException e )
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
