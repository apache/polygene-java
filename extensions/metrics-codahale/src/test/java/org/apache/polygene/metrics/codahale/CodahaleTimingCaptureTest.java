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
package org.apache.polygene.metrics.codahale;

import com.codahale.metrics.MetricRegistry;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.metrics.codahale.assembly.CodahaleMetricsAssembler;
import org.apache.polygene.test.metrics.AbstractTimingCaptureTest;
import org.apache.polygene.test.metrics.MetricValuesProvider;

import java.util.Collection;

import static org.apache.polygene.api.common.Visibility.application;

public class CodahaleTimingCaptureTest extends AbstractTimingCaptureTest
{
    @Override
    protected Assemblers.Visible<? extends Assembler> metricsAssembler()
    {
        return new CodahaleMetricsAssembler();
    }

    @Override
    protected MetricValuesProvider metricValuesProvider()
    {
        CodahaleMetricsProvider metricsProvider = serviceFinder.findService( CodahaleMetricsProvider.class ).get();
        MetricRegistry metricRegistry = metricsProvider.metricRegistry();
        return new MetricValuesProvider()
        {
            @Override
            public Collection<String> registeredMetricNames()
            {
                return metricRegistry.getNames();
            }

            @Override
            public long timerCount( String timerName )
            {
                return metricRegistry.timer( application.name() + '.' + timerName ).getCount();
            }
        };
    }
}
