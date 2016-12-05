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

package org.apache.zest.library.rest.client;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Initializable;
import org.apache.zest.api.structure.Module;
import org.apache.zest.library.rest.client.spi.RequestWriter;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegates to a list of potential writers. Register writers on startup.
 */
public class RequestWriterDelegator
   implements RequestWriter, Initializable
{
   List<RequestWriter> requestWriters = new ArrayList<RequestWriter>( );

   @Structure
   private Module module;

    @Override
   public void initialize()
   {
      Logger logger = LoggerFactory.getLogger( getClass() );

      ResourceBundle defaultRequestWriters = ResourceBundle.getBundle( "org.apache.zest.library.rest.client.rest-client" );

      String requestWriterClasses = defaultRequestWriters.getString( "requestwriters" );
      logger.info( "Using request writers:"+requestWriterClasses );
      for (String className : requestWriterClasses.split( "," ))
      {
         try
         {
            Class writerClass = module.descriptor().classLoader().loadClass( className.trim() );
            RequestWriter requestWriter = (RequestWriter) module.newObject( writerClass );
            registerRequestWriter(requestWriter);
         } catch (ClassNotFoundException e)
         {
            logger.warn( "Could not register request writer "+className, e );
         }
      }
   }

   public void registerRequestWriter(RequestWriter writer)
   {
      requestWriters.add( writer );
   }

    @Override
   public boolean writeRequest(Object requestObject, Request request) throws ResourceException
   {
      if (requestObject == null)
      {
         if (!Method.GET.equals(request.getMethod()))
            request.setEntity(new EmptyRepresentation());

         return true;
      }

      if (requestObject instanceof Representation)
      {
         request.setEntity((Representation) requestObject);
         return true;
      }

      for (RequestWriter requestWriter : requestWriters)
      {
         if (requestWriter.writeRequest(requestObject, request))
            return true;
      }

      return false;
   }
}