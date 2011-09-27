/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package org.qi4j.library.rest.server.restlet;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * Finder that instantiates ServerResources using Qi4j.
 */
public class ResourceFinder extends Finder
{
   @Structure
   private Module module;

   public ResourceFinder()
   {
   }

   @Override
   public ServerResource create( Class<? extends ServerResource> targetClass, Request request, Response response )
   {
      try
      {
         ServerResource resource = module.newObject(targetClass);
         return resource;
      } catch (Exception e)
      {
         e.printStackTrace(  );
         return null;
      }
   }
}
