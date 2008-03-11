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

package org.qi4j.composite.scope;

import org.qi4j.Qi4j;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntitySessionFactory;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.service.ServiceLocator;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class StructureInjectionTest
    extends AbstractQi4jTest
{
    public void configure( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( StructureInjectionComposite.class );
    }

    public void testStructureInjection()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertTrue( sic.test() );
    }

    @Mixins( StructureInjectionMixin.class )
    public interface StructureInjectionComposite
        extends Composite
    {
        boolean test();
    }

    public abstract static class StructureInjectionMixin
        implements StructureInjectionComposite
    {
        @Structure CompositeBuilderFactory cbf;
        @Structure ObjectBuilderFactory obf;
        @Structure EntitySessionFactory esf;
        @Structure ServiceLocator sl;
        @Structure ModuleBinding mb;

        @Structure Qi4j qi4j;
        @Structure Qi4jSPI qi4jSpi;
        @Structure Qi4jRuntime qi4jRuntime;

        public boolean test()
        {
            return cbf != null &&
                   obf != null &&
                   esf != null &&
                   sl != null &&
                   qi4j != null &&
                   qi4jSpi != null &&
                   qi4jRuntime != null;
        }
    }
}

