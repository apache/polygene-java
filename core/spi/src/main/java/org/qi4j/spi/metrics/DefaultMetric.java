/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.spi.metrics;

import org.qi4j.api.metrics.MetricsCounter;
import org.qi4j.api.metrics.MetricsGauge;
import org.qi4j.api.metrics.MetricsHealthCheck;
import org.qi4j.api.metrics.MetricsHistogram;
import org.qi4j.api.metrics.MetricsMeter;
import org.qi4j.api.metrics.MetricsTimer;

/**
 * Default Metric implementing all supported Metrics as a null object.
 */
public final class DefaultMetric
    implements MetricsGauge, MetricsCounter, MetricsHistogram, MetricsHealthCheck, MetricsMeter, MetricsTimer
{
    public static final DefaultMetric NULL = new DefaultMetric();

    @Override
    public void increment()
    {
    }

    @Override
    public void increment( int steps )
    {
    }

    @Override
    public void decrement()
    {
    }

    @Override
    public void decrement( int steps )
    {
    }

    @Override
    public Context start()
    {
        return new Context()
        {
            @Override
            public void stop()
            {
            }
        };
    }

    @Override
    public Object value()
    {
        return null;
    }

    @Override
    public void update( long newValue )
    {
    }

    @Override
    public Result check()
        throws Exception
    {
        return new Result( true, "No checks", null );
    }

    @Override
    public void mark()
    {
    }

    @Override
    public void mark( int numberOfEvents )
    {
    }
}
