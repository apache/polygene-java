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

package org.qi4j.runtime.injection;

import org.junit.Test;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test the @Structure annotation
 */
public class StructureInjectionTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( StructureInjectionComposite.class );
    }

    /**
     * Tests injected mixin for a CompositeBuilderFactory annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForCompositeBuilderFactory()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected CompositeBuilderFactory", sic.getCompositeBuilderFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ObjectBuilderFactory annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForObjectBuilderFactory()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected ObjectBuilderFactory", sic.getObjectBuilderFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a UnitOfWorkFactory annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForUnitOfWorkFactory()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected UnitOfWorkFactory", sic.getUnitOfWorkFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ServiceLocator annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForServiceLocator()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected ServiceLocator", sic.getServiceLocator(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ModuleBinding annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForModuleBinding()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected Module", sic.getModule(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a Qi4j annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForQi4j()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected Qi4j", sic.getQi4j(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a Qi4jSpi annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForQi4jSpi()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected Qi4jSpi", sic.getQi4jSpi(), is( notNullValue() ) );
    }

    @Mixins( StructureInjectionMixin.class )
    public interface StructureInjectionComposite
        extends TransientComposite
    {
        public TransientBuilderFactory getCompositeBuilderFactory();

        public ObjectBuilderFactory getObjectBuilderFactory();

        public UnitOfWorkFactory getUnitOfWorkFactory();

        public ServiceFinder getServiceLocator();

        public Module getModule();

        public Qi4j getQi4j();

        public Qi4jSPI getQi4jSpi();
    }

    public abstract static class StructureInjectionMixin
        implements StructureInjectionComposite
    {
        @Structure
        TransientBuilderFactory compositeBuilderFactory;

        @Structure
        ObjectBuilderFactory objectBuilderFactory;

        @Structure
        UnitOfWorkFactory unitOfWorkFactory;

        @Structure
        ServiceFinder serviceLocator;

        @Structure
        Module module;

        @Structure
        Qi4j qi4j;
        @Structure
        Qi4jSPI qi4jSpi;

        public TransientBuilderFactory getCompositeBuilderFactory()
        {
            return compositeBuilderFactory;
        }

        public ObjectBuilderFactory getObjectBuilderFactory()
        {
            return objectBuilderFactory;
        }

        public UnitOfWorkFactory getUnitOfWorkFactory()
        {
            return unitOfWorkFactory;
        }

        public ServiceFinder getServiceLocator()
        {
            return serviceLocator;
        }

        public Module getModule()
        {
            return module;
        }

        public Qi4j getQi4j()
        {
            return qi4j;
        }

        public Qi4jSPI getQi4jSpi()
        {
            return qi4jSpi;
        }
    }
}

