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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.Qi4j;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.service.ServiceLocator;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test the @Structure annotation
 */
public class StructureInjectionTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( StructureInjectionComposite.class );
    }

    /**
     * Tests injected mixin for a CompositeBuilderFactory annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForCompositeBuilderFactory()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertThat( "Injected CompositeBuilderFactory", sic.getCompositeBuilderFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ObjectBuilderFactory annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForObjectBuilderFactory()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertThat( "Injected ObjectBuilderFactory", sic.getObjectBuilderFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a UnitOfWorkFactory annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForUnitOfWorkFactory()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertThat( "Injected UnitOfWorkFactory", sic.getUnitOfWorkFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ServiceLocator annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForServiceLocator()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertThat( "Injected ServiceLocator", sic.getServiceLocator(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ModuleBinding annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForModuleBinding()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertThat( "Injected ModuleBinding", sic.getModuleBinding(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a Qi4j annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForQi4j()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertThat( "Injected Qi4j", sic.getQi4j(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a Qi4jSpi annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForQi4jSpi()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertThat( "Injected Qi4jSpi", sic.getQi4jSpi(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a Qi4jRuntime annotated with {@link @org.qi4j.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForQi4jRuntime()
    {
        StructureInjectionComposite sic = compositeBuilderFactory.newComposite( StructureInjectionComposite.class );
        assertThat( "Injected Qi4jRuntime", sic.getQi4jRuntime(), is( notNullValue() ) );
    }

    @Mixins( StructureInjectionMixin.class )
    public interface StructureInjectionComposite
        extends Composite
    {
        public CompositeBuilderFactory getCompositeBuilderFactory();

        public ObjectBuilderFactory getObjectBuilderFactory();

        public UnitOfWorkFactory getUnitOfWorkFactory();

        public ServiceLocator getServiceLocator();

        public ModuleBinding getModuleBinding();

        public Qi4j getQi4j();

        public Qi4jSPI getQi4jSpi();

        public Qi4jRuntime getQi4jRuntime();
    }

    public abstract static class StructureInjectionMixin
        implements StructureInjectionComposite
    {
        @Structure CompositeBuilderFactory compositeBuilderFactory;
        @Structure ObjectBuilderFactory objectBuilderFactory;
        @Structure UnitOfWorkFactory unitOfWorkFactory;
        @Structure ServiceLocator serviceLocator;
        @Structure ModuleBinding moduleBinding;

        @Structure Qi4j qi4j;
        @Structure Qi4jSPI qi4jSpi;
        @Structure Qi4jRuntime qi4jRuntime;


        public CompositeBuilderFactory getCompositeBuilderFactory()
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

        public ServiceLocator getServiceLocator()
        {
            return serviceLocator;
        }

        public ModuleBinding getModuleBinding()
        {
            return moduleBinding;
        }

        public Qi4j getQi4j()
        {
            return qi4j;
        }

        public Qi4jSPI getQi4jSpi()
        {
            return qi4jSpi;
        }

        public Qi4jRuntime getQi4jRuntime()
        {
            return qi4jRuntime;
        }
    }
}

