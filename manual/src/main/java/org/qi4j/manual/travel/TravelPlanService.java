// START SNIPPET: serviceComposite
// The package is relevant to the Initial Values discussed later.
package org.qi4j.manual.travel;
// END SNIPPET: serviceComposite

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

// START SNIPPET: serviceComposite
@Mixins( { TravelPlanMixin.class } )
public interface TravelPlanService extends TravelPlan, ServiceComposite
{}
// END SNIPPET: serviceComposite