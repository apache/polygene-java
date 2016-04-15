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
package org.apache.zest.runtime.visibility;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;
import org.junit.Test;

public class VisibilityInUnitOfWorkTest
{
    @Test
    public void givenTwoModulesWithServiceAndEntityInOneAndEntityInOtherWhenOtherEntityAccessServiceWhichUsesItsEntityExpectServiceToHaveVisibility()
        throws Exception
    {
        Application underTest = createApplication();
        Module module = underTest.findModule( "layer1", "My Module" );
        ServiceReference<MyService> service = module.findService( MyService.class );
        service.get().create();
    }

    @Mixins( YourService.Mixin.class )
    public interface YourService
    {
        void create();

        YourEntity get();

        class Mixin
            implements YourService
        {
            @Structure
            private UnitOfWorkFactory uowf;

            @Override
            public void create()
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                YourEntity entity = uow.newEntity( YourEntity.class, "345" );
            }

            @Override
            public YourEntity get()
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                return uow.get( YourEntity.class, "345" );
            }
        }
    }

    public interface YourEntity
    {
    }

    @Mixins( MyEntity.Mixin.class )
    public interface MyEntity
    {
        void logic();

        class Mixin
            implements MyEntity
        {
            @Service
            private YourService service;

            @Override
            public void logic()
            {
                YourEntity result = service.get();
            }
        }
    }

    @Mixins( MyService.Mixin.class )
    public interface MyService
    {
        void create();

        class Mixin
            implements MyService
        {

            @Service
            private YourService service;

            @Structure
            private UnitOfWorkFactory uowf;

            @Override
            public void create()
            {
                try (UnitOfWork uow = uowf.newUnitOfWork())
                {
                    uow.newEntity( MyEntity.class, "123" );
                    MyEntity entity1 = uow.get( MyEntity.class, "123" );
                    service.create();
                    YourEntity entity2 = service.get();
                }
            }
        }
    }

    private Application createApplication()
        throws AssemblyException
    {
        Energy4Java zest = new Energy4Java();
        return zest.newApplication( appFactory -> {
            ApplicationAssembly appAssembly = appFactory.newApplicationAssembly();
            LayerAssembly layer1 = appAssembly.layer( "layer1" );
            ModuleAssembly myModule = layer1.module( "My Module" );
            ModuleAssembly yourModule = layer1.module( "Your Module" );
            ModuleAssembly infraModule = layer1.module( "Infra Module" );
            myModule.services( MyService.class );
            myModule.entities( MyEntity.class );
            new DefaultUnitOfWorkAssembler().assemble( myModule );
            yourModule.entities( YourEntity.class );
            yourModule.services( YourService.class ).visibleIn( Visibility.layer );
            new DefaultUnitOfWorkAssembler().assemble( yourModule );
            infraModule.services( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
            infraModule.services( UuidIdentityGeneratorService.class ).visibleIn( Visibility.layer );
            infraModule.services( OrgJsonValueSerializationService.class )
                .visibleIn( Visibility.layer )
                .taggedWith( ValueSerialization.Formats.JSON );
            new DefaultUnitOfWorkAssembler().assemble( infraModule );
            return appAssembly;
        } );
    }
}
