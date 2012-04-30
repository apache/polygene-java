package com.marcgrue.dcisample_a.context.rolemap;

import com.marcgrue.dcisample_a.context.shipping.handling.RegisterHandlingEvent;
import com.marcgrue.dcisample_a.context.shipping.handling.RegisterHandlingEvent;
import com.marcgrue.dcisample_a.context.shipping.handling.RegisterHandlingEvent;
import com.marcgrue.dcisample_a.data.entity.HandlingEventsEntity;

/**
 * Handling Events Role Map
 */
public interface HandlingEventsRoleMap
      extends HandlingEventsEntity,

      RegisterHandlingEvent.HandlingEventFactoryRole
{
}
