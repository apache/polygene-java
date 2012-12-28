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

package org.qi4j.api.service.qualifier;

import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Specification;

/**
 * This class helps you select a particular service
 * from a list. Provide a Selector which does the actual
 * selection from the list. A common case is to select
 * based on identity of the service, which you can do this way:
 *
 * <pre><code>
 * new ServiceQualifier<MyService>(services, ServiceQualifier.withId("someId"))
 * </code></pre>
 *
 * Many selectors can be combined by using firstOf. Example:
 * <pre><code>
 * new ServiceQualifier<MyService>(services, firstOf(withTags("sometag"), firstActive(), first()))
 * </code></pre>
 * This will pick a service that has the tag "sometag", or if none is found take the first active one. If no
 * service is active, then the first service will be picked.
 */
public abstract class ServiceQualifier
{
    public static <T> T firstService( Specification<ServiceReference<?>> qualifier,
                                      Iterable<ServiceReference<T>> services
    )
    {
        for( ServiceReference<T> service : services )
        {
            if( qualifier.satisfiedBy( service ) )
            {
                return service.get();
            }
        }
        return null;
    }

    public static Specification<ServiceReference<?>> withId( final String anId )
    {
        return new Specification<ServiceReference<?>>()
        {
            @Override
            public boolean satisfiedBy( ServiceReference<?> service )
            {
                return service.identity().equals( anId );
            }
        };
    }

    public static Specification<ServiceReference<?>> whereMetaInfoIs( final Object metaInfo )
    {
        return new Specification<ServiceReference<?>>()
        {
            @Override
            public boolean satisfiedBy( ServiceReference<?> service )
            {
                Object metaObject = service.metaInfo( metaInfo.getClass() );
                return metaObject != null && metaInfo.equals( metaObject );
            }
        };
    }

    public static Specification<ServiceReference<?>> whereActive()
    {
        return new Specification<ServiceReference<?>>()
        {
            @Override
            public boolean satisfiedBy( ServiceReference<?> service )
            {
                return service.isActive();
            }
        };
    }

    public static Specification<ServiceReference<?>> whereAvailable()
    {
        return new Specification<ServiceReference<?>>()
        {
            @Override
            public boolean satisfiedBy( ServiceReference<?> service )
            {
                return service.isAvailable();
            }
        };
    }

    public static Specification<ServiceReference<?>> withTags( final String... tags )
    {
        return new Specification<ServiceReference<?>>()
        {
            @Override
            public boolean satisfiedBy( ServiceReference<?> service )
            {
                ServiceTags serviceTags = service.metaInfo( ServiceTags.class );

                return serviceTags != null && serviceTags.hasTags( tags );
            }
        };
    }
}
