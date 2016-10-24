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

package org.apache.zest.metrics.yammer;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.zest.api.injection.scope.Structure;
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
import org.apache.zest.api.structure.Application;
import org.apache.zest.spi.metrics.MetricsProviderAdapter;

public class YammerMetricsMixin extends MetricsProviderAdapter
    implements YammerMetricsProvider
{
    @Structure
    private Application app;

    @Override
    protected MetricsTimerFactory createMetricsTimerFactory()
    {
        return new MetricsTimerFactory()
        {
            @Override
            public MetricsTimer createTimer( Class<?> origin, String name )
            {
                return new YammerTimer( Metrics.newTimer( origin, name, app.name(), TimeUnit.MILLISECONDS, TimeUnit.SECONDS ) );
            }

            @Override
            public Stream<Metric> registered()
            {
                return Stream.empty();
            }
        };
    }

    @Override
    protected MetricsMeterFactory createMetricsMeterFactory()
    {
        return new MetricsMeterFactory()
        {
            @Override
            public MetricsMeter createMeter( Class<?> origin, String name )
            {
                return new YammerMeter( Metrics.newMeter( origin, name, app.name(), TimeUnit.MILLISECONDS ) );
            }

            @Override
            public Stream<Metric> registered()
            {
                return Stream.empty();
            }
        };
    }

    @Override
    protected MetricsHistogramFactory createMetricsHistogramFactory()
    {
        return new MetricsHistogramFactory()
        {
            @Override
            public MetricsHistogram createHistogram( Class<?> origin, String name )
            {
                return new YammerHistogram( Metrics.newHistogram( origin, name, app.name() ) );
            }

            @Override
            public Stream<Metric> registered()
            {
                return Stream.empty();
            }
        };
    }

    @Override
    protected MetricsHealthCheckFactory createMetricsHealthCheckFactory()
    {
        return new MetricsHealthCheckFactory()
        {
            @Override
            public MetricsHealthCheck registerHealthCheck( Class<?> origin, String name, MetricsHealthCheck check )
            {
                return new YammerHealthCheck( origin, name, check );
            }

            @Override
            public Stream<Metric> registered()
            {
                return Stream.empty();
            }
        };
    }

    @Override
    protected MetricsGaugeFactory createMetricsGaugeFactory()
    {
        return new MetricsGaugeFactory()
        {
            @Override
            public <T> MetricsGauge<T> registerGauge( Class<?> origin, String name, final MetricsGauge<T> gauge )
            {
                Gauge<T> yammer = Metrics.newGauge( origin, name, app.name(), new Gauge<T>()
                {

                    @Override
                    public T value()
                    {
                        return gauge.value();
                    }
                } );
                return new YammerGauge<>( yammer );
            }

            @Override
            public Stream<Metric> registered()
            {
                return Stream.empty();
            }
        };
    }

    @Override
    protected MetricsCounterFactory createMetricsCounterFactory()
    {
        return new MetricsCounterFactory()
        {
            @Override
            public MetricsCounter createCounter( Class<?> origin, String name )
            {
                Counter counter = Metrics.newCounter( origin, name, app.name() );
                return new YammerCounter( counter );
            }

            @Override
            public Stream<Metric> registered()
            {
                return Stream.empty();
            }
        };
    }

    @Override
    public void shutdownMetrics()
        throws Exception
    {
        Metrics.shutdown();
    }

}
