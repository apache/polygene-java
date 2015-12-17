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

package org.apache.zest.library.rest.client.responsereader;

import java.util.Iterator;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.service.qualifier.Tagged;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.type.ValueCompositeType;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.api.value.ValueDeserializer;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.library.rest.client.spi.ResponseReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class JSONResponseReader
   implements ResponseReader
{
   @Structure
   private ModuleDescriptor module;

   @Service
   @Tagged( ValueSerialization.Formats.JSON )
   private ValueDeserializer valueDeserializer;

    @Override
   public Object readResponse( Response response, Class<?> resultType )
   {
      if (response.getEntity().getMediaType().equals( MediaType.APPLICATION_JSON))
      {
         if (ValueComposite.class.isAssignableFrom( resultType ))
         {
            String jsonValue = response.getEntityAsText();
            ValueCompositeType valueType = module.valueDescriptor( resultType.getName() ).valueType();
            return valueDeserializer.deserialize( module, valueType, jsonValue );
         }
         else if (resultType.equals(Form.class))
         {
            try
            {
               String jsonValue = response.getEntityAsText();
               JSONObject jsonObject = new JSONObject(jsonValue);
               Iterator<?> keys = jsonObject.keys();
               Form form = new Form();
               while (keys.hasNext())
               {
                  Object key = keys.next();
                  form.set(key.toString(), jsonObject.get(key.toString()).toString());
               }
               return form;
            } catch (JSONException e)
            {
               throw new ResourceException(e);
            }
         }
      }

      return null;
   }
}
