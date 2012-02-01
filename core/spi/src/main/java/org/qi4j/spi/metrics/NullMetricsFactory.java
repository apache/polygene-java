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

public final class NullMetricsFactory
{
    static class NullCounterFactory implements MetricsCounterFactory
    {
        @Override
        public MetricsCounter createCounter( Class<?> origin, String name )
        {
            return DefaultMetric.NULL;
        }
    }

    static class NullGaugeFactory implements MetricsGaugeFactory
    {
        @Override
        public <T> MetricsGauge<T> registerGauge( Class<?> origin, String name, MetricsGauge<T> gauge )
        {
            return DefaultMetric.NULL;
        }
    }

    static class NullHealthCheckFactory implements MetricsHealthCheckFactory
    {
        @Override
        public MetricsHealthCheck registerHealthCheck( Class<?> origin, String name, MetricsHealthCheck check )
        {
            return DefaultMetric.NULL;
        }
    }

    static class NullHistogramFactory implements MetricsHistogramFactory
    {
        @Override
        public MetricsHistogram createHistogram( Class<?> origin, String name )
        {
            return DefaultMetric.NULL;
        }
    }

    static class NullMeterFactory implements MetricsMeterFactory
    {
        @Override
        public MetricsMeter createMeter( Class<?> origin, String name, String eventType, TimeUnit rate )
        {

            return DefaultMetric.NULL;
        }
    }

    static class NullTimerFactory implements MetricsTimerFactory
    {
        @Override
        public MetricsTimer createTimer( Class<?> origin, String name, TimeUnit duration, TimeUnit rate )
        {
            return DefaultMetric.NULL;
        }
    }
}
