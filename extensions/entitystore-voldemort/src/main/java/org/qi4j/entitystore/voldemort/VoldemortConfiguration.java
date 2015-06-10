/*
 * Copyright (c) 2010 Niclas Hedhman <niclas@hedhman.org>.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.entitystore.voldemort;

import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * The configuration entity for the Voldemort EntityStore.
 * <p>
 *     Most of these properties are used to call the corresponding setters in the
 *     {@code voldemort.client.ClientConfig} object passed to Voldemort runtime upon start.
 *     The {@code voldemort.client.ClientConfig} is instantiated by the default constructor.
 *
 * </p>
 */
public interface VoldemortConfiguration extends ConfigurationComposite
{
    @Optional
    Property<String> storeName();

    //    @Optional
//    Property<Integer> clientZoneId();
//
    @Optional
    Property<Integer> connectionTimeout();

    /**
     * The Bootstrap URL for Voldemort runtime to use.
     * <p>
     * The configuration property is optional. Default: "tcp://localhost:8581"
     * </p>
     * <p>
     * This value is used to call voldemort.client.ClientConfig#setBootstrapUrls(java.util.List)
     * </p>
     *
     * @return The property containing the bootstrap URL configuration.
     */
    @Optional
    Property<List<String>> bootstrapUrl();

    @Optional
    Property<Boolean> enableJmx();

//    @Optional
//    Property<Boolean> enablePipelineRoutedStore();

    @Optional
    Property<Long> failureDetectorAsyncRecoveryInterval();

    @Optional
    Property<Long> failureDetectorBannagePeriod();

    @Optional
    Property<List<String>> failureDetectorCatastrophicErrorType();

    @Optional
    Property<String> failureDetectorImplementation();

    @Optional
    Property<Long> failureDetectoreRequestLengthThreshold();

    @Optional
    Property<Integer> failureDetectorThreshold();

    @Optional
    Property<Integer> failureDetectorThresholdCountMinimum();

    @Optional
    Property<Long> failureDetectorThreasholdInterval();

    @Optional
    Property<Integer> maxBootstrapRetries();

    @Optional
    Property<Integer> setMaxConnectionsPerNode();

    @Optional
    Property<Integer> maxQueueRequests();

    @Optional
    Property<Integer> maxThreads();

    @Optional
    Property<Integer> maxTotalConnections();

    /** Voldemort has avolved the format over time, and this property sets which request format to use.
     * See {@code voldemort.client.protocol.RequestFormatType} in Voldemort for further details.
     *
     * @return The configuration property that contains the request type formet used in Voldemort.
     */
    @Optional
    Property<String> requestFormatType();

    /** Sets the routing tier of this Voldemort runtime.
     * <p>
     *     Possible values are 'client' and 'server'. See {@code voldemort.client.RoutingTier} in
     *     Voldemort for details.
     * </p>
     * @return the confirguration property that contains the Routing Tier setting for Voldemort.
     */
    @Optional
    Property<String> routingTier();

    @Optional
    Property<Integer> routingTimeout();

//    @Optional
//    Property<Integer> selectors();

    @Optional
    Property<Integer> socketBufferSize();

    @Optional
    Property<Boolean> socketKeepAlive();

    @Optional
    Property<Integer> socketTimeout();

    @Optional
    Property<Integer> threadIdleTime();
}
