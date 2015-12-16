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

package org.apache.zest.library.eventsourcing.domain.rest.server;

import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ImportedServiceDeclaration;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.library.eventsourcing.domain.api.DomainEvent;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.apache.zest.library.eventsourcing.domain.factory.CurrentUserUoWPrincipal;
import org.apache.zest.library.eventsourcing.domain.factory.DomainEventCreationConcern;
import org.apache.zest.library.eventsourcing.domain.factory.DomainEventFactoryService;
import org.apache.zest.library.eventsourcing.domain.source.EventSource;
import org.apache.zest.library.eventsourcing.domain.source.memory.MemoryEventStoreService;
import org.apache.zest.test.EntityTestAssembler;
import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import java.security.Principal;

/**
 * Start simple web server that exposes the Restlet resource. Test through browser.
 */
public class DomainEventSourceResourceSample
{
    public static void main( String[] args ) throws Exception
    {
        Component component = new Component();
        component.getServers().add( Protocol.HTTP, 8080 );

        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                new EntityTestAssembler().assemble( module );

                module.values( DomainEventValue.class, UnitOfWorkDomainEventsValue.class );
                module.services( MemoryEventStoreService.class ).taggedWith( "domain" );
                module.services( DomainEventFactoryService.class );
                module.importedServices( CurrentUserUoWPrincipal.class ).importedBy( ImportedServiceDeclaration.NEW_OBJECT );
                module.objects( CurrentUserUoWPrincipal.class );

                module.objects( DomainEventSourceResource.class, PingResource.class );

                module.entities( TestEntity.class ).withConcerns( DomainEventCreationConcern.class );
            }
        };

        component.getDefaultHost().attach( "/events", new TestApplication( assembler ) );
        component.getDefaultHost().attach( "/ping", assembler.module().newObject( PingResource.class ) );
        component.start();

        generateTestData(assembler.module().unitOfWorkFactory());
    }

    private static void generateTestData(UnitOfWorkFactory unitOfWorkFactory) throws UnitOfWorkCompletionException
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
        for (int i = 0; i < 43; i++)
        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "Change description "+(i+1) ));
            uow.setMetaInfo( administratorPrincipal );

            TestEntity entity = uow.newEntity( TestEntity.class );
            entity.changedDescription( "New description" );
            uow.complete();
        }
    }

    static class TestApplication
        extends Application
    {
        private final SingletonAssembler assembler;

        TestApplication(SingletonAssembler assembler)
        {
            this.assembler = assembler;
        }

        @Override
        public Restlet createInboundRoot()
        {
            getTunnelService().setExtensionsTunnel( true );
            return assembler.module().newObject(DomainEventSourceResource.class  );
        }
    }


    @Mixins(TestEntity.Mixin.class)
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

    // Used to create more events
    public static class PingResource
        extends Restlet
    {
        @Structure
        UnitOfWorkFactory unitOfWorkFactory;

        @Service
        EventSource eventSource;

        @Override
        public void handle( Request request, Response response )
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
            try
            {
                UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "Change description "+(eventSource.count()) ));
                uow.setMetaInfo( administratorPrincipal );

                TestEntity entity = uow.newEntity( TestEntity.class );
                entity.changedDescription( "New description" );
                uow.complete();

                response.setEntity( new StringRepresentation( "Event created" ) );
                response.setStatus( Status.SUCCESS_OK );
            } catch (UnitOfWorkCompletionException e)
            {
                throw new ResourceException(e);
            }
        }
    }
}
