/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.library.eventsourcing.bootstrap;

import org.apache.zest.bootstrap.Assemblers;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ImportedServiceDeclaration;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.eventsourcing.application.api.ApplicationEvent;
import org.apache.zest.library.eventsourcing.application.api.TransactionApplicationEvents;
import org.apache.zest.library.eventsourcing.application.factory.ApplicationEventFactoryService;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.apache.zest.library.eventsourcing.domain.factory.CurrentUserUoWPrincipal;
import org.apache.zest.library.eventsourcing.domain.factory.DomainEventFactoryService;

public class EventsourcingAssembler
    extends Assemblers.Visibility<EventsourcingAssembler>
{
    private boolean domainEvents;
    private boolean applicationEvents;

    private boolean uowPrincipal;

    public EventsourcingAssembler withDomainEvents()
    {
        domainEvents = true;
        return this;
    }

    public EventsourcingAssembler withApplicationEvents()
    {
        applicationEvents = true;
        return this;
    }

    public EventsourcingAssembler withCurrentUserFromUOWPrincipal()
    {
        uowPrincipal = true;
        return this;
    }

    @Override
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        if( domainEvents )
        {
            module.values( DomainEventValue.class, UnitOfWorkDomainEventsValue.class );
            module.services( DomainEventFactoryService.class).visibleIn(visibility() );
        }

        if( applicationEvents )
        {
            module.values( ApplicationEvent.class, TransactionApplicationEvents.class );
            module.services( ApplicationEventFactoryService.class ).visibleIn( visibility() );
        }

        if( uowPrincipal )
        {
            module.importedServices( CurrentUserUoWPrincipal.class )
                .importedBy( ImportedServiceDeclaration.NEW_OBJECT );
            module.objects( CurrentUserUoWPrincipal.class );
        }
    }
}
