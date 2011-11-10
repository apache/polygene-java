/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.eventsourcing.domain.rest.client;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JAVADOC
 */
public interface DomainEventSourceClient
    extends ServiceComposite, Configuration<DomainEventSourceClientConfiguration>, Activatable
{
    class Mixin
        implements Activatable, Runnable
    {
        @This
        Configuration<DomainEventSourceClientConfiguration> configuration;

        public void activate() throws Exception
        {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate( this, 0, configuration.configuration().sleep().get(), TimeUnit.SECONDS );
        }

        public void passivate() throws Exception
        {
        }

        public void run()
        {
        }
    }
}
