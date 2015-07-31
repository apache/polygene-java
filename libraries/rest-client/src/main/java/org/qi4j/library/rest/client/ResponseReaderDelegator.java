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

package org.qi4j.library.rest.client;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.client.spi.ResponseReader;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegates to a list of potential readers. Register readers on startup.
 */
public class ResponseReaderDelegator
   implements ResponseReader, Initializable
{
   List<ResponseReader> responseReaders = new ArrayList<ResponseReader>( );

   @Structure
   private Module module;

   @Override
   public void initialize() throws InitializationException
   {
      Logger logger = LoggerFactory.getLogger( getClass() );

      ResourceBundle defaultResponseReaders = ResourceBundle.getBundle( "org.qi4j.library.rest.client.rest-client" );

      String responseReaderClasses = defaultResponseReaders.getString( "responsereaders" );
      logger.info( "Using responsereaders:"+responseReaderClasses );
      for (String className : responseReaderClasses.split( "," ))
      {
         try
         {
            Class readerClass = module.classLoader().loadClass( className.trim() );
            ResponseReader reader = (ResponseReader) module.newObject( readerClass );
            registerResponseReader( reader );
         } catch (ClassNotFoundException e)
         {
            logger.warn( "Could not register response reader "+className, e );
         }
      }
   }

   public void registerResponseReader( ResponseReader reader )
   {
      responseReaders.add( reader );
   }

   @Override
   public Object readResponse( Response response, Class<?> resultType )
   {
      if (resultType.equals(Representation.class))
         return response.getEntity();

      for (ResponseReader responseReader : responseReaders)
      {
         Object result = responseReader.readResponse( response, resultType );
         if (result != null)
            return result;
      }

      return null;
   }
}