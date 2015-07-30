/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
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

package org.apache.zest.library.eventsourcing.domain.source.jdbm;

import java.io.IOException;
import java.security.Principal;
import org.junit.Test;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ImportedServiceDeclaration;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.functional.Function;
import org.apache.zest.io.Outputs;
import org.apache.zest.io.Transforms;
import org.apache.zest.library.eventsourcing.domain.api.DomainEvent;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.apache.zest.library.eventsourcing.domain.factory.CurrentUserUoWPrincipal;
import org.apache.zest.library.eventsourcing.domain.factory.DomainEventCreationConcern;
import org.apache.zest.library.eventsourcing.domain.factory.DomainEventFactoryService;
import org.apache.zest.library.eventsourcing.domain.source.EventSource;
import org.apache.zest.library.fileconfig.FileConfigurationService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class JdbmEventStoreServiceTest
        extends AbstractZestTest
    {
        @Override
        public void assemble( ModuleAssembly module ) throws AssemblyException
        {
            module.layer().application().setName( "JDBMEventStoreTest" );

            new EntityTestAssembler(  ).assemble( module );

            module.values( DomainEventValue.class, UnitOfWorkDomainEventsValue.class );
            module.services( FileConfigurationService.class );
            new OrgJsonValueSerializationAssembler().assemble( module );
            module.services( JdbmEventStoreService.class );
            module.services( DomainEventFactoryService.class );
            module.importedServices( CurrentUserUoWPrincipal.class ).importedBy( ImportedServiceDeclaration.NEW_OBJECT );
            module.objects( CurrentUserUoWPrincipal.class );

            module.entities( TestEntity.class ).withConcerns(DomainEventCreationConcern.class);
        }

        @Test
        public void testDomainEvent() throws UnitOfWorkCompletionException, IOException
        {
            UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Create entity" ));
            TestEntity entity = uow.newEntity( TestEntity.class );
            uow.complete();

            int count = 10;
            for (int i = 0; i < count; i++)
            {
                uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Change description" ));
                uow.setMetaInfo( new Principal()
                {
                    public String getName()
                    {
                        return "administrator";
                    }
                });

                entity = uow.get( entity );
                entity.changeDescription( "New description" );
                uow.complete();
            }

            EventSource source = (EventSource) module.findService( EventSource.class ).get();

            source.events( 0, Long.MAX_VALUE ).transferTo( Transforms.map( new Function<UnitOfWorkDomainEventsValue, String>()
                    {
                        public String map( UnitOfWorkDomainEventsValue unitOfWorkDomainEventsValue )
                        {
                            return unitOfWorkDomainEventsValue.toString();
                        }
                    }, Outputs.systemOut() ));
        }

        @Mixins( TestEntity.Mixin.class )
        public interface TestEntity
            extends EntityComposite
        {
            @UseDefaults
            Property<String> description();

            @DomainEvent
            void changeDescription(String newName);

            abstract class Mixin
                implements TestEntity
            {
                public void changeDescription( String newName )
                {
                    description().set( newName );
                }
            }
        }
    }
