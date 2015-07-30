/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.spi.entitystore.helpers;

import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.service.ServiceReference;

/**
 * Activation for JSONMapEntityStoreMixin.
 */
@Activators( JSONMapEntityStoreActivation.Activator.class )
public interface JSONMapEntityStoreActivation
{

    void setUpJSONMapES()
        throws Exception;

    void tearDownJSONMapES()
        throws Exception;

    /**
     * JSONMapEntityStoreMixin Activator.
     */
    public class Activator
        extends ActivatorAdapter<ServiceReference<JSONMapEntityStoreActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<JSONMapEntityStoreActivation> activated )
            throws Exception
        {
            activated.get().setUpJSONMapES();
        }

        @Override
        public void beforePassivation( ServiceReference<JSONMapEntityStoreActivation> passivating )
            throws Exception
        {
            passivating.get().tearDownJSONMapES();
        }

    }

}
