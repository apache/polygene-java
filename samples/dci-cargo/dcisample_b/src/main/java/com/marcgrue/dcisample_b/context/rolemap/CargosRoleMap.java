package com.marcgrue.dcisample_b.context.rolemap;

import com.marcgrue.dcisample_b.context.interaction.booking.BookNewCargo;
import com.marcgrue.dcisample_b.data.aggregateroot.CargoAggregateRoot;

/**
 * Cargos Role Map
 *
 * Shows what Roles the CargoAggregateRoot can play.
 */
public interface CargosRoleMap
      extends CargoAggregateRoot,

      BookNewCargo.BookingSystemRole
{
}
