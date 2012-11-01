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

public interface VoldemortConfiguration extends ConfigurationComposite
{
    @Optional
    Property<String> storeName();

//    @Optional
//    Property<Integer> clientZoneId();
//
    @Optional
    Property<Integer> connectionTimeout();

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

    @Optional
    Property<String> requestFormatType();

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
