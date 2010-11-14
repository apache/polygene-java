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

import java.util.Iterator;

/**
 * This class helps you select a particular service
 * from a list. Provide a Selector which does the actual
 * selection from the list. A common case is to select
 * based on identity of the service, which you can do this way:
 * new ServiceQualifier<MyService>(services, ServiceQualifier.withId("someId"))
 *
 * Many selectors can be combined by using firstOf. Example:
 * new ServiceQualifier<MyService>(services, firstOf(withTags("sometag"), firstActive(), first()))
 * This will pick a service that has the tag "sometag", or if none is found take the first active one. If no
 * service is active, then the first service will be picked.
 */
public abstract class ServiceQualifier
{
    public static <T> ServiceReference<T> first( ServiceQualifier qualifier, Iterable<ServiceReference<T>> services )
    {
        for( ServiceReference<T> service : services )
        {
            if( qualifier.qualifies( service ) )
            {
                return service;
            }
        }
        return null;
    }

    public static <T> T firstService( ServiceQualifier qualifier, Iterable<ServiceReference<T>> services )
    {
        for( ServiceReference<T> service : services )
        {
            if( qualifier.qualifies( service ) )
            {
                return service.get();
            }
        }
        return null;
    }

    public static <T> Iterable<ServiceReference<T>> filter( ServiceQualifier qualifier,
                                                            Iterable<ServiceReference<T>> services)
    {
        return new QualifierFilterIterable<T>( qualifier, services );
    }

    public static ServiceQualifier withId( final String anId )
    {
        return new ServiceQualifier()
        {
            @Override
            public boolean qualifies( ServiceReference<?> service )
            {
                return service.identity().equals( anId );
            }
        };
    }

    public static ServiceQualifier whereMetaInfoIs( final Object metaInfo )
    {
        return new ServiceQualifier()
        {
            @Override
            public boolean qualifies( ServiceReference<?> service )
            {
                Object metaObject = service.metaInfo( metaInfo.getClass() );
                return metaObject != null && metaInfo.equals( metaObject );
            }
        };
    }

    public static ServiceQualifier whereActive()
    {
        return new ServiceQualifier()
        {
            @Override
            public boolean qualifies( ServiceReference<?> service )
            {
                return service.isActive();
            }
        };
    }

    public static ServiceQualifier whereAvailable()
    {
        return new ServiceQualifier()
        {
            @Override
            public boolean qualifies( ServiceReference<?> service )
            {
                return service.isAvailable();
            }
        };
    }

    public static ServiceQualifier withTags( final String... tags )
    {
        return new ServiceQualifier()
        {
            @Override
            public boolean qualifies( ServiceReference<?> service )
            {
                ServiceTags serviceTags = service.metaInfo( ServiceTags.class );

                return serviceTags != null && serviceTags.hasTags( tags );
            }
        };
    }

    public abstract boolean qualifies( ServiceReference<?> service );

    private static class QualifierFilterIterable<T>
        implements Iterable<ServiceReference<T>>
    {
        private final ServiceQualifier qualifier;
        private final Iterable<ServiceReference<T>> services;

        public QualifierFilterIterable( ServiceQualifier qualifier, Iterable<ServiceReference<T>> services )
        {
            this.qualifier = qualifier;
            this.services = services;
        }

        public Iterator<ServiceReference<T>> iterator()
        {
            return new QualifierFilterIterator<T>(qualifier, services.iterator());
        }
    }

    private static class QualifierFilterIterator<T>
        implements Iterator<ServiceReference<T>>
    {
        private Iterator<ServiceReference<T>> iterator;
        private ServiceReference<T> currentValue;
        private boolean finished = false;
        private boolean nextConsumed = true;
        private final ServiceQualifier qualifier;

        public QualifierFilterIterator( ServiceQualifier qualifier, Iterator<ServiceReference<T>> iterator)
        {
            this.qualifier = qualifier;
            this.iterator = iterator;
        }

        public boolean moveToNextValid()
        {
            boolean found = false;
            while( !found && iterator.hasNext() )
            {
                ServiceReference<T> currentValue = iterator.next();
                if( qualifier.qualifies( currentValue ) )
                {
                    found = true;
                    this.currentValue = currentValue;
                    nextConsumed = false;
                }
            }
            if( !found )
            {
                finished = true;
            }
            return found;
        }

        public ServiceReference<T> next()
        {
            if( !nextConsumed )
            {
                nextConsumed = true;
                return currentValue;
            }
            else
            {
                if( !finished )
                {
                    if( moveToNextValid() )
                    {
                        nextConsumed = true;
                        return currentValue;
                    }
                }
            }
            return null;
        }

        public boolean hasNext()
        {
            return !finished &&
                   ( !nextConsumed || moveToNextValid() );
        }

        public void remove()
        {
        }
    }
}
