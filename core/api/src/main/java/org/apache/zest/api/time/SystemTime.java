package org.apache.zest.api.time;

import java.time.Clock;
import java.time.Instant;

public class SystemTime
{
    private static Clock defaultClock = Clock.systemUTC();

    public static Clock getDefaultClock()
    {
        return defaultClock;
    }

    public static void setDefaultClock(Clock defaultClock)
    {
        SystemTime.defaultClock = defaultClock;
    }

    public static Instant now()
    {
        return Instant.now(defaultClock);
    }
}
