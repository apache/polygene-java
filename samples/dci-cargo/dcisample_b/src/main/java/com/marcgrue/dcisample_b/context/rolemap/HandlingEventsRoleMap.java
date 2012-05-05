package com.marcgrue.dcisample_b.context.rolemap;

import com.marcgrue.dcisample_b.context.interaction.handling.registration.RegisterHandlingEvent;
import com.marcgrue.dcisample_b.data.aggregateroot.HandlingEventAggregateRoot;

/**
 * Handling Events Role Map
 *
 * shows what Roles the HandlingEventAggregateRoot can play.
 */
public interface HandlingEventsRoleMap
      extends HandlingEventAggregateRoot,

      RegisterHandlingEvent.EventRegistrarRole
{
}
