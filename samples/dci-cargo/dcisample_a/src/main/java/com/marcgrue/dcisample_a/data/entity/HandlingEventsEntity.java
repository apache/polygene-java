package com.marcgrue.dcisample_a.data.entity;

import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvents;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvents;
import org.qi4j.api.entity.EntityComposite;

/**
 * HandlingEvent aggregate root?
 */
public interface HandlingEventsEntity
      extends EntityComposite,

      HandlingEvents
{
    public static final String HANDLING_EVENTS_ID = "Handling_events_id";
}
