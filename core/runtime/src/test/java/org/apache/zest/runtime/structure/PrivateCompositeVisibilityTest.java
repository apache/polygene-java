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

package org.apache.zest.runtime.structure;

import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.NoSuchTransientException;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.structure.Application;
import org.apache.zest.bootstrap.ApplicationAssemblerAdapter;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.bootstrap.ModuleAssembly;

/**
 * JAVADOC
 */
public class PrivateCompositeVisibilityTest
{
    @Test( expected = NoSuchTransientException.class )
    public void testPrivateCompositeVisibility()
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer
                  {
                      new AssemblerA()
                  },
                  {
                      new AssemblerB()
                  }
                }
            };
        Application app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" ).newObject( ObjectA.class );
        object.test();
    }

    class AssemblerA
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.setName( "Module A" );
            module.objects( ObjectA.class );
        }
    }

    class AssemblerB
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.setName( "Module B" );
            module.transients( CompositeB.class ).visibleIn( Visibility.module );
        }
    }

    public static class ObjectA
    {
        @Structure
        TransientBuilderFactory cbf;

        String test()
        {
            CompositeB instance = cbf.newTransient( CompositeB.class );
            return instance.test();
        }
    }

    @Mixins( MixinB.class )
    public interface CompositeB
        extends TransientComposite
    {
        String test();
    }

    public abstract static class MixinB
        implements CompositeB
    {

        public String test()
        {
            return "ok";
        }
    }
}
