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
package org.apache.zest.library.metrics;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.Assemblers;
import org.apache.zest.metrics.yammer.YammerMetricsAssembler;
import org.apache.zest.test.metrics.MetricValuesProvider;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

public class YammerTimingCaptureTest extends AbstractTimingCaptureTest
{
    @Override
    protected Assemblers.Visible<? extends Assembler> metricsAssembler()
    {
        return new YammerMetricsAssembler();
    }

    @Override
    protected MetricValuesProvider metricValuesProvider()
    {
        MetricsRegistry metricsRegistry = Metrics.defaultRegistry();
        return new MetricValuesProvider()
        {
            @Override
            public Collection<String> registeredMetricNames()
            {
                return metricsRegistry.allMetrics().keySet().stream().map( MetricName::getName ).collect( toList() );
            }

            @Override
            public long timerCount( String timerName )
            {
                MetricName metricName = new MetricName( "", "", timerName );
                return metricsRegistry.newTimer( metricName, MILLISECONDS, MILLISECONDS ).count();
            }
        };
    }

    @Override
    public void tearDown()
            throws Exception
    {
        Field metrics = MetricsRegistry.class.getDeclaredField( "metrics" );
        metrics.setAccessible( true );
        Map m = (Map) metrics.get( Metrics.defaultRegistry() );
        m.clear();
        super.tearDown();
    }
}
