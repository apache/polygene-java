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

package org.qi4j.library.rest.client.requestwriter;

import org.qi4j.library.rest.client.spi.RequestWriter;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.resource.ResourceException;

/**
 * Request writer for Form. Transfers form state to request reference as query parameters
 */
public class FormRequestWriter
   implements RequestWriter
{
    @Override
   public boolean writeRequest(Object requestObject, Request request) throws ResourceException
   {
      if (requestObject instanceof Form)
      {
         // Form as query parameters
         if (request.getMethod().equals(Method.GET))
            request.getResourceRef().setQuery(((Form)requestObject).getQueryString());
         else
            request.setEntity(((Form)requestObject).getWebRepresentation(CharacterSet.UTF_8));

         return true;
      }

      return false;
   }
}
