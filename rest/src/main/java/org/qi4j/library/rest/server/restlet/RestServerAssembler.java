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

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rest.server.restlet.requestreader.DefaultRequestReader;
import org.qi4j.library.rest.server.restlet.responsewriter.*;
import org.restlet.service.MetadataService;

import java.util.Properties;

import static org.qi4j.bootstrap.ImportedServiceDeclaration.INSTANCE;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * JAVADOC
 */
public class RestServerAssembler
        implements Assembler
{
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      Properties props = new Properties();
      try
      {
         props.load(getClass().getResourceAsStream("/velocity.properties"));

         VelocityEngine velocity = new VelocityEngine(props);

         module.importedServices(VelocityEngine.class)
                 .importedBy(INSTANCE).setMetaInfo(velocity);

      } catch (Exception e)
      {
         throw new AssemblyException("Could not load velocity properties", e);
      }
      module.objects(DefaultCommandQueryResource.class);

      module.importedServices(MetadataService.class);

      module.importedServices(ResponseWriterDelegator.class).identifiedBy("responsewriterdelegator").importedBy(NEW_OBJECT).visibleIn(Visibility.layer);
      module.objects(ResponseWriterDelegator.class);

      module.importedServices(RequestReaderDelegator.class).identifiedBy("requestreaderdelegator").importedBy(NEW_OBJECT).visibleIn(Visibility.layer);
      module.objects(RequestReaderDelegator.class);

      // Standard result writers
      module.objects(ResourceTemplateResponseWriter.class,
              DefaultResponseWriter.class,
              LinksResponseWriter.class,
              TableResponseWriter.class,
              ResourceResponseWriter.class,
              ValueCompositeResponseWriter.class,
              JSONResponseWriter.class,
              FormResponseWriter.class);

      // Standard request readers
      module.objects(DefaultRequestReader.class);

   }
}
