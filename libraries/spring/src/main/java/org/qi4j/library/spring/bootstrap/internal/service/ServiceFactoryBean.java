/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.library.spring.bootstrap.internal.service;

import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.springframework.beans.factory.FactoryBean;

import static org.qi4j.functional.Iterables.first;
import static org.springframework.util.Assert.notNull;

public final class ServiceFactoryBean
    implements FactoryBean
{
    private ServiceReference serviceReference;

    public ServiceFactoryBean( Application anApplication, String aServiceId )
        throws IllegalArgumentException
    {
        notNull( anApplication, "Argument [anApplication] must not be [null]." );
        notNull( aServiceId, "Argument [aServiceId] must not be [null]." );

        ServiceLocator serviceLocator = new ServiceLocator( aServiceId );
        anApplication.descriptor().accept( serviceLocator );
        serviceReference = serviceLocator.locateService( anApplication );

        if( serviceReference == null )
        {
            throw new IllegalArgumentException( "Qi4j service with id [" + aServiceId + "] is not found." );
        }
    }

    @Override
    public final Object getObject()
        throws Exception
    {
        return serviceReference.get();
    }

    @Override
    public final Class getObjectType()
    {
        return first( serviceReference.types() );
    }

    @Override
    public final boolean isSingleton()
    {
        return false;
    }
}
