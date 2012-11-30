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
 * Create MetricsHealthCheck instances.
 */
public interface MetricsHealthCheckFactory extends MetricsFactory
{
    /**
     * Create a MetricsHealthCheck instance.
     * If the same arguments are given twice, the same instance must be returned.
     *
     * @param origin The class that instantiate the metric
     * @param name   A human readable, short name of the metric.
     * @param check  The health check to be performed regularly.
     *
     * @return A MetricsHealthCheck instance to be used, OR org.qi4j.spi.metrics.DefaultMetric.NULL if not supported.
     *
     * @see DefaultMetric
     */
    MetricsHealthCheck registerHealthCheck( Class<?> origin, String name, MetricsHealthCheck check );
}
