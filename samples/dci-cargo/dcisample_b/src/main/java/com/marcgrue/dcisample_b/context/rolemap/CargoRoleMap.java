package com.marcgrue.dcisample_b.context.rolemap;

import com.marcgrue.dcisample_b.context.interaction.booking.specification.DeriveUpdatedRouteSpecification;
import com.marcgrue.dcisample_b.context.interaction.booking.routing.AssignCargoToRoute;
import com.marcgrue.dcisample_b.context.interaction.booking.routing.RegisterNewDestination;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.event.*;
import com.marcgrue.dcisample_b.data.entity.CargoEntity;

/**
 * Cargo Role Map
 *
 * Shows all the Roles that a CargoEntity can play.
 *
 * Note that the CargoEntity knows nothing about this map (and that Cargo (Data) knows nothing about CargoEntity).
 */
public interface CargoRoleMap
      extends CargoEntity,

      RegisterNewDestination.CargoInspectorRole,
      AssignCargoToRoute.CargoInspectorRole,
      DeriveUpdatedRouteSpecification.CargoInspectorRole,


      InspectCargoDeliveryStatus.DeliveryInspectorRole,

      InspectUnhandledCargo.DeliveryInspectorRole,
      InspectReceivedCargo.DeliveryInspectorRole,
      InspectLoadedCargo.DeliveryInspectorRole,
      InspectUnloadedCargo.DeliveryInspectorRole,
      InspectArrivedCargo.DeliveryInspectorRole,
      InspectCargoInCustoms.DeliveryInspectorRole,
      InspectClaimedCargo.DeliveryInspectorRole
{
}
