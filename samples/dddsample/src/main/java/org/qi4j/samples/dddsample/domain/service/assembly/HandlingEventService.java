package org.qi4j.samples.dddsample.domain.service.assembly;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.service.DomainEventNotifier;

@Mixins( HandlingEventService.HandlingEventServiceMixin.class )
interface HandlingEventService
    extends org.qi4j.samples.dddsample.domain.service.HandlingEventService, ServiceComposite
{
    class HandlingEventServiceMixin
        implements org.qi4j.samples.dddsample.domain.service.HandlingEventService
    {
        @Service
        private ServiceReference<DomainEventNotifier> domainEventNotifierRef;

        public void register( final HandlingEvent event )
        {
//             NOTE:
//               The cargo instance that's loaded and associated with the handling event is
//               in an inconsitent state, because the cargo delivery history's collection of
//               events does not contain the event created here. However, this is not a problem,
//               because cargo is in a different aggregate from handling event.
//
//               The rules of an aggregate dictate that all consistency rules within the aggregate
//               are enforced synchronously in the transaction, but consistency rules of other aggregates
//               are enforced by asynchronous updates, after the commit of this transaction.
            // TODO: Qi4j doesn't have save.
//            handlingEventRepository.save( event );

            DomainEventNotifier domainEventNotifier = domainEventNotifierRef.get();
            domainEventNotifier.cargoWasHandled( event );
        }
    }
}