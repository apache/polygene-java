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

package org.apache.zest.api.metrics;

/**
 * Metrics Provider SPI.
 * <p>
 * The Zest Runtime will automatically look for a service that implements the MetricsProvider interface
 * and use it for internal Runtime metrics, such as the UnitOfWork measuring the time from creation to close.
 * </p>
 * <p>
 * The Metrics Library is available to add metric functionality to applications in the same way, and
 * will use the same MetricsProvider.
 * </p>
 * <p>
 * Note that the usual visibility rules applies, so you might have more than one MetricsProvider server,
 * perhaps per layer.
 * </p>
 */
public interface MetricsProvider
{
    /**
     * Creates a new factory instance.
     *
     * The instantiation is done by providing a Metric type, which is one of
     * <ul>
     * <li>{@link MetricsCounter}</li>
     * <li>{@link MetricsGauge}</li>
     * <li>{@link MetricsHealthCheck}</li>
     * <li>{@link MetricsHistogram}</li>
     * <li>{@link MetricsMeter}</li>
     * <li>{@link MetricsTimer}</li>
     * </ul>
     *
     * @param factoryType The class of the metric type needed.
     * @param <T>         The metric type requested.
     *
     * @return A factory instance
     *
     * @throws MetricsNotSupportedException when the MetricsProvider is not supporting the factory type requested.
     */
    <T extends MetricsFactory> T createFactory( Class<T> factoryType )
        throws MetricsNotSupportedException;
}
