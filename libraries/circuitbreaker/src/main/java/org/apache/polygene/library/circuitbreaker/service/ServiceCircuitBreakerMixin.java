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
package org.apache.polygene.library.circuitbreaker.service;

import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Initializable;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.library.circuitbreaker.CircuitBreaker;

/**
 * Helper implementation of ServiceCircuitBreaker. Fetches the CircuitBreaker from meta-info
 * for the service.
 */
public class ServiceCircuitBreakerMixin
        implements ServiceCircuitBreaker, Initializable
{

    @Uses
    ServiceDescriptor descriptor;

    CircuitBreaker circuitBreaker;

    @Override
    public void initialize()
    {
        circuitBreaker = descriptor.metaInfo( CircuitBreaker.class );
    }

    @Override
    public CircuitBreaker circuitBreaker()
    {
        return circuitBreaker;
    }

}
