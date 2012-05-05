package com.marcgrue.dcisample_b.data.aggregateroot;

import com.marcgrue.dcisample_b.data.factory.HandlingEventFactory;
import org.qi4j.api.entity.EntityComposite;

/**
 * HandlingEvent aggregate root
 *
 * An identified unique starting point to create HandlingEvents.
 *
 * HandlingEvents are only allowed to be created through this aggregate root. Can we enforce this?
 */
public interface HandlingEventAggregateRoot
      extends EntityComposite,

      HandlingEventFactory
{
    public static final String HANDLING_EVENTS_ID = "Handling_events_id";
}
