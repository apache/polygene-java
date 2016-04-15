/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.spi.metrics;

import java.util.concurrent.TimeUnit;
import org.apache.zest.api.metrics.Metric;
import org.apache.zest.api.metrics.MetricsCounter;
import org.apache.zest.api.metrics.MetricsCounterFactory;
import org.apache.zest.api.metrics.MetricsGauge;
import org.apache.zest.api.metrics.MetricsGaugeFactory;
import org.apache.zest.api.metrics.MetricsHealthCheck;
import org.apache.zest.api.metrics.MetricsHealthCheckFactory;
import org.apache.zest.api.metrics.MetricsHistogram;
import org.apache.zest.api.metrics.MetricsHistogramFactory;
import org.apache.zest.api.metrics.MetricsMeter;
import org.apache.zest.api.metrics.MetricsMeterFactory;
import org.apache.zest.api.metrics.MetricsTimer;
import org.apache.zest.api.metrics.MetricsTimerFactory;
import org.apache.zest.functional.Iterables;

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
