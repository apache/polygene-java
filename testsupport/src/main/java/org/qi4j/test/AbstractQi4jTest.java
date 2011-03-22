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

package org.qi4j.test;

import org.junit.After;
import org.junit.Before;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Composite tests.
 */
public abstract class AbstractQi4jTest
        implements Assembler
{
   protected Qi4j api;
   protected Qi4jSPI spi;

   protected Energy4Java qi4j;
   protected ApplicationModelSPI applicationModel;
   protected ApplicationSPI application;

   protected TransientBuilderFactory transientBuilderFactory;
   protected ObjectBuilderFactory objectBuilderFactory;
   protected ValueBuilderFactory valueBuilderFactory;
   protected UnitOfWorkFactory unitOfWorkFactory;
   protected QueryBuilderFactory queryBuilderFactory;
   protected ServiceFinder serviceLocator;

   protected ModuleSPI moduleInstance;

   private Logger log;

   @Before
   public void setUp()
           throws Exception
   {
      qi4j = new Energy4Java();
      applicationModel = newApplication();
      if (applicationModel == null)
      {
         // An AssemblyException has occurred that the Test wants to check for.
         return;
      }
      application = applicationModel.newInstance(qi4j.spi());
      initApplication(application);
      api = spi = qi4j.spi();
      application.activate();

      // Assume only one module
      moduleInstance = (ModuleSPI) application.findModule("Layer 1", "Module 1");
      transientBuilderFactory = moduleInstance.transientBuilderFactory();
      objectBuilderFactory = moduleInstance.objectBuilderFactory();
      valueBuilderFactory = moduleInstance.valueBuilderFactory();
      unitOfWorkFactory = moduleInstance.unitOfWorkFactory();
      queryBuilderFactory = moduleInstance.queryBuilderFactory();
      serviceLocator = moduleInstance.serviceFinder();
   }

   protected ApplicationModelSPI newApplication()
           throws AssemblyException
   {
      ApplicationAssembler assembler = new ApplicationAssembler()
      {
         public ApplicationAssembly assemble(ApplicationAssemblyFactory applicationFactory)
                 throws AssemblyException
         {
            return applicationFactory.newApplicationAssembly(new Assembler()
            {
               @Override
               public void assemble(ModuleAssembly module) throws AssemblyException
               {
                  module.layer().application().setMode(Application.Mode.test);
                  AbstractQi4jTest.this.assemble(module);
               }
            });
         }
      };

      try
      {
         return qi4j.newApplicationModel(assembler);
      } catch (AssemblyException e)
      {
         assemblyException(e);
         return null;
      }
   }

   /**
    * This method is called when there was an AssemblyException in the creation of the Qi4j application model.
    * <p/>
    * Override this method to catch valid failures to place into satisfiedBy suites.
    *
    * @param exception the exception thrown.
    * @throws AssemblyException The default implementation of this method will simply re-throw the exception.
    */
   protected void assemblyException(AssemblyException exception)
           throws AssemblyException
   {
      throw exception;
   }

   protected void initApplication(Application app)
           throws Exception
   {
   }

   @After
   public void tearDown()
           throws Exception
   {
      if (unitOfWorkFactory != null && unitOfWorkFactory.currentUnitOfWork() != null)
      {
         UnitOfWork current;
         while ((current = unitOfWorkFactory.currentUnitOfWork()) != null)
         {
            if (current.isOpen())
            {
               current.discard();
            } else
            {
               throw new InternalError("I have seen a case where a UoW is on the stack, but not opened.");
            }
         }

         new Exception("UnitOfWork not properly cleaned up").printStackTrace();
      }

      if (application != null)
      {
         application.passivate();
      }
   }

   protected Logger getLog()
   {
      if (this.log == null)
      {
         this.log = LoggerFactory.getLogger(this.getClass());
      }

      return this.log;
   }
}