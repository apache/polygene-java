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

package org.apache.zest.library.eventsourcing.domain;

import java.util.function.Function;
import org.junit.Test;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.io.Outputs;
import org.apache.zest.io.Transforms;
import org.apache.zest.library.eventsourcing.bootstrap.EventsourcingAssembler;
import org.apache.zest.library.eventsourcing.domain.api.DomainEvent;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.apache.zest.library.eventsourcing.domain.factory.DomainEventCreationConcern;
import org.apache.zest.library.eventsourcing.domain.source.EventSource;
import org.apache.zest.library.eventsourcing.domain.source.memory.MemoryEventStoreService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import java.io.IOException;
import java.security.Principal;

/**
 * JAVADOC
 */
public class DomainEventTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        new EntityTestAssembler(  ).assemble( module );

        // START SNIPPET: assemblyDE
        new EventsourcingAssembler()
                .withDomainEvents()
                .withCurrentUserFromUOWPrincipal()
                .assemble(module);
        // END SNIPPET: assemblyDE

        // START SNIPPET: storeDE
        module.services( MemoryEventStoreService.class );
        // END SNIPPET: storeDE

        // START SNIPPET: concernDE
        module.entities( TestEntity.class ).withConcerns(DomainEventCreationConcern.class);
        // END SNIPPET: concernDE
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
        UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Change description" ));
        uow.setMetaInfo( administratorPrincipal );

        TestEntity entity = uow.newEntity( TestEntity.class );
        entity.changedDescription( "New description" );
        uow.complete();

        // Print events
        EventSource source = serviceFinder.findService( EventSource.class ).get();

        source.events( 0, Long.MAX_VALUE ).transferTo( Transforms.map( new Function<UnitOfWorkDomainEventsValue, String>()
                {
                    public String apply( UnitOfWorkDomainEventsValue unitOfWorkDomainEventsValue )
                    {
                        return unitOfWorkDomainEventsValue.toString();
                    }
                }, Outputs.systemOut() ));
    }

    // START SNIPPET: methodDE
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
    // END SNIPPET: methodDE
}
