package org.qi4j.api.mixin.partial;

// START SNIPPET: partial
public abstract class SpeedMixin
        implements SpeedLocation
{
    // state for speed

    public void accelerate( float acceleration )
    {
        // logic
    }
}

// END SNIPPET: partial