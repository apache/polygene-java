/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.rdf.repository;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * JAVADOC
 */
public class NativeRepositoryTest
        extends AbstractQi4jTest
{
   @Service
   Repository repository;

   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      new EntityTestAssembler().assemble( module );
      module.services(FileConfigurationService.class).instantiateOnStartup();
      module.services(NativeRepositoryService.class).instantiateOnStartup();
      module.entities(NativeConfiguration.class);
      module.objects(getClass());
   }

   @Test
   public void testNativeRepository() throws RepositoryException
   {
      module.injectTo(this);

      RepositoryConnection conn = repository.getConnection();

      Assert.assertThat("repository is open", conn.isOpen(), CoreMatchers.equalTo(true));

      conn.close();
   }
}