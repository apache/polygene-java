package org.qi4j.library.eventsourcing.bootstrap;

import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.eventsourcing.application.api.ApplicationEvent;
import org.qi4j.library.eventsourcing.application.api.TransactionApplicationEvents;
import org.qi4j.library.eventsourcing.application.factory.ApplicationEventFactoryService;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.factory.CurrentUserUoWPrincipal;
import org.qi4j.library.eventsourcing.domain.factory.DomainEventFactoryService;


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

        if (domainEvents)
        {
            module.values(DomainEventValue.class, UnitOfWorkDomainEventsValue.class);
            module.services(DomainEventFactoryService.class).visibleIn(visibility());
        }

        if (applicationEvents)
        {
            module.values(ApplicationEvent.class, TransactionApplicationEvents.class);
            module.services(ApplicationEventFactoryService.class).visibleIn(visibility());
        }

        if (uowPrincipal)
        {
            module.importedServices(CurrentUserUoWPrincipal.class).importedBy(ImportedServiceDeclaration.NEW_OBJECT);
            module.objects(CurrentUserUoWPrincipal.class);
        }

    }
}
