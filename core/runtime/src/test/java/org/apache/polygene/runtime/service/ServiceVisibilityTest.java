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

package org.apache.polygene.runtime.service;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.NoSuchServiceTypeException;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.ApplicationAssemblerAdapter;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceVisibilityTest
{
    public static final Identity TEST_IDENTITY = StringIdentity.identityOf( "123" );

    private Energy4Java polygene;
    private Module module;
    private Application app;
    private UnitOfWorkFactory uowf;

    @BeforeEach
    public void setup()
        throws Exception
    {
        polygene = new Energy4Java();

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
        app = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        app.activate();
        module = app.findModule( "From Layer", "From" );
        uowf = module.unitOfWorkFactory();
    }

    @AfterEach
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

    @Test
    public void givenFromServiceWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.besideModuleVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromService service = module.findService( FromService.class ).get();
        service.belowApplicationVisible();
    }

    @Test
    public void givenFromServiceWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.aboveModuleVisible();
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingModuleApplicationVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
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
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
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
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
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
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
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
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
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

    @Test
    public void givenFromEntityWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.besideModuleVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        UnitOfWork unitOfWork = uowf.newUnitOfWork();
        try
        {
            FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
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

    @Test
    public void givenFromEntityWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.belowLayerVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.belowModuleVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.aboveApplicationVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.aboveLayerVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
    }

    @Test
    public void givenFromEntityWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                FromEntity entity = unitOfWork.newEntity( FromEntity.class, TEST_IDENTITY );
                entity.aboveModuleVisible();
            }
            finally
            {
                if( unitOfWork.isOpen() )
                {
                    unitOfWork.discard();
                }
            }
        } );
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

    @Test
    public void givenFromValueWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.besideModuleVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromValue value = module.newValue( FromValue.class );
        value.belowApplicationVisible();
    }

    @Test
    public void givenFromValueWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.aboveModuleVisible();
        } );
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

    @Test
    public void givenFromTransientWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.besideModuleVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromTransient transientt = module.newTransient( FromTransient.class );
        transientt.belowApplicationVisible();
    }

    @Test
    public void givenFromTransientWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.aboveModuleVisible();
        } );
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

    @Test
    public void givenFromObjectWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.besideModuleVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingBelowApplicationVisibleExpectSuccess()
    {
        FromObject object = module.newObject( FromObject.class );
        object.belowApplicationVisible();
    }

    @Test
    public void givenFromObjectWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchServiceTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.aboveModuleVisible();
        } );
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

            module.services( ModuleApplicationVisible.class ).visibleIn( Visibility.application );
            module.services( ModuleLayerVisible.class ).visibleIn( Visibility.layer );
            module.services( ModuleModuleVisible.class ).visibleIn( Visibility.module );
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
            module.services( BelowApplicationVisible.class ).visibleIn( Visibility.application );
            module.services( BelowLayerVisible.class ).visibleIn( Visibility.layer );
            module.services( BelowModuleVisible.class ).visibleIn( Visibility.module );

            new EntityTestAssembler().visibleIn( Visibility.application ).assemble( module );
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
            module.services( AboveApplicationVisible.class ).visibleIn( Visibility.application );
            module.services( AboveLayerVisible.class ).visibleIn( Visibility.layer );
            module.services( AboveModuleVisible.class ).visibleIn( Visibility.module );
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
            module.services( BesideApplicationVisible.class ).visibleIn( Visibility.application );
            module.services( BesideLayerVisible.class ).visibleIn( Visibility.layer );
            module.services( BesideModuleVisible.class ).visibleIn( Visibility.module );
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
        private Module module;

        @Override
        public void moduleApplicationVisible()
        {
            ModuleApplicationVisible service = module.findService( ModuleApplicationVisible.class ).get();
        }

        @Override
        public void moduleLayerVisible()
        {
            ModuleLayerVisible service = module.findService( ModuleLayerVisible.class ).get();
        }

        @Override
        public void moduleModuleVisible()
        {
            ModuleModuleVisible service = module.findService( ModuleModuleVisible.class ).get();
        }

        @Override
        public void besideApplicationVisible()
        {
            BesideApplicationVisible service = module.findService( BesideApplicationVisible.class ).get();
        }

        @Override
        public void besideLayerVisible()
        {
            BesideLayerVisible service = module.findService( BesideLayerVisible.class ).get();
        }

        @Override
        public void besideModuleVisible()
        {
            BesideModuleVisible service = module.findService( BesideModuleVisible.class ).get();
        }

        @Override
        public void belowApplicationVisible()
        {
            BelowApplicationVisible service = module.findService( BelowApplicationVisible.class ).get();
        }

        @Override
        public void belowLayerVisible()
        {
            BelowLayerVisible service = module.findService( BelowLayerVisible.class ).get();
        }

        @Override
        public void belowModuleVisible()
        {
            BelowModuleVisible service = module.findService( BelowModuleVisible.class ).get();
        }

        @Override
        public void aboveApplicationVisible()
        {
            AboveApplicationVisible service = module.findService( AboveApplicationVisible.class ).get();
        }

        @Override
        public void aboveLayerVisible()
        {
            AboveLayerVisible service = module.findService( AboveLayerVisible.class ).get();
        }

        @Override
        public void aboveModuleVisible()
        {
            AboveModuleVisible service = module.findService( AboveModuleVisible.class ).get();
        }
    }

    public interface ModuleApplicationVisible extends ServiceComposite
    {
    }

    public interface ModuleLayerVisible extends ServiceComposite
    {
    }

    public interface ModuleModuleVisible extends ServiceComposite
    {
    }

    public interface BesideApplicationVisible extends ServiceComposite
    {
    }

    public interface BesideLayerVisible extends ServiceComposite
    {
    }

    public interface BesideModuleVisible extends ServiceComposite
    {
    }

    public interface BelowApplicationVisible extends ServiceComposite
    {
    }

    public interface BelowLayerVisible extends ServiceComposite
    {
    }

    public interface BelowModuleVisible extends ServiceComposite
    {
    }

    public interface AboveApplicationVisible extends ServiceComposite
    {
    }

    public interface AboveLayerVisible extends ServiceComposite
    {
    }

    public interface AboveModuleVisible extends ServiceComposite
    {
    }
}
