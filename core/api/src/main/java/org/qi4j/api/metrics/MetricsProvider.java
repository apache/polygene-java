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
 * Metrics Provider SPI.
 */
public interface MetricsProvider
{
    /**
     * Creates a new factory instance.
     *
     * @param factoryType The class of the metric type needed.
     * @param <T>         The metric type requested.
     *
     * @return A factory instance
     * @throws MetricsNotSupportedException when the MetricsProvider is not supporting the factory type requested.
     */
    <T extends MetricsFactory> T createFactory( Class<T> factoryType )
        throws MetricsNotSupportedException;
}
