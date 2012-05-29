package org.qi4j.api.mixin.partial;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.mixin.StartMixin;
import org.qi4j.api.mixin.Startable;

// START SNIPPET: partial
@Mixins( { StartMixin.class, SpeedMixin.class, CrashResultMixin.class } )
public interface Car extends Startable, Vehicle
{}

// END SNIPPET: partial
