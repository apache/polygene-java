package com.marcgrue.dcisample_a.context.rolemap;

import com.marcgrue.dcisample_a.context.shipping.booking.BuildDeliverySnapshot;
import com.marcgrue.dcisample_a.context.shipping.booking.BuildDeliverySnapshot;
import com.marcgrue.dcisample_a.context.shipping.handling.InspectCargo;
import com.marcgrue.dcisample_a.context.shipping.handling.InspectCargo;
import com.marcgrue.dcisample_a.context.shipping.booking.BuildDeliverySnapshot;
import com.marcgrue.dcisample_a.context.shipping.handling.InspectCargo;
import com.marcgrue.dcisample_a.data.entity.HandlingEventEntity;

/**
 * Handling Event Role Map
 */
public interface HandlingEventRoleMap
      extends HandlingEventEntity,

      BuildDeliverySnapshot.HandlingEventRole,
      InspectCargo.CargoInspectorRole
{
}
