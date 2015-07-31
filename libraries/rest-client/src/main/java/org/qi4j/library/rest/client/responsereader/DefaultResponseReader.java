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

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONTokener;
import org.qi4j.library.rest.client.spi.ResponseReader;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.resource.ResourceException;

/**
 * ResponseReader for simple types from JSON
 */
public class DefaultResponseReader
   implements ResponseReader
{
    @Override
   public Object readResponse(Response response, Class<?> resultType) throws ResourceException
   {
      if (MediaType.APPLICATION_JSON.equals(response.getEntity().getMediaType()))
         if (resultType.equals(String.class))
         {
            try
            {
               return response.getEntity().getText();
            } catch (IOException e)
            {
               throw new ResourceException(e);
            }
         } else if (Number.class.isAssignableFrom(resultType))
         {
            try
            {
               Number value = (Number) new JSONTokener(response.getEntityAsText()).nextValue();
               if (resultType.equals(Integer.class))
                  return Integer.valueOf(value.intValue());
            } catch (JSONException e)
            {
               throw new ResourceException(e);
            }
         }

      return null;
   }
}
