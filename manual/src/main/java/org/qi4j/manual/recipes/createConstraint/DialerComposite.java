package org.qi4j.manual.recipes.createConstraint;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.service.ServiceComposite;

// START SNIPPET: composite
@Constraints( PhoneNumberConstraint.class )
public interface DialerComposite extends ServiceComposite, Dialer
{
}
// END SNIPPET: composite
