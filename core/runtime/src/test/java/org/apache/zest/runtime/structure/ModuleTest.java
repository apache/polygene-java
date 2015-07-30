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

package org.apache.zest.runtime.structure;

import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.bootstrap.ApplicationAssembler;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.bootstrap.ModuleAssembly;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of the Module interface. This should satisfiedBy both the general interface and all possible visibility
 * rules.
 */
public class ModuleTest
{
    public Application givenFixture1()
        throws AssemblyException
    {
        Energy4Java boot = new Energy4Java();
        return boot.newApplication( new ApplicationAssembler()
        {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                return applicationFactory.newApplicationAssembly( new Assembler[][][]
                                                                  {
                                                                      {
                                                                          {
                                                                              new Assembler()
                                                                              {
                                                                                  public void assemble( ModuleAssembly module )
                                                                                      throws AssemblyException
                                                                                  {
                                                                                      module.transients( TestComposite1.class );
                                                                                  }
                                                                              }
                                                                          },
                                                                          {
                                                                              new Assembler()
                                                                              {
                                                                                  public void assemble( ModuleAssembly module )
                                                                                      throws AssemblyException
                                                                                  {
                                                                                      module.transients( TestComposite2.class )
                                                                                          .visibleIn( Visibility.layer );
                                                                                  }
                                                                              }
                                                                          }
                                                                      }
                                                                  } );
            }
        } );
    }

    @Test
    public void givenFixture1WhenGetNameThenConfiguredNameIsReturned()
        throws AssemblyException
    {
        Application app = givenFixture1();

        Module module = app.findModule( "Layer 1", "Module 1" ).newTransient( TestComposite1.class ).getModule();
        assertThat( "module name is correct", module.name(), equalTo( "Module 1" ) );
    }

    @Test
    public void givenFixture1WhenGetModuleForCompositeThenCorrectModuleIsReturned()
        throws AssemblyException
    {
        Application app = givenFixture1();

        Module module = app.findModule( "Layer 1", "Module 1" ).newTransient( TestComposite1.class ).getModule();
        assertThat( "module for composite is correct", module, equalTo( module ) );
    }

    @Test
    public void givenFixture1WhenFindFromAnotherModuleThenCompositeTypeIsReturned()
        throws ClassNotFoundException, AssemblyException
    {
        Application app = givenFixture1();

        Module module = app.findModule( "Layer 1", "Module 1" ).newTransient( TestComposite1.class ).getModule();
        module.classLoader().loadClass( TestComposite2.class.getName() );
    }

    @Mixins( TestMixin1.class )
    public interface TestComposite1
        extends TransientComposite
    {
        Module getModule();
    }

    public abstract static class TestMixin1
        implements TestComposite1
    {
        @Structure
        Module module;

        public Module getModule()
        {
            return module;
        }
    }

    @Mixins( TestMixin2.class )
    public interface TestComposite2
        extends TransientComposite
    {
        Module getModule();
    }

    public abstract static class TestMixin2
        implements TestComposite2
    {
        @Structure
        Module module;

        public Module getModule()
        {
            return module;
        }
    }

    public interface TestComposite21
        extends TestComposite2
    {
    }

    public static class TestObject1
    {

    }

    public static class TestObject2
    {

    }
}
