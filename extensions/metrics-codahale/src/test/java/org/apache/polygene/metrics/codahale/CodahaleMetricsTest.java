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
 */
package org.apache.polygene.metrics.codahale;

import com.codahale.metrics.MetricRegistry;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.metrics.codahale.assembly.CodahaleMetricsAssembler;
import org.apache.polygene.test.metrics.AbstractPolygeneMetricsTest;

import org.apache.polygene.test.metrics.MetricValuesProvider;
import org.junit.Test;

import java.util.Collection;

public class CodahaleMetricsTest extends AbstractPolygeneMetricsTest
{
    @Override
    protected Assemblers.Visible<? extends Assembler> metricsAssembler()
    {
        // START SNIPPET: assembly
        return new CodahaleMetricsAssembler();
        // END SNIPPET: assembly
    }

    @Test
    public void uowTimerCodahale() throws PassivationException, ActivationException
    {
        assertUowTimer( codahaleMetricValuesProvider() );
    }

    @Test
    public void timingCaptureCodahale() throws PassivationException, ActivationException
    {
        assertTimingCapture( codahaleMetricValuesProvider() );
    }

    private MetricValuesProvider codahaleMetricValuesProvider()
    {
        Module module = metricsModule();
        // START SNIPPET: registry
        CodahaleMetricsProvider metricsProvider = module.findService( CodahaleMetricsProvider.class ).get();
        // END SNIPPET: registry
        return new MetricValuesProvider()
        {
            @Override
            public Collection<String> registeredMetricNames()
            {
                // START SNIPPET: registry
                MetricRegistry metricRegistry = metricsProvider.metricRegistry();
                // END SNIPPET: registry
                return metricRegistry.getNames();
            }

            @Override
            public long timerCount( String timerName )
            {
                MetricRegistry metricRegistry = metricsProvider.metricRegistry();
                return metricRegistry.timer( timerName ).getCount();
            }
        };
    }
}
