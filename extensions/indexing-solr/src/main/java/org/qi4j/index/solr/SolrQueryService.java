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
package org.qi4j.index.solr;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.index.solr.internal.SolrEntityIndexerMixin;
import org.qi4j.index.solr.internal.SolrEntityQueryMixin;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.query.EntityFinder;

/**
 * JAVADOC
 */
@Mixins( { SolrEntityIndexerMixin.class, SolrEntityQueryMixin.class } )
@Activators( SolrQueryService.Activator.class )
public interface SolrQueryService
        extends EntityFinder, StateChangeListener, SolrSearch, ServiceComposite
{

    void inflateSolrSchema();

    void releaseSolrSchema();

    class Activator
            extends ActivatorAdapter<ServiceReference<SolrQueryService>>
    {

        @Override
        public void afterActivation( ServiceReference<SolrQueryService> activated )
                throws Exception
        {
            activated.get().inflateSolrSchema();
        }

        @Override
        public void beforePassivation( ServiceReference<SolrQueryService> passivating )
                throws Exception
        {
            passivating.get().releaseSolrSchema();
        }

    }

}
