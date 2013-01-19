package org.qi4j.manual.travel;

import org.qi4j.api.injection.scope.This;
// START SNIPPET: mixin
import org.qi4j.api.configuration.Configuration;

public class TravelPlanMixin implements TravelPlan
{
    @This
    Configuration<TravelPlanConfiguration> config;

    private void foo()
    {
        TravelPlanConfiguration tpConf = config.get();
        String hostName = tpConf.hostName().get();
        // ...
    }
// END SNIPPET: mixin

    // START SNIPPET: refresh
    public void doSomething()
    {
        // Refresh Configuration before reading it.
        config.refresh();

        TravelPlanConfiguration tpConf = config.get();
        // ...
    }
    // END SNIPPET: refresh

// START SNIPPET: mixin
}
// END SNIPPET: mixin