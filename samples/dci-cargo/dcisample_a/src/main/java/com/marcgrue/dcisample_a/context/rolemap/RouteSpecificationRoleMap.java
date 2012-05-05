package com.marcgrue.dcisample_a.context.rolemap;

import com.marcgrue.dcisample_a.context.shipping.booking.BuildDeliverySnapshot;
import com.marcgrue.dcisample_a.data.shipping.cargo.RouteSpecification;
import org.qi4j.api.value.ValueComposite;

/**
 * Route Specification Role Map
 *
 * Note that this is a Value Composite (and not an entity) capable of playing different Roles.
 */
public interface RouteSpecificationRoleMap
      extends ValueComposite,

      RouteSpecification,

      BuildDeliverySnapshot.FactoryRole
{
}
