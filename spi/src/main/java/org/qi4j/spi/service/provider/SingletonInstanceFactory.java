/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.service.provider;

import org.qi4j.service.ServiceDescriptor;
import org.qi4j.service.ServiceInstanceFactory;
import org.qi4j.service.ServiceInstanceProviderException;

/**
 * Return a predefined singleton instance that was provided as service-info
 * through the Singleton class.
 */
public class SingletonInstanceFactory
    implements ServiceInstanceFactory
{

    public Object newInstance( ServiceDescriptor serviceDescriptor ) throws ServiceInstanceProviderException
    {
        return serviceDescriptor.metaInfo( Singleton.class ).instance();
    }

    public void releaseInstance( Object instance )
    {
    }
}
