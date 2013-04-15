package org.qi4j.manual.travel;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.Matches;
import org.qi4j.library.constraints.annotation.Range;

// START SNIPPET: configuration
public interface TravelPlanConfiguration
{
    Property<String> hostName();

    @Range( min=0, max=65535 )
    Property<Integer> portNumber();

    @Matches( "(ssh|rlogin|telnet)" )
    Property<String> protocol();
}
// END SNIPPET: configuration