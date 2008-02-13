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

import junit.framework.TestCase;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.Structure;
import org.qi4j.runtime.Energy4Java;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.spi.structure.Visibility;

/**
 * TODO
 */
public class PrivateCompositeVisibilityTest
    extends TestCase
{
    public void testPrivateCompositeVisibility()
        throws Exception
    {
        Qi4jRuntime is = new Energy4Java();
        ApplicationFactory applicationFactory = new ApplicationFactory( is, new ApplicationAssemblyFactory() );

        Assembly[][][] assemblies = new Assembly[][][]
            {
                { // Layer
                  {
                      new AssemblyA()
                  },
                  {
                      new AssemblyB()
                  }
                }
            };


        ApplicationInstance app = applicationFactory.newApplication( assemblies ).newApplicationInstance( "Test" );
        app.activate();
        ObjectA object = app.getLayerInstances().get( 0 ).getModuleInstances().get( 0 ).getObjectBuilderFactory().newObjectBuilder( ObjectA.class ).newInstance();
        try
        {
            object.test();
            fail( "Should have thrown an exception since CompositeB is not visible" );
        }
        catch( InvalidApplicationException e )
        {
            // Ok
        }

    }

    class AssemblyA
        implements Assembly
    {
        public void configure( ModuleAssembly module ) throws AssemblyException
        {
            module.setName( "Module A" );
            module.addObjects( ObjectA.class );
        }
    }

    class AssemblyB
        implements Assembly
    {
        public void configure( ModuleAssembly module ) throws AssemblyException
        {
            module.setName( "Module B" );
            module.addComposites( CompositeB.class ).visibleIn( Visibility.module );
        }
    }

    public static class ObjectA
    {
        @Structure CompositeBuilderFactory cbf;

        String test()
        {
            CompositeB instance = cbf.newComposite( CompositeB.class );
            return instance.test();
        }
    }

    @Mixins( MixinB.class )
    public interface CompositeB
        extends Composite
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
