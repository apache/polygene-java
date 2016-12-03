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

package org.apache.zest.runtime.entity;

import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.identity.StringIdentity;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.NoSuchEntityTypeException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.ApplicationAssemblerAdapter;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.EntityTestAssembler;

public class EntityVisibilityTest
{

    public static final Identity TEST_IDENTITY = new StringIdentity( "123" );

    private Energy4Java zest;
    private Module module;
    private Application app;
    private UnitOfWorkFactory uowf;

    @Before
    public void setup()
        throws Exception
    {
        zest = new Energy4Java();

        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // Layer Above
                  {
                      new AboveAssembler()
                  }
                },
                { // Layer From
                  { // From Module
                    new FromAssembler(),
                  },
                  { // Beside Module
                    new BesideAssembler()
                  }
                },
                { // Layer Below
                  {
                      new BelowAssembler()
                  }
                }
            };
        app = zest.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        module = app.findModule( "From Layer", "From" );
        uowf = module.unitOfWorkFactory();
    }

    @After
    public void tearDown()
        throws Exception
    {
        app.passivate();
    }

    @Test
    public void givenFromServiceWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.moduleApplicationVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.moduleLayerVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.moduleModuleVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.besideApplicationVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.besideLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromServiceWhenAccessingBesideModuleVisibleExpectException()
    {
        FromService service = module.findService( FromService.class ).get();
        service.besideModuleVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.belowApplicationVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromServiceWhenAccessingBelowLayerVisibleExpectException()
    {
        FromService service = module.findService( FromService.class ).get();
        service.belowLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromServiceWhenAccessingBelowModuleVisibleExpectException()
    {
        FromService service = module.findService( FromService.class ).get();
        service.belowModuleVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromServiceWhenAccessingAboveApplicationVisibleExpectException()
    {
        FromService service = module.findService( FromService.class ).get();
        service.aboveApplicationVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromServiceWhenAccessingAboveLayerVisibleExpectException()
    {
        FromService service = module.findService( FromService.class ).get();
        service.aboveLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromServiceWhenAccessingAboveModuleVisibleExpectException()
    {
        FromService service = module.findService( FromService.class ).get();
        service.aboveModuleVisible();
    }

    @Test
    public void givenFromEntityWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.moduleApplicationVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.moduleLayerVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.moduleModuleVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.besideApplicationVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.besideLayerVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromEntityWhenAccessingBesideModuleVisibleExpectException()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.besideModuleVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromEntityWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.belowApplicationVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromEntityWhenAccessingBelowLayerVisibleExpectException()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.belowLayerVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromEntityWhenAccessingBelowModuleVisibleExpectException()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.belowModuleVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromEntityWhenAccessingAboveApplicationVisibleExpectException()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.aboveApplicationVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromEntityWhenAccessingAboveLayerVisibleExpectException()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.aboveLayerVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromEntityWhenAccessingAboveModuleVisibleExpectException()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY);
            entity.aboveModuleVisible();
        }
        finally
        {
            if( unitOfWork.isOpen() )
            {
                unitOfWork.discard();
            }
        }
    }

    @Test
    public void givenFromValueWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.moduleApplicationVisible();
    }

    @Test
    public void givenFromValueWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.moduleLayerVisible();
    }

    @Test
    public void givenFromValueWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.moduleModuleVisible();
    }

    @Test
    public void givenFromValueWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.besideApplicationVisible();
    }

    @Test
    public void givenFromValueWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.besideLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromValueWhenAccessingBesideModuleVisibleExpectException()
    {
        FromValue value = module.newValue( FromValue.class );
        value.besideModuleVisible();
    }

    @Test
    public void givenFromValueWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.belowApplicationVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromValueWhenAccessingBelowLayerVisibleExpectException()
    {
        FromValue value = module.newValue( FromValue.class );
        value.belowLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromValueWhenAccessingBelowModuleVisibleExpectException()
    {
        FromValue value = module.newValue( FromValue.class );
        value.belowModuleVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromValueWhenAccessingAboveApplicationVisibleExpectException()
    {
        FromValue value = module.newValue( FromValue.class );
        value.aboveApplicationVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromValueWhenAccessingAboveLayerVisibleExpectException()
    {
        FromValue value = module.newValue( FromValue.class );
        value.aboveLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromValueWhenAccessingAboveModuleVisibleExpectException()
    {
        FromValue value = module.newValue( FromValue.class );
        value.aboveModuleVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.moduleApplicationVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.moduleLayerVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.moduleModuleVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.besideApplicationVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.besideLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromTransientWhenAccessingBesideModuleVisibleExpectException()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.besideModuleVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.belowApplicationVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromTransientWhenAccessingBelowLayerVisibleExpectException()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.belowLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromTransientWhenAccessingBelowModuleVisibleExpectException()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.belowModuleVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromTransientWhenAccessingAboveApplicationVisibleExpectException()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.aboveApplicationVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromTransientWhenAccessingAboveLayerVisibleExpectException()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.aboveLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromTransientWhenAccessingAboveModuleVisibleExpectException()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.aboveModuleVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.moduleApplicationVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingModuleLayerVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.moduleLayerVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingModuleModuleVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.moduleModuleVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingBesideApplicationVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.besideApplicationVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingBesideLayerVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.besideLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromObjectWhenAccessingBesideModuleVisibleExpectException()
    {
        FromObject object = module.newObject( FromObject.class );
        object.besideModuleVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.belowApplicationVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromObjectWhenAccessingBelowLayerVisibleExpectException()
    {
        FromObject object = module.newObject( FromObject.class );
        object.belowLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromObjectWhenAccessingBelowModuleVisibleExpectException()
    {
        FromObject object = module.newObject( FromObject.class );
        object.belowModuleVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromObjectWhenAccessingAboveApplicationVisibleExpectException()
    {
        FromObject object = module.newObject( FromObject.class );
        object.aboveApplicationVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromObjectWhenAccessingAboveLayerVisibleExpectException()
    {
        FromObject object = module.newObject( FromObject.class );
        object.aboveLayerVisible();
    }

    @Test( expected = NoSuchEntityTypeException.class )
    public void givenFromObjectWhenAccessingAboveModuleVisibleExpectException()
    {
        FromObject object = module.newObject( FromObject.class );
        object.aboveModuleVisible();
    }

    private static class FromAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.layer().setName( "From Layer" );
            module.setName( "From" );
            module.services( FromService.class );
            module.entities( FromEntity.class );
            module.transients( FromTransient.class );
            module.values( FromValue.class );
            module.objects( FromObject.class );

            module.entities( ModuleApplicationVisible.class ).visibleIn( Visibility.application );
            module.entities( ModuleLayerVisible.class ).visibleIn( Visibility.layer );
            module.entities( ModuleModuleVisible.class ).visibleIn( Visibility.module );
            new DefaultUnitOfWorkAssembler().assemble( module );
        }
    }

    private static class BelowAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.layer().setName( "Below Layer" );
            module.setName( "Below" );
            module.entities( BelowApplicationVisible.class ).visibleIn( Visibility.application );
            module.entities( BelowLayerVisible.class ).visibleIn( Visibility.layer );
            module.entities( BelowModuleVisible.class ).visibleIn( Visibility.module );

            new EntityTestAssembler().visibleIn( Visibility.application ).assemble( module );
            new DefaultUnitOfWorkAssembler().assemble( module );
        }
    }

    private static class AboveAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.layer().setName( "Above Layer" );
            module.setName( "Above" );
            module.entities( AboveApplicationVisible.class ).visibleIn( Visibility.application );
            module.entities( AboveLayerVisible.class ).visibleIn( Visibility.layer );
            module.entities( AboveModuleVisible.class ).visibleIn( Visibility.module );
            new DefaultUnitOfWorkAssembler().assemble( module );
        }
    }

    private static class BesideAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.setName( "Beside" );
            module.entities( BesideApplicationVisible.class ).visibleIn( Visibility.application );
            module.entities( BesideLayerVisible.class ).visibleIn( Visibility.layer );
            module.entities( BesideModuleVisible.class ).visibleIn( Visibility.module );
            new DefaultUnitOfWorkAssembler().assemble( module );
        }
    }

    @Mixins( Mixin.class )
    public interface From
    {
        void moduleApplicationVisible();

        void moduleLayerVisible();

        void moduleModuleVisible();

        void besideApplicationVisible();

        void besideLayerVisible();

        void besideModuleVisible();

        void belowApplicationVisible();

        void belowLayerVisible();

        void belowModuleVisible();

        void aboveApplicationVisible();

        void aboveLayerVisible();

        void aboveModuleVisible();
    }

    public interface FromValue extends From, ValueComposite
    {
    }

    public interface FromEntity extends From, EntityComposite
    {
    }

    public interface FromService extends From, ServiceComposite
    {
    }

    public interface FromTransient extends From, TransientComposite
    {
    }

    public static class FromObject extends Mixin
    {
    }

    public abstract static class Mixin
        implements From
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public void moduleApplicationVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                ModuleApplicationVisible entity = uow.newEntity( ModuleApplicationVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void moduleLayerVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                ModuleLayerVisible entity = uow.newEntity( ModuleLayerVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void moduleModuleVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                ModuleModuleVisible entity = uow.newEntity( ModuleModuleVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void besideApplicationVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BesideApplicationVisible entity = uow.newEntity( BesideApplicationVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void besideLayerVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BesideLayerVisible entity = uow.newEntity( BesideLayerVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void besideModuleVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BesideModuleVisible entity = uow.newEntity( BesideModuleVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void belowApplicationVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BelowApplicationVisible entity = uow.newEntity( BelowApplicationVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void belowLayerVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BelowLayerVisible entity = uow.newEntity( BelowLayerVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void belowModuleVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                BelowModuleVisible entity = uow.newEntity( BelowModuleVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void aboveApplicationVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                AboveApplicationVisible entity = uow.newEntity( AboveApplicationVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void aboveLayerVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                AboveLayerVisible entity = uow.newEntity( AboveLayerVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        @Override
        public void aboveModuleVisible()
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                AboveModuleVisible entity = uow.newEntity( AboveModuleVisible.class );
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }
    }

    public interface ModuleApplicationVisible extends EntityComposite
    {
    }

    public interface ModuleLayerVisible extends EntityComposite
    {
    }

    public interface ModuleModuleVisible extends EntityComposite
    {
    }

    public interface BesideApplicationVisible extends EntityComposite
    {
    }

    public interface BesideLayerVisible extends EntityComposite
    {
    }

    public interface BesideModuleVisible extends EntityComposite
    {
    }

    public interface BelowApplicationVisible extends EntityComposite
    {
    }

    public interface BelowLayerVisible extends EntityComposite
    {
    }

    public interface BelowModuleVisible extends EntityComposite
    {
    }

    public interface AboveApplicationVisible extends EntityComposite
    {
    }

    public interface AboveLayerVisible extends EntityComposite
    {
    }

    public interface AboveModuleVisible extends EntityComposite
    {
    }
}
