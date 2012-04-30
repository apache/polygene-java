package com.marcgrue.dcisample_a.data.entity;

import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import org.qi4j.api.entity.EntityComposite;

/**
 * Voyage entity
 *
 * Voyages have been created outside the shipping application context so we don't have any
 * separate aggregate root to create those from.
 */
public interface VoyageEntity
      extends EntityComposite,

      Voyage
{
}
