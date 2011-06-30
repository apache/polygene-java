package org.qi4j.samples.cargo.app1.model.handling;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.samples.cargo.app1.system.repositories.HandlingEventRepository;

import java.util.List;

/**
 *
 */
@Mixins( HandlingHistory.HandlingHistoryMixin.class )
public interface HandlingHistory extends ValueComposite
{

    HandlingEvent mostRecentlyCompletedEvent();

    public interface State
    {
        Property<List<String>> handlingEventIdentities();
    }

    public abstract class HandlingHistoryMixin
        implements HandlingHistory
    {
        @This
        private State state;

        @Structure
        private HandlingEventRepository handlingEventRepository;

        public HandlingEvent mostRecentlyCompletedEvent()
        {
            List<String> identities = state.handlingEventIdentities().get();
            String id = identities.get( identities.size() - 1 );
            return handlingEventRepository.findHandlingEventByIdentity( id );
        }
    }
}