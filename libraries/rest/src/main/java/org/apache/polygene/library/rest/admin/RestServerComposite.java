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

package org.apache.polygene.library.rest.admin;

import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;

@Mixins( { RestServerMixin.class } )
@Activators( RestServerComposite.Activator.class )
public interface RestServerComposite
    extends RestServer
{
    
    void startServer()
            throws Exception;
    
    void stopServer()
            throws Exception;
    
    class Activator extends ActivatorAdapter<ServiceReference<RestServerComposite>>
    {

        @Override
        public void afterActivation( ServiceReference<RestServerComposite> activated )
                throws Exception
        {
            activated.get().startServer();
        }

        @Override
        public void beforePassivation( ServiceReference<RestServerComposite> passivating )
                throws Exception
        {
            passivating.get().stopServer();
        }
        
    }
}
