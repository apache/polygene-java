/*
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.circuitbreaker.service;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Availability;
import org.qi4j.library.circuitbreaker.CircuitBreaker;

/**
 * Abstract composite that determines Availability by
 * checking the Enabled configuration and a CircuitBreaker.
 *
 * To use this, the service must implement ServiceCircuitBreaker, and its ConfigurationComposite
 * must extend Enabled.
 */
@Mixins( AbstractEnabledCircuitBreakerAvailability.Mixin.class )
public interface AbstractEnabledCircuitBreakerAvailability
        extends Availability
{

    class Mixin
            implements Availability
    {

        @This
        Configuration<Enabled> config;

        @This
        ServiceCircuitBreaker circuitBreaker;

        @Override
        public boolean isAvailable()
        {
            return config.get().enabled().get() && circuitBreaker.circuitBreaker().status() == CircuitBreaker.Status.on;
        }

    }

}
