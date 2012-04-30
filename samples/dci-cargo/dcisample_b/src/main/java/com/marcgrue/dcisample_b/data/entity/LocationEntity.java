package com.marcgrue.dcisample_b.data.entity;

import com.marcgrue.dcisample_b.data.structure.location.Location;
import org.qi4j.api.entity.EntityComposite;

/**
 * Location entity
 *
 * Locations have been created outside the shipping application context so we don't have any
 * separate aggregate root to create those from.
 */
public interface LocationEntity
      extends EntityComposite,

      Location
{
}
