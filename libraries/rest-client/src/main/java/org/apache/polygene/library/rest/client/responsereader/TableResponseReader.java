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

package org.apache.polygene.library.rest.client.responsereader;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.library.rest.client.spi.ResponseReader;
import org.apache.polygene.library.rest.common.table.Table;
import org.apache.polygene.library.rest.common.table.TableBuilder;
import org.apache.polygene.spi.serialization.JsonDeserializer;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class TableResponseReader
    implements ResponseReader
{
    @Structure
    private Module module;

    @Service
    private JsonDeserializer jsonDeserializer;

    @Override
    public Object readResponse( Response response, Class<?> resultType ) throws ResourceException
    {
        if( response.getEntity().getMediaType().equals( MediaType.APPLICATION_JSON )
            && Table.class.isAssignableFrom( resultType ) )
        {
            try
            {
                JsonObject jsonObject = Json.createReader( response.getEntity().getReader() ).readObject();
                JsonObject table = jsonObject.getJsonObject( "table" );

                TableBuilder builder = new TableBuilder( module );

                JsonArray cols = table.getJsonArray( "cols" );
                cols.getValuesAs( JsonObject.class ).forEach(
                    col -> builder.column( col.getString( "id", null ),
                                           col.getString( "label" ),
                                           col.getString( "type" ) ) );

                table.getJsonArray( "rows" ).getValuesAs( JsonObject.class ).forEach(
                    row ->
                    {
                        builder.row();
                        List<JsonObject> cells = row.getJsonArray( "c" ).getValuesAs( JsonObject.class );
                        for( int idx = 0; idx < cells.size(); idx++ )
                        {
                            JsonObject cell = cells.get( idx );
                            JsonValue jsonValue = cell.get( "v" );
                            String formatted = cell.getString( "f", null );
                            String type = cols.getJsonObject( idx ).getString( "type" );
                            Object value;
                            switch( type )
                            {
                                case "datetime":
                                    value = ZonedDateTime.parse( ( (JsonString) jsonValue ).getString() );
                                    break;
                                case "date":
                                    value = DateTimeFormatter.ofPattern( "yyyy-MM-dd" )
                                                             .parse( ( (JsonString) jsonValue ).getString() );
                                    break;
                                case "timeofday":
                                    value = DateTimeFormatter.ofPattern( "HH:mm:ss" )
                                                             .parse( ( (JsonString) jsonValue ).getString() );
                                    break;
                                default:
                                    value = jsonValue.getValueType() == JsonValue.ValueType.STRING
                                            ? ( (JsonString) jsonValue ).getString()
                                            : jsonValue.toString();
                            }
                            builder.cell( value, formatted );
                        }
                        builder.endRow();
                    }
                );
                return builder.newTable();
            }
            catch( Exception e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e );
            }
        }
        return null;
    }
}
