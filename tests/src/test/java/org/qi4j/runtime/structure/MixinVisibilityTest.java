/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.AmbiguousMixinTypeException;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.MixinTypeNotAvailableException;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.property.Property;
import org.qi4j.spi.injection.StructureContext;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public class MixinVisibilityTest
{
    @Test
    public void testMixinInModuleIsVisible()
        throws Exception
    {
        ApplicationFactory applicationFactory = new ApplicationFactory();
        Assembler[][][] assemblers = new Assembler[][][]
            { { // Layer
                {  // Module 1
                   new Assembler()
                   {
                       public void assemble( ModuleAssembly module )
                           throws AssemblyException
                       {
                           module.setName( "Module A" );
                           module.addComposites( B1Composite.class );
                           module.addObjects( ObjectA.class );
                       }
                   }
                }
            } };


        ApplicationInstance app = applicationFactory.newApplication( assemblers ).newApplicationInstance( "Test" );
        app.activate();
        LayerInstance layerInstance = app.getLayerInstances().get( 0 );
        ModuleInstance moduleInstance = layerInstance.getModuleInstances().get( 0 );
        StructureContext structureContext = moduleInstance.getStructureContext();
        ObjectBuilderFactory objectBuilderFactory = structureContext.getObjectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousMixinTypeException.class )
    public void testMultipleMixinsInModuleWillFail()
        throws Exception
    {
        ApplicationFactory applicationFactory = new ApplicationFactory();
        Assembler[][][] assemblers = new Assembler[][][]
            { { // Layer
                {  // Module 1
                   new Assembler()
                   {
                       public void assemble( ModuleAssembly module )
                           throws AssemblyException
                       {
                           module.setName( "Module A" );
                           module.addComposites( B1Composite.class, B2Composite.class );
                           module.addObjects( ObjectA.class );
                       }
                   }
                }
            } };


        ApplicationInstance app = applicationFactory.newApplication( assemblers ).newApplicationInstance( "Test" );
        app.activate();
        LayerInstance layerInstance = app.getLayerInstances().get( 0 );
        ModuleInstance moduleInstance = layerInstance.getModuleInstances().get( 0 );
        StructureContext structureContext = moduleInstance.getStructureContext();
        ObjectBuilderFactory objectBuilderFactory = structureContext.getObjectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = MixinTypeNotAvailableException.class )
    public void testMixinInLayerIsNotVisible()
        throws Exception
    {

        ApplicationFactory applicationFactory = new ApplicationFactory();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.addObjects( ObjectA.class );
                          }
                      }
                  },
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.addComposites( B1Composite.class );
                          }
                      }
                  }
                }
            };


        ApplicationInstance app = applicationFactory.newApplication( assemblers ).newApplicationInstance( "Test" );
        app.activate();
        LayerInstance layerInstance = app.getLayerInstances().get( 0 );
        ModuleInstance moduleInstance = layerInstance.getModuleInstances().get( 0 );
        StructureContext structureContext = moduleInstance.getStructureContext();
        ObjectBuilderFactory objectBuilderFactory = structureContext.getObjectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test
    public void testMixinInLayerIsVisible()
        throws Exception
    {
        ApplicationFactory applicationFactory = new ApplicationFactory();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.addObjects( ObjectA.class );
                          }
                      }
                  },
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.addComposites( B1Composite.class ).visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };


        ApplicationInstance app = applicationFactory.newApplication( assemblers ).newApplicationInstance( "Test" );
        app.activate();
        LayerInstance layerInstance = app.getLayerInstances().get( 0 );
        ModuleInstance moduleInstance = layerInstance.getModuleInstances().get( 0 );
        StructureContext structureContext = moduleInstance.getStructureContext();
        ObjectBuilderFactory objectBuilderFactory = structureContext.getObjectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }


    @Test( expected = AmbiguousMixinTypeException.class )
    public void testMultipleMixinsInLayerWillFailSameModule()
        throws Exception
    {
        ApplicationFactory applicationFactory = new ApplicationFactory();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.addObjects( ObjectA.class );
                          }
                      }
                  },
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.addComposites( B1Composite.class, B2Composite.class ).visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };


        ApplicationInstance app = applicationFactory.newApplication( assemblers ).newApplicationInstance( "Test" );
        app.activate();
        LayerInstance layerInstance = app.getLayerInstances().get( 0 );
        ModuleInstance moduleInstance = layerInstance.getModuleInstances().get( 0 );
        StructureContext structureContext = moduleInstance.getStructureContext();
        ObjectBuilderFactory objectBuilderFactory = structureContext.getObjectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousMixinTypeException.class )
    public void testMultipleMixinsInLayerWillFailDiffModule()
        throws Exception
    {
        ApplicationFactory applicationFactory = new ApplicationFactory();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  { // Module 1
                    new Assembler()
                    {
                        public void assemble( ModuleAssembly module )
                            throws AssemblyException
                        {
                            module.setName( "Module A" );
                            module.addObjects( ObjectA.class );
                        }
                    }
                  },
                  { // Module 2
                    new Assembler()
                    {
                        public void assemble( ModuleAssembly module )
                            throws AssemblyException
                        {
                            module.setName( "Module B" );
                            module.addComposites( B1Composite.class ).visibleIn( Visibility.layer );
                        }
                    }
                  },
                  { // Module 3
                    new Assembler()
                    {
                        public void assemble( ModuleAssembly module )
                            throws AssemblyException
                        {
                            module.setName( "Module C" );
                            module.addComposites( B2Composite.class ).visibleIn( Visibility.layer );
                        }
                    }
                  }
                }
            };


        ApplicationInstance app = applicationFactory.newApplication( assemblers ).newApplicationInstance( "Test" );
        app.activate();
        LayerInstance layerInstance = app.getLayerInstances().get( 0 );
        ModuleInstance moduleInstance = layerInstance.getModuleInstances().get( 0 );
        StructureContext structureContext = moduleInstance.getStructureContext();
        ObjectBuilderFactory objectBuilderFactory = structureContext.getObjectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    // @Test( expected= MixinTypeNotAvailableException.class )
    public void testMixinInLowerLayerIsNotVisible()
        throws Exception
    {

        ApplicationFactory applicationFactory = new ApplicationFactory();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer 1
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.addObjects( ObjectA.class );
                          }
                      }
                  }
                },
                { // Layer 2
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.addComposites( B1Composite.class ).visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };


        ApplicationInstance app = applicationFactory.newApplication( assemblers ).newApplicationInstance( "Test" );
        app.activate();
        ModuleInstance moduleInstance = app.getLayerByName( "Layer 1" ).getModuleByName( "Module A" );
        StructureContext structureContext = moduleInstance.getStructureContext();
        ObjectBuilderFactory objectBuilderFactory = structureContext.getObjectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }


    @Test
    public void testMixinInLowerLayerIsVisible()
        throws Exception
    {

        ApplicationFactory applicationFactory = new ApplicationFactory();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer 1
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module A" );
                              module.addObjects( ObjectA.class );
                          }
                      }
                  }
                },
                { // Layer 2
                  {
                      new Assembler()
                      {
                          public void assemble( ModuleAssembly module )
                              throws AssemblyException
                          {
                              module.setName( "Module B" );
                              module.addComposites( B1Composite.class ).visibleIn( Visibility.application );
                          }
                      }
                  }
                }
            };


        ApplicationInstance app = applicationFactory.newApplication( assemblers ).newApplicationInstance( "Test" );
        app.activate();
        ModuleInstance moduleInstance = app.getLayerByName( "Layer 1" ).getModuleByName( "Module A" );
        StructureContext structureContext = moduleInstance.getStructureContext();
        ObjectBuilderFactory objectBuilderFactory = structureContext.getObjectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    class AssemblerB
        implements Assembler
    {
        public void assemble( ModuleAssembly module ) throws AssemblyException
        {
            module.setName( "Module B" );
            module.addComposites( B1Composite.class ).visibleIn( Visibility.module );
        }
    }

    public static class ObjectA
    {
        @Structure CompositeBuilderFactory cbf;

        String test1()
        {
            B1 instance = cbf.newComposite( B1.class );
            return instance.test();
        }

        String test2()
        {
            CompositeBuilder<B2> builder = cbf.newCompositeBuilder( B2.class );
            builder.stateFor( B2.class ).b2().set( "abc" );
            B2 instance = builder.newInstance();
            return instance.b2().get();
        }
    }

    @Mixins( { MixinB.class } )
    public interface B1Composite extends Composite, B1
    {
    }

    public interface B2Composite extends Composite, B2
    {
    }

    public interface B2
    {
        Property<String> b2();
    }

    public interface B1 extends B2
    {
        String test();
    }

    public abstract static class MixinB
        implements B1
    {
        public String test()
        {
            return "ok";
        }
    }

}