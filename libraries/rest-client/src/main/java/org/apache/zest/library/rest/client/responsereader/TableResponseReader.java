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

package org.apache.zest.library.rest.client.responsereader;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.structure.Module;
import org.apache.zest.library.rest.client.spi.ResponseReader;
import org.apache.zest.library.rest.common.table.Table;
import org.apache.zest.library.rest.common.table.TableBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
   Module module;

    @Override
   public Object readResponse( Response response, Class<?> resultType ) throws ResourceException
   {
      if (response.getEntity().getMediaType().equals( MediaType.APPLICATION_JSON) && Table.class.isAssignableFrom( resultType ))
      {
         String jsonValue = response.getEntityAsText();
         try
         {
            JSONObject jsonObject = new JSONObject(jsonValue);

            JSONObject table = jsonObject.getJSONObject( "table" );
            TableBuilder builder = new TableBuilder(module);

            JSONArray cols = table.getJSONArray( "cols" );
            for (int i = 0; i < cols.length(); i++)
            {
               JSONObject col = cols.getJSONObject( i );
               builder.column( col.optString( "id" ),  col.getString( "label" ), col.getString( "type" ));
            }

            JSONArray rows = table.getJSONArray( "rows" );
            for (int i = 0; i < rows.length(); i++)
            {
               builder.row();
               JSONObject row = rows.getJSONObject( i );
               JSONArray cells = row.getJSONArray( "c" );
               for (int j = 0; j < cells.length(); j++)
               {
                  JSONObject cell = cells.getJSONObject( j );
                  Object value = cell.opt( "v" );
                  String formatted = cell.optString("f");

                  if (cols.getJSONObject( j ).getString( "type" ).equals("datetime") && value != null)
                     value = ZonedDateTime.parse( value.toString() );
                  else if (cols.getJSONObject( j ).getString( "type" ).equals("date") && value != null)
                     try
                     {
                        value = DateTimeFormatter.ofPattern( "yyyy-MM-dd").parse( value.toString() );
                     } catch (DateTimeParseException e)
                     {
                        throw new ResourceException(e);
                     }
                  else if (cols.getJSONObject( j ).getString( "type" ).equals("timeofday") && value != null)
                     try
                     {
                        value = DateTimeFormatter.ofPattern(  "HH:mm:ss").parse( value.toString() );
                     } catch (DateTimeParseException e)
                     {
                        throw new ResourceException(e);
                     }

                  builder.cell( value, formatted );
               }
               builder.endRow();
            }

            return builder.newTable();
         } catch (JSONException e)
         {
            throw new ResourceException( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
         }
      }

      return null;
   }
}
