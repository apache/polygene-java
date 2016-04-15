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

import org.apache.zest.api.metrics.MetricsCounterFactory;
import org.apache.zest.api.metrics.MetricsFactory;
import org.apache.zest.api.metrics.MetricsGaugeFactory;
import org.apache.zest.api.metrics.MetricsHealthCheckFactory;
import org.apache.zest.api.metrics.MetricsHistogramFactory;
import org.apache.zest.api.metrics.MetricsMeterFactory;
import org.apache.zest.api.metrics.MetricsNotSupportedException;
import org.apache.zest.api.metrics.MetricsProvider;
import org.apache.zest.api.metrics.MetricsTimerFactory;

/**
 * Adapter to ease MetricsProvider implementation.
 */
public class MetricsProviderAdapter
    implements MetricsProvider
{
    private static final MetricsCounterFactory NULL_COUNTER_FACTORY = new NullMetricsFactory.NullCounterFactory();
    private static final MetricsGaugeFactory NULL_GAUGE_FACTORY = new NullMetricsFactory.NullGaugeFactory();
    private static final MetricsMeterFactory NULL_METER_FACTORY = new NullMetricsFactory.NullMeterFactory();
    private static final MetricsHistogramFactory NULL_HISTOGRAM_FACTORY = new NullMetricsFactory.NullHistogramFactory();
    private static final MetricsTimerFactory NULL_TIMER_FACTORY = new NullMetricsFactory.NullTimerFactory();
    private static final MetricsHealthCheckFactory NULL_HEALTHCHECK_FACTORY = new NullMetricsFactory.NullHealthCheckFactory();

    @SuppressWarnings( "unchecked" )
    @Override
    public <T extends MetricsFactory> T createFactory( Class<T> factoryType )
    {
        if( factoryType.equals( MetricsCounterFactory.class ) )
        {
            return (T) createMetricsCounterFactory();
        }
        else if( factoryType.equals( MetricsGaugeFactory.class ) )
        {
            return (T) createMetricsGaugeFactory();
        }
        else if( factoryType.equals( MetricsHealthCheckFactory.class ) )
        {
            return (T) createMetricsHealthCheckFactory();
        }
        else if( factoryType.equals( MetricsHistogramFactory.class ) )
        {
            return (T) createMetricsHistogramFactory();
        }
        else if( factoryType.equals( MetricsMeterFactory.class ) )
        {
            return (T) createMetricsMeterFactory();
        }
        else if( factoryType.equals( MetricsTimerFactory.class ) )
        {
            return (T) createMetricsTimerFactory();
        }
        throw new MetricsNotSupportedException( factoryType, getClass() );
    }

    protected MetricsTimerFactory createMetricsTimerFactory()
    {
        return NULL_TIMER_FACTORY;
    }

    protected MetricsMeterFactory createMetricsMeterFactory()
    {
        return NULL_METER_FACTORY;
    }

    protected MetricsHistogramFactory createMetricsHistogramFactory()
    {
        return NULL_HISTOGRAM_FACTORY;
    }

    protected MetricsHealthCheckFactory createMetricsHealthCheckFactory()
    {
        return NULL_HEALTHCHECK_FACTORY;
    }

    protected MetricsGaugeFactory createMetricsGaugeFactory()
    {
        return NULL_GAUGE_FACTORY;
    }

    protected MetricsCounterFactory createMetricsCounterFactory()
    {
        return NULL_COUNTER_FACTORY;
    }
}
