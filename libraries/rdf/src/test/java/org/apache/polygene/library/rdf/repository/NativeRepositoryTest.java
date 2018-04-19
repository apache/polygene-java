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

package org.apache.polygene.library.rdf.repository;

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.fileconfig.FileConfigurationAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationOverride;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.TemporaryFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * JAVADOC
 */
@ExtendWith( TemporaryFolder.class )
public class NativeRepositoryTest extends AbstractPolygeneTest
{
   private TemporaryFolder tmpDir;

   @Service
   Repository repository;

   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      new EntityTestAssembler().assemble( module );
      new FileConfigurationAssembler()
          .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
          .assemble( module );
      module.services(NativeRepositoryService.class).instantiateOnStartup();
      module.entities(NativeConfiguration.class);
      module.objects(getClass());
   }

   @Test
   public void testNativeRepository() throws RepositoryException
   {
      RepositoryConnection conn = repository.getConnection();
       assertThat( "repository is open", conn.isOpen(), equalTo( true ) );
      conn.close();
   }
}