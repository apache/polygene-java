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

package org.qi4j.api.service;

/**
 * This class helps you select a particular service
 * from a list. Provide a Selector which does the actual
 * selection from the list. A common case is to select
 * based on identity of the service, which you can do this way:
 * new ServiceSelector<MyService>(services, ServiceSelector.withId("someId"))
 *
 * Many selectors can be combined by using firstOf. Example:
 * new ServiceSelector<MyService>(services, firstOf(withTags("sometag"), firstActive()))
 * This will pick a service that has the tag "sometag", or if none is found take the first active one.
 */
public final class ServiceSelector<T>
{
    public static <T> ServiceSelector<T> select( Iterable<ServiceReference<T>> services, Selector<T> selector )
    {
        return new ServiceSelector<T>( services, selector );
    }

    public static <T> T service( Iterable<ServiceReference<T>> services, Selector<T> selector )
    {
        ServiceSelector<T> serviceSelector = select( services, selector );
        if( serviceSelector != null )
        {
            return serviceSelector.get();
        }
        else
        {
            return null;
        }
    }

    public static Selector<Object> withId( final String anId )
    {
        return new Selector<Object>()
        {
            public ServiceReference<Object> select( Iterable<ServiceReference<Object>> services )
            {
                for( ServiceReference<Object> service : services )
                {
                    if( service.identity().equals( anId ) )
                    {
                        return service;
                    }
                }
                return null;
            }
        };
    }

    public static Selector<Object> firstActive()
    {
        return new Selector<Object>()
        {
            public ServiceReference<Object> select( Iterable<ServiceReference<Object>> services )
            {
                for( ServiceReference<Object> service : services )
                {
                    if( service.isActive() )
                    {
                        return service;
                    }
                }
                return null;
            }
        };
    }

    public static Selector<Object> withTags( final String... tags )
    {
        return new Selector<Object>()
        {
            public ServiceReference<Object> select( Iterable<ServiceReference<Object>> services )
            {
                for( ServiceReference<Object> service : services )
                {
                    ServiceTags serviceTags = service.metaInfo( ServiceTags.class );

                    if( tags != null && serviceTags.hasTags( tags ) )
                    {
                        return service;
                    }
                }
                return null;
            }
        };
    }

    public static Selector<Object> firstOf( final Selector<Object>... selectors )
    {
        return new Selector<Object>()
        {
            public ServiceReference<Object> select( Iterable<ServiceReference<Object>> services )
            {
                for( Selector<Object> selector : selectors )
                {
                    ServiceReference<Object> serviceRef = selector.select( services );
                    if( serviceRef != null )
                    {
                        return serviceRef;
                    }
                }
                return null;
            }
        };
    }

    private final Iterable<ServiceReference<T>> services;
    private final Selector<T> selector;

    public ServiceSelector( Iterable<ServiceReference<T>> services, Selector<T> selector )
    {
        this.services = services;
        this.selector = selector;
    }

    public T get()
    {
        ServiceReference<T> serviceRef = selector.select( services );

        if( serviceRef != null )
        {
            return serviceRef.get();
        }
        else
        {
            return null;
        }
    }

    public interface Selector<T>
    {
        ServiceReference<T> select( Iterable<ServiceReference<T>> services );
    }
}
