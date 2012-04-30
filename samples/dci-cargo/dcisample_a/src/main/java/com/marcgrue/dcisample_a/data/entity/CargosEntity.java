package com.marcgrue.dcisample_a.data.entity;

import com.marcgrue.dcisample_a.data.shipping.cargo.Cargos;
import org.qi4j.api.entity.EntityComposite;

/**
 * Cargo aggregate root?
 */
public interface CargosEntity
      extends EntityComposite,

      Cargos
{
    public static final String CARGOS_ID = "Cargos_id";
}
