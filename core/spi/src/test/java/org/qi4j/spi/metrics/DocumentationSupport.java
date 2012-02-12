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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.qi4j.api.injection.scope.Service;

public class DocumentationSupport
{
    // START SNIPPET: common
    @Service
    private MetricsProvider provider;
    // END SNIPPET: common

    public void forDocumentationOnly()
    {
        // START SNIPPET: gauge
        final BlockingQueue queue = new LinkedBlockingQueue( 20 );
        // START SNIPPET: gauge
        // END SNIPPET: gauge
        MetricsGaugeFactory gaugeFactory = provider.createFactory( MetricsGaugeFactory.class );
        MetricsGauge<Integer> gauge = gaugeFactory.registerGauge( getClass(), "Sample Gauge", new MetricsGauge<Integer>()
        {
            @Override
            public Integer value()
            {
                return queue.size();
            }
        } );
        // END SNIPPET: gauge

        // START SNIPPET: counter
        // END SNIPPET: counter

        // START SNIPPET: histogram
        // END SNIPPET: histogram

        // START SNIPPET: meter
        // END SNIPPET: meter

        // START SNIPPET: timer
        // END SNIPPET: timer

        // START SNIPPET: healthcheck
        // END SNIPPET: healthcheck

    }
}
