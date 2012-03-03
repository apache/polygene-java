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

package org.qi4j.library.eventsourcing.domain;

import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Function;
import org.qi4j.io.Outputs;
import org.qi4j.io.Transforms;
import org.qi4j.library.eventsourcing.domain.api.DomainEvent;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.factory.CurrentUserUoWPrincipal;
import org.qi4j.library.eventsourcing.domain.factory.DomainEventCreationConcern;
import org.qi4j.library.eventsourcing.domain.factory.DomainEventFactoryService;
import org.qi4j.library.eventsourcing.domain.source.EventSource;
import org.qi4j.library.eventsourcing.domain.source.memory.MemoryEventStoreService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import java.io.IOException;
import java.security.Principal;

/**
 * JAVADOC
 */
public class DomainEventTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        new EntityTestAssembler(  ).assemble( module );

        module.values( DomainEventValue.class, UnitOfWorkDomainEventsValue.class );
        module.services( MemoryEventStoreService.class );
        module.services( DomainEventFactoryService.class );
        module.importedServices( CurrentUserUoWPrincipal.class ).importedBy( ImportedServiceDeclaration.NEW_OBJECT );
        module.objects( CurrentUserUoWPrincipal.class );

        module.entities( TestEntity.class ).withConcerns(DomainEventCreationConcern.class);
    }

    @Test
    public void testDomainEvent() throws UnitOfWorkCompletionException, IOException
    {
        // Set principal for the UoW
        Principal administratorPrincipal = new Principal()
        {
            public String getName()
            {
                return "administrator";
            }
        };

        // Perform UoW with usecase defined
        UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Change description" ));
        uow.setMetaInfo( administratorPrincipal );

        TestEntity entity = uow.newEntity( TestEntity.class );
        entity.changedDescription( "New description" );
        uow.complete();

        // Print events
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
        void changedDescription( String newName );

        abstract class Mixin
            implements TestEntity
        {
            public void changedDescription( String newName )
            {
                description().set( newName );
            }
        }
    }
}
