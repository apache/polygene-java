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

package org.qi4j.library.rest.client.responsereader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Dates;
import org.qi4j.library.rest.client.spi.ResponseReader;
import org.qi4j.library.rest.common.table.Table;
import org.qi4j.library.rest.common.table.TableBuilder;
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
                     value = Dates.fromString( value.toString() );
                  else if (cols.getJSONObject( j ).getString( "type" ).equals("date") && value != null)
                     try
                     {
                        value = new SimpleDateFormat( "yyyy-MM-dd").parse( value.toString() );
                     } catch (ParseException e)
                     {
                        throw new ResourceException(e);
                     }
                  else if (cols.getJSONObject( j ).getString( "type" ).equals("timeofday") && value != null)
                     try
                     {
                        value = new SimpleDateFormat( "HH:mm:ss").parse( value.toString() );
                     } catch (ParseException e)
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
