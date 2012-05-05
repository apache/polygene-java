package com.marcgrue.dcisample_a.context.rolemap;

import com.marcgrue.dcisample_a.context.shipping.booking.BuildDeliverySnapshot;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import org.qi4j.api.value.ValueComposite;

/**
 * Itinerary Role Map
 *
 * Note that this is a Value Composite (and not an entity) capable of playing different Roles.
 */
public interface ItineraryRoleMap
      extends ValueComposite,

      Itinerary,

      BuildDeliverySnapshot.ItineraryRole
{
}
