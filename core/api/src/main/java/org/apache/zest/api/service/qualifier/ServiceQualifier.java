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

package org.apache.zest.api.service.qualifier;

import java.util.function.Predicate;
import org.apache.zest.api.service.ServiceReference;

/**
 * This class helps you select a particular service
 * from a list.
 * <p>
 * Provide a Selector which does the actual
 * selection from the list. A common case is to select
 * based on reference of the service, which you can do this way:
 * </p>
 *
 * <pre><code>
 * new ServiceQualifier&lt;MyService&gt;(services, ServiceQualifier.withId("someId"))
 * </code></pre>
 * <p>
 * Many selectors can be combined by using firstOf. Example:
 * </p>
 * <pre><code>
 * new ServiceQualifier&lt;MyService&gt;(services, firstOf(withTags("sometag"), firstActive(), first()))
 * </code></pre>
 * <p>
 * This will pick a service that has the tag "sometag", or if none is found take the first active one. If no
 * service is active, then the first service will be picked.
 * </p>
 */
public abstract class ServiceQualifier
{
    public static <T> T firstService( Predicate<ServiceReference<?>> qualifier,
                                      Iterable<ServiceReference<T>> services
    )
    {
        for( ServiceReference<T> service : services )
        {
            if( qualifier.test( service ) )
            {
                return service.get();
            }
        }
        return null;
    }

    public static Predicate<ServiceReference<?>> withId( final String anId )
    {
        return new Predicate<ServiceReference<?>>()
        {
            @Override
            public boolean test( ServiceReference<?> service )
            {
                return service.identity().toString().equals( anId );
            }
        };
    }

    public static Predicate<ServiceReference<?>> whereMetaInfoIs( final Object metaInfo )
    {
        return new Predicate<ServiceReference<?>>()
        {
            @Override
            public boolean test( ServiceReference<?> service )
            {
                Object metaObject = service.metaInfo( metaInfo.getClass() );
                return metaObject != null && metaInfo.equals( metaObject );
            }
        };
    }

    public static Predicate<ServiceReference<?>> whereActive()
    {
        return new Predicate<ServiceReference<?>>()
        {
            @Override
            public boolean test( ServiceReference<?> service )
            {
                return service.isActive();
            }
        };
    }

    public static Predicate<ServiceReference<?>> whereAvailable()
    {
        return new Predicate<ServiceReference<?>>()
        {
            @Override
            public boolean test( ServiceReference<?> service )
            {
                return service.isAvailable();
            }
        };
    }

    public static Predicate<ServiceReference<?>> withTags( final String... tags )
    {
        return new Predicate<ServiceReference<?>>()
        {
            @Override
            public boolean test( ServiceReference<?> service )
            {
                ServiceTags serviceTags = service.metaInfo( ServiceTags.class );

                return serviceTags != null && serviceTags.hasTags( tags );
            }
        };
    }
}
