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
 * MetricsGauge is the most basic Metric type, and is completely flexible and therefor handled slightly differently in
 * the MetricsFactory than all other Gauges. It needs to pass on custom code, so the implementation is typically
 * an anonymous class, inlined at the implementation.
 *
 * @param <T> Any type holding the MetricsGauge's current value.
 */
public interface MetricsGauge<T> extends Metric
{
    /**
     * Returns the metric's current value.
     *
     * @return the metric's current value
     */
    T value();
}
