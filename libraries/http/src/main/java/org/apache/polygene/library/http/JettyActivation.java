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

package org.apache.polygene.library.http;

import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.service.ServiceReference;

public interface JettyActivation
{

    void startJetty()
            throws Exception;

    void stopJetty()
            throws Exception;

    public class Activator
            extends ActivatorAdapter<ServiceReference<JettyActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<JettyActivation> activated )
                throws Exception
        {
            activated.get().startJetty();
        }

        @Override
        public void beforePassivation( ServiceReference<JettyActivation> passivating )
                throws Exception
        {
            passivating.get().stopJetty();
        }

    }

}
