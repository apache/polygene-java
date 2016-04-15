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

import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.metrics.MetricsProvider;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceReference;

@Mixins( YammerMetricsMixin.class )
@Activators( YammerMetricsProvider.Activator.class )
public interface YammerMetricsProvider
        extends MetricsProvider
{

    void shutdownMetrics()
            throws Exception;

    class Activator
            extends ActivatorAdapter<ServiceReference<YammerMetricsProvider>>
    {

        @Override
        public void beforePassivation( ServiceReference<YammerMetricsProvider> passivating )
                throws Exception
        {
            passivating.get().shutdownMetrics();
        }

    }

}
