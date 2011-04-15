package org.qi4j.samples.cargo.app1.system.repositories;

import org.qi4j.samples.cargo.app1.model.handling.HandlingEvent;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;


/**
 *
 */
@Mixins(HandlingEventRepository.HandlingEventRepositoryMixin.class)
public interface HandlingEventRepository extends ServiceComposite {

    HandlingEvent findHandlingEventByIdentity(String identity);

    public abstract class HandlingEventRepositoryMixin
            implements HandlingEventRepository {
        @Structure
        private UnitOfWorkFactory uowf;

        public HandlingEvent findHandlingEventByIdentity(final String identity) {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get(HandlingEvent.class, identity);
        }
    }
}