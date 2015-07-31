/*  Copyright 2008 Rickard Öberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.jdbm;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.service.ServiceReference;

/**
 * Activation for JdbmEntityStoreMixin.
 */
@Activators( { JdbmEntityStoreActivation.Activator.class } )
public interface JdbmEntityStoreActivation
{

    void setUpJdbm()
            throws Exception;

    void tearDownJdbm()
            throws Exception;

    public class Activator
            extends ActivatorAdapter<ServiceReference<JdbmEntityStoreActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<JdbmEntityStoreActivation> activated )
                throws Exception
        {
            activated.get().setUpJdbm();
        }

        @Override
        public void beforePassivation( ServiceReference<JdbmEntityStoreActivation> passivating )
                throws Exception
        {
            passivating.get().tearDownJdbm();
        }

    }

}
