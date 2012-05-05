package com.marcgrue.dcisample_a.context.rolemap;

import com.marcgrue.dcisample_a.context.shipping.booking.BookNewCargo;
import com.marcgrue.dcisample_a.data.entity.CargoEntity;

/**
 * Cargo Role Map
 */
public interface CargoRoleMap
      extends CargoEntity,

      BookNewCargo.RoutingFacadeRole
{
}
