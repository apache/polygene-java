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

package org.apache.zest.runtime.injection;

import org.junit.Test;
import org.apache.zest.api.PolygeneAPI;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.service.ServiceFinder;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.spi.PolygeneSPI;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Test the @Structure annotation
 */
public class StructureInjectionTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( StructureInjectionComposite.class );
    }

    /**
     * Tests injected mixin for a CompositeBuilderFactory annotated with {@link @org.apache.zest.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForCompositeBuilderFactory()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected CompositeBuilderFactory", sic.getCompositeBuilderFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ObjectBuilderFactory annotated with {@link @org.apache.zest.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForObjectBuilderFactory()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected ObjectBuilderFactory", sic.getObjectFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a UnitOfWorkFactory annotated with {@link @org.apache.zest.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForUnitOfWorkFactory()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected UnitOfWorkFactory", sic.getUnitOfWorkFactory(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ServiceLocator annotated with {@link @org.apache.zest.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForServiceLocator()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected ServiceLocator", sic.getServiceLocator(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a ModuleBinding annotated with {@link @org.apache.zest.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForModuleBinding()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected Module", sic.getModule(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a Polygene annotated with {@link @org.apache.zest.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForPolygene()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected Polygene", sic.getPolygene(), is( notNullValue() ) );
    }

    /**
     * Tests injected mixin for a PolygeneSPI annotated with {@link @org.apache.zest.composite.scope.Structure}.
     */
    @Test
    public void injectedStructureForPolygeneSpi()
    {
        StructureInjectionComposite sic = transientBuilderFactory.newTransient( StructureInjectionComposite.class );
        assertThat( "Injected PolygeneSPI", sic.getPolygeneSpi(), is( notNullValue() ) );
    }

    @Mixins( StructureInjectionMixin.class )
    public interface StructureInjectionComposite
        extends TransientComposite
    {
        public TransientBuilderFactory getCompositeBuilderFactory();

        public ObjectFactory getObjectFactory();

        public UnitOfWorkFactory getUnitOfWorkFactory();

        public ServiceFinder getServiceLocator();

        public Module getModule();

        public PolygeneAPI getPolygene();

        public PolygeneSPI getPolygeneSpi();
    }

    public abstract static class StructureInjectionMixin
        implements StructureInjectionComposite
    {
        @Structure
        TransientBuilderFactory compositeBuilderFactory;

        @Structure
        ObjectFactory objectFactory;

        @Structure
        UnitOfWorkFactory unitOfWorkFactory;

        @Structure
        ServiceFinder serviceLocator;

        @Structure
        Module module;

        @Structure
        PolygeneAPI api;
        @Structure
        PolygeneSPI spi;

        public TransientBuilderFactory getCompositeBuilderFactory()
        {
            return compositeBuilderFactory;
        }

        public ObjectFactory getObjectFactory()
        {
            return objectFactory;
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

        public PolygeneAPI getPolygene()
        {
            return api;
        }

        public PolygeneSPI getPolygeneSpi()
        {
            return spi;
        }
    }
}

