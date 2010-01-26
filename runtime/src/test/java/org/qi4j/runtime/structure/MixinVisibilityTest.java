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

import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.structure.ApplicationSPI;

import static org.junit.Assert.*;

/**
 * JAVADOC
 */
public class MixinVisibilityTest
{
    @Test
    public void testMixinInModuleIsVisible()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {  // Module 1
                     new Assembler()
                     {
                         public void assemble( ModuleAssembly module )
                             throws AssemblyException
                         {
                             module.setName( "Module A" );
                             module.addTransients( B1Composite.class );
                             module.addObjects( ObjectA.class );
                         }
                     }
                  }
                }
            };

        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectBuilderFactory objectBuilderFactory = app.findModule( "Layer 1", "Module A" ).objectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInModuleWillFail()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {  // Module 1
                     new Assembler()
                     {
                         public void assemble( ModuleAssembly module )
                             throws AssemblyException
                         {
                             module.setName( "Module A" );
                             module.addTransients( B1Composite.class, B2Composite.class );
                             module.addObjects( ObjectA.class );
                         }
                     }
                  }
                }
            };

        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectBuilderFactory objectBuilderFactory = app.findModule( "Layer 1", "Module A" ).objectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = NoSuchCompositeException.class )
    public void testMixinInLayerIsNotVisible()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
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
                              module.addTransients( B1Composite.class );
                          }
                      }
                  }
                }
            };

        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectBuilderFactory objectBuilderFactory = app.findModule( "Layer 1", "Module A" ).objectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test
    public void testMixinInLayerIsVisible()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
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
                              module.addTransients( B1Composite.class ).visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };

        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectBuilderFactory objectBuilderFactory = app.findModule( "Layer 1", "Module A" ).objectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInLayerWillFailSameModule()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
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
                              module.addTransients( B1Composite.class, B2Composite.class )
                                  .visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };

        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectBuilderFactory objectBuilderFactory = app.findModule( "Layer 1", "Module A" ).objectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInLayerWillFailDiffModule()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
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
                            module.addTransients( B1Composite.class ).visibleIn( Visibility.layer );
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
                            module.addTransients( B2Composite.class ).visibleIn( Visibility.layer );
                        }
                    }
                  }
                }
            };

        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectBuilderFactory objectBuilderFactory = app.findModule( "Layer 1", "Module A" ).objectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    // @Test( expected= MixinTypeNotAvailableException.class )

    public void testMixinInLowerLayerIsNotVisible()
        throws Exception
    {

        Energy4Java boot = new Energy4Java();
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
                              module.addTransients( B1Composite.class ).visibleIn( Visibility.layer );
                          }
                      }
                  }
                }
            };

        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectBuilderFactory objectBuilderFactory = app.findModule( "Layer 1", "Module " ).objectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test
    public void testMixinInLowerLayerIsVisible()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
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
                              module.addTransients( B1Composite.class ).visibleIn( Visibility.application );
                          }
                      }
                  }
                }
            };

        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectBuilderFactory objectBuilderFactory = app.findModule( "Layer 1", "Module A" ).objectBuilderFactory();
        ObjectA object = objectBuilderFactory.newObjectBuilder( ObjectA.class ).newInstance();
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    class AssemblerB
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.setName( "Module B" );
            module.addTransients( B1Composite.class ).visibleIn( Visibility.module );
        }
    }

    public static class ObjectA
    {
        @Structure
        TransientBuilderFactory cbf;

        String test1()
        {
            B1 instance = cbf.newTransient( B1.class );
            return instance.test();
        }

        String test2()
        {
            TransientBuilder<B2> builder = cbf.newTransientBuilder( B2.class );
            builder.prototypeFor( B2.class ).b2().set( "abc" );
            B2 instance = builder.newInstance();
            return instance.b2().get();
        }
    }

    @Mixins( { MixinB.class } )
    public interface B1Composite
        extends TransientComposite, B1
    {
    }

    public interface B2Composite
        extends TransientComposite, B2
    {
    }

    public interface B2
    {
        @Optional
        Property<String> b2();
    }

    public interface B1
        extends B2
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