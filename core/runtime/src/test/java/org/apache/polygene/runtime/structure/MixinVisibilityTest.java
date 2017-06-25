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

package org.apache.polygene.runtime.structure;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.AmbiguousTypeException;
import org.apache.polygene.api.composite.NoSuchTransientTypeException;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.ApplicationAssemblerAdapter;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * JAVADOC
 */
public class MixinVisibilityTest
{
    @Test
    public void testMixinInModuleIsVisible()
        throws Exception
    {
        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {  // Module 1
                     module -> {
                         module.setName( "Module A" );
                         module.transients( B1Composite.class );
                         module.objects( ObjectA.class );
                     }
                  }
                }
            };

        Application app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInModuleWillFail()
        throws Exception
    {
        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {  // Module 1
                     module -> {
                         module.setName( "Module A" );
                         module.transients( B1Composite.class, B2Composite.class );
                         module.objects( ObjectA.class );
                     }
                  }
                }
            };

        Application app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = NoSuchTransientTypeException.class )
    public void testMixinInLayerIsNotVisible()
        throws Exception
    {
        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      module -> {
                          module.setName( "Module A" );
                          module.objects( ObjectA.class );
                      }
                  },
                  {
                      module -> {
                          module.setName( "Module B" );
                          module.transients( B1Composite.class );
                      }
                  }
                }
            };

        Application app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test
    public void testMixinInLayerIsVisible()
        throws Exception
    {
        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      module -> {
                          module.setName( "Module A" );
                          module.objects( ObjectA.class );
                      }
                  },
                  {
                      module -> {
                          module.setName( "Module B" );
                          module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                      }
                  }
                }
            };

        Application app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInLayerWillFailSameModule()
        throws Exception
    {
        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      module -> {
                          module.setName( "Module A" );
                          module.objects( ObjectA.class );
                      }
                  },
                  {
                      module -> {
                          module.setName( "Module B" );
                          module.transients( B1Composite.class, B2Composite.class )
                                .visibleIn( Visibility.layer );
                      }
                  }
                }
            };

        Application app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test( expected = AmbiguousTypeException.class )
    public void testMultipleMixinsInLayerWillFailDiffModule()
        throws Exception
    {
        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  { // Module 1
                    module -> {
                        module.setName( "Module A" );
                        module.objects( ObjectA.class );
                    }
                  },
                  { // Module 2
                    module -> {
                        module.setName( "Module B" );
                        module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                    }
                  },
                  { // Module 3
                    module -> {
                        module.setName( "Module C" );
                        module.transients( B2Composite.class ).visibleIn( Visibility.layer );
                    }
                  }
                }
            };

        Application app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    // @Test( expected= MixinTypeNotAvailableException.class )

    public void testMixinInLowerLayerIsNotVisible()
        throws Exception
    {

        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer 1
                  {
                      module -> {
                          module.setName( "Module A" );
                          module.objects( ObjectA.class );
                      }
                  }
                },
                { // Layer 2
                  {
                      module -> {
                          module.setName( "Module B" );
                          module.transients( B1Composite.class ).visibleIn( Visibility.layer );
                      }
                  }
                }
            };

        Application app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module " ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    @Test
    public void testMixinInLowerLayerIsVisible()
        throws Exception
    {
        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer 1
                  {
                      module -> {
                          module.setName( "Module A" );
                          module.objects( ObjectA.class );
                      }
                  }
                },
                { // Layer 2
                  {
                      module -> {
                          module.setName( "Module B" );
                          module.transients( B1Composite.class ).visibleIn( Visibility.application );
                      }
                  }
                }
            };

        Application app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        assertEquals( "ok", object.test1() );
        assertEquals( "abc", object.test2() );
    }

    class AssemblerB
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
        {
            module.setName( "Module B" );
            module.transients( B1Composite.class ).visibleIn( Visibility.module );
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
        extends B1
    {
    }

    public interface B2Composite
        extends B2
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