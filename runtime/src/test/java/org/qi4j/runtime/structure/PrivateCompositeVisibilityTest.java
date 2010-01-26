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
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.structure.ApplicationSPI;

/**
 * JAVADOC
 */
public class PrivateCompositeVisibilityTest
{
    @Test( expected = NoSuchCompositeException.class )
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
        ApplicationSPI app = boot.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        ObjectA object = app.findModule( "Layer 1", "Module A" )
            .objectBuilderFactory()
            .newObjectBuilder( ObjectA.class )
            .newInstance();
        object.test();
    }

    class AssemblerA
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.setName( "Module A" );
            module.addObjects( ObjectA.class );
        }
    }

    class AssemblerB
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.setName( "Module B" );
            module.addTransients( CompositeB.class ).visibleIn( Visibility.module );
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
