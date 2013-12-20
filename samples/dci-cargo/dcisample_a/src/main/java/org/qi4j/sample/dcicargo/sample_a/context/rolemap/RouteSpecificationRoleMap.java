/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.context.rolemap;

import org.qi4j.api.value.ValueComposite;
import org.qi4j.sample.dcicargo.sample_a.context.shipping.booking.BuildDeliverySnapshot;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.RouteSpecification;

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
