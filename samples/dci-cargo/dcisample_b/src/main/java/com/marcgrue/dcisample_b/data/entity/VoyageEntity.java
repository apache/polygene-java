package com.marcgrue.dcisample_b.data.entity;

import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
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
