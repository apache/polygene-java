package com.marcgrue.dcisample_a.context.rolemap;

import com.marcgrue.dcisample_a.context.shipping.booking.BookNewCargo;
import com.marcgrue.dcisample_a.data.entity.CargosEntity;

/**
 * Cargos Role Map
 */
public interface CargosRoleMap
      extends CargosEntity,

      BookNewCargo.CargoFactoryRole
{
}
