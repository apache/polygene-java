/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import org.qi4j.service.ServiceComposite;
import org.qi4j.spi.service.ServiceProvider;

/**
 * TODO
 */
public class ServiceRef<T>
{
    T service;
    ServiceProvider provider;

    public ServiceRef( T service, ServiceProvider provider )
    {
        this.service = service;
        this.provider = provider;
    }

    public T getService()
    {
        return service;
    }

    public ServiceProvider getProvider()
    {
        return provider;
    }

    public void release()
    {
        provider.releaseService( (ServiceComposite) service );
        service = null;
        provider = null;
    }
}
