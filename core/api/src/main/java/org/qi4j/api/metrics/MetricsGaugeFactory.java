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

package org.qi4j.api.metrics;

/**
 * Register MetricsGauge with the underlying Metrics system.
 */
public interface MetricsGaugeFactory extends MetricsFactory
{
    /**
     * Register a MetricsGauge with the underlying Metrics system.
     *
     * @param origin The class where the MetricsGauge is created.
     * @param name   A human readable, short name of the metric.
     * @param gauge  The implementation of the MetricsGauge.
     * @param <T>    Any type holding the MetricsGauge's current value.
     *
     * @return The same MetricsGauge or the DefaultMetric.NULL MetricsGauge instance.
     */
    <T> MetricsGauge<T> registerGauge( Class<?> origin, String name, MetricsGauge<T> gauge );
}
