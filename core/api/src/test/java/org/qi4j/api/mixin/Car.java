package org.qi4j.api.mixin;

// START SNIPPET: mixins
@Mixins( { StartMixin.class, VehicleMixin.class } )
public interface Car extends Startable, Vehicle
{}
// END SNIPPET: mixins

