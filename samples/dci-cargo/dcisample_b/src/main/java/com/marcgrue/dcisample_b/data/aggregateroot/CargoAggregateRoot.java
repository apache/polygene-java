package com.marcgrue.dcisample_b.data.aggregateroot;

import com.marcgrue.dcisample_b.data.factory.CargoFactory;
import org.qi4j.api.entity.EntityComposite;

/**
 * Cargo aggregate root
 *
 * An identified unique starting point to create Cargos.
 *
 * Cargos are only allowed to be created through this aggregate root. Can we enforce this?
 */
public interface CargoAggregateRoot
      extends EntityComposite,

      CargoFactory
{
    public static final String CARGOS_ID = "Cargos_id";
}
