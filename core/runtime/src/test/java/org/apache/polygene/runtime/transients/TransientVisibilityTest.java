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

package org.apache.polygene.runtime.transients;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.NoSuchTransientTypeException;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
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

public class TransientVisibilityTest
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromService service = module.findService( FromService.class ).get();
            service.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromServiceWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
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

    @Test
    public void givenFromEntityWhenAccessingBesideModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
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

    @Test
    public void givenFromEntityWhenAccessingBelowLayerVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromValue value = module.newValue( FromValue.class );
            value.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromValueWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromTransient transientt = module.newTransient( FromTransient.class );
            transientt.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromTransientWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
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
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.belowLayerVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingBelowModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.belowModuleVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveApplicationVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.aboveApplicationVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveLayerVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
            FromObject object = module.newObject( FromObject.class );
            object.aboveLayerVisible();
        } );
    }

    @Test
    public void givenFromObjectWhenAccessingAboveModuleVisibleExpectException()
    {
        assertThrows( NoSuchTransientTypeException.class, () -> {
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

            module.transients( ModuleApplicationVisible.class ).visibleIn( Visibility.application );
            module.transients( ModuleLayerVisible.class ).visibleIn( Visibility.layer );
            module.transients( ModuleModuleVisible.class ).visibleIn( Visibility.module );
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
            module.transients( BelowApplicationVisible.class ).visibleIn( Visibility.application );
            module.transients( BelowLayerVisible.class ).visibleIn( Visibility.layer );
            module.transients( BelowModuleVisible.class ).visibleIn( Visibility.module );

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
            module.transients( AboveApplicationVisible.class ).visibleIn( Visibility.application );
            module.transients( AboveLayerVisible.class ).visibleIn( Visibility.layer );
            module.transients( AboveModuleVisible.class ).visibleIn( Visibility.module );
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
            module.transients( BesideApplicationVisible.class ).visibleIn( Visibility.application );
            module.transients( BesideLayerVisible.class ).visibleIn( Visibility.layer );
            module.transients( BesideModuleVisible.class ).visibleIn( Visibility.module );
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
            TransientBuilder<ModuleApplicationVisible> builder = module.newTransientBuilder( ModuleApplicationVisible.class );
            builder.newInstance();
        }

        @Override
        public void moduleLayerVisible()
        {
            TransientBuilder<ModuleLayerVisible> builder = module.newTransientBuilder( ModuleLayerVisible.class );
            builder.newInstance();
        }

        @Override
        public void moduleModuleVisible()
        {
            TransientBuilder<ModuleModuleVisible> builder = module.newTransientBuilder( ModuleModuleVisible.class );
            builder.newInstance();
        }

        @Override
        public void besideApplicationVisible()
        {
            TransientBuilder<BesideApplicationVisible> builder = module.newTransientBuilder( BesideApplicationVisible.class );
            builder.newInstance();
        }

        @Override
        public void besideLayerVisible()
        {
            TransientBuilder<BesideLayerVisible> builder = module.newTransientBuilder( BesideLayerVisible.class );
            builder.newInstance();
        }

        @Override
        public void besideModuleVisible()
        {
            TransientBuilder<BesideModuleVisible> builder = module.newTransientBuilder( BesideModuleVisible.class );
            builder.newInstance();
        }

        @Override
        public void belowApplicationVisible()
        {
            TransientBuilder<BelowApplicationVisible> builder = module.newTransientBuilder( BelowApplicationVisible.class );
            builder.newInstance();
        }

        @Override
        public void belowLayerVisible()
        {
            TransientBuilder<BelowLayerVisible> builder = module.newTransientBuilder( BelowLayerVisible.class );
            builder.newInstance();
        }

        @Override
        public void belowModuleVisible()
        {
            TransientBuilder<BelowModuleVisible> builder = module.newTransientBuilder( BelowModuleVisible.class );
            builder.newInstance();
        }

        @Override
        public void aboveApplicationVisible()
        {
            TransientBuilder<AboveApplicationVisible> builder = module.newTransientBuilder( AboveApplicationVisible.class );
            builder.newInstance();
        }

        @Override
        public void aboveLayerVisible()
        {
            TransientBuilder<AboveLayerVisible> builder = module.newTransientBuilder( AboveLayerVisible.class );
            builder.newInstance();
        }

        @Override
        public void aboveModuleVisible()
        {
            TransientBuilder<AboveModuleVisible> builder = module.newTransientBuilder( AboveModuleVisible.class );
            builder.newInstance();
        }
    }

    public interface ModuleApplicationVisible extends TransientComposite
    {
    }

    public interface ModuleLayerVisible extends TransientComposite
    {
    }

    public interface ModuleModuleVisible extends TransientComposite
    {
    }

    public interface BesideApplicationVisible extends TransientComposite
    {
    }

    public interface BesideLayerVisible extends TransientComposite
    {
    }

    public interface BesideModuleVisible extends TransientComposite
    {
    }

    public interface BelowApplicationVisible extends TransientComposite
    {
    }

    public interface BelowLayerVisible extends TransientComposite
    {
    }

    public interface BelowModuleVisible extends TransientComposite
    {
    }

    public interface AboveApplicationVisible extends TransientComposite
    {
    }

    public interface AboveLayerVisible extends TransientComposite
    {
    }

    public interface AboveModuleVisible extends TransientComposite
    {
    }
}
