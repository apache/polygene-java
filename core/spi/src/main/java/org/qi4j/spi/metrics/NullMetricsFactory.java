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

import java.util.concurrent.TimeUnit;
import org.qi4j.api.metrics.Metric;
import org.qi4j.api.metrics.MetricsCounter;
import org.qi4j.api.metrics.MetricsCounterFactory;
import org.qi4j.api.metrics.MetricsGauge;
import org.qi4j.api.metrics.MetricsGaugeFactory;
import org.qi4j.api.metrics.MetricsHealthCheck;
import org.qi4j.api.metrics.MetricsHealthCheckFactory;
import org.qi4j.api.metrics.MetricsHistogram;
import org.qi4j.api.metrics.MetricsHistogramFactory;
import org.qi4j.api.metrics.MetricsMeter;
import org.qi4j.api.metrics.MetricsMeterFactory;
import org.qi4j.api.metrics.MetricsTimer;
import org.qi4j.api.metrics.MetricsTimerFactory;
import org.qi4j.functional.Iterables;

/**
 * Factory for Metrics null objects.
 */
public final class NullMetricsFactory
{
    public static class NullCounterFactory implements MetricsCounterFactory
    {
        @Override
        public MetricsCounter createCounter( Class<?> origin, String name )
        {
            return DefaultMetric.NULL;
        }

        @Override
        public Iterable<Metric> registered()
        {
            return Iterables.iterable( (Metric) DefaultMetric.NULL );
        }
    }

    public static class NullGaugeFactory implements MetricsGaugeFactory
    {
        @Override
        @SuppressWarnings( "unchecked" )
        public <T> MetricsGauge<T> registerGauge( Class<?> origin, String name, MetricsGauge<T> gauge )
        {
            return DefaultMetric.NULL;
        }

        @Override
        public Iterable<Metric> registered()
        {
            return Iterables.iterable( (Metric) DefaultMetric.NULL );
        }
    }

    public static class NullHealthCheckFactory implements MetricsHealthCheckFactory
    {
        @Override
        public MetricsHealthCheck registerHealthCheck( Class<?> origin, String name, MetricsHealthCheck check )
        {
            return DefaultMetric.NULL;
        }

        @Override
        public Iterable<Metric> registered()
        {
            return Iterables.iterable( (Metric) DefaultMetric.NULL );
        }
    }

    public static class NullHistogramFactory implements MetricsHistogramFactory
    {
        @Override
        public MetricsHistogram createHistogram( Class<?> origin, String name )
        {
            return DefaultMetric.NULL;
        }

        @Override
        public Iterable<Metric> registered()
        {
            return Iterables.iterable( (Metric) DefaultMetric.NULL );
        }
    }

    public static class NullMeterFactory implements MetricsMeterFactory
    {
        @Override
        public MetricsMeter createMeter( Class<?> origin, String name, String eventType, TimeUnit rate )
        {

            return DefaultMetric.NULL;
        }

        @Override
        public Iterable<Metric> registered()
        {
            return Iterables.iterable( (Metric) DefaultMetric.NULL );
        }
    }

    public static class NullTimerFactory implements MetricsTimerFactory
    {
        @Override
        public MetricsTimer createTimer( Class<?> origin, String name, TimeUnit duration, TimeUnit rate )
        {
            return DefaultMetric.NULL;
        }

        @Override
        public Iterable<Metric> registered()
        {
            return Iterables.iterable( (Metric) DefaultMetric.NULL );
        }
    }
}
