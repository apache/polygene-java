/*
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.eventsourcing.domain.source.helper;

import java.lang.reflect.Method;
import java.util.Date;
import org.qi4j.api.util.Methods;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

import static org.qi4j.functional.Iterables.map;

/**
 * Helper methods for working with Iterables of DomainEvents and UnitOfWorkDomainEventsValue.
 */
public class Events
{
    public static Iterable<DomainEventValue> events( Iterable<UnitOfWorkDomainEventsValue> transactions )
    {
        return Iterables.flattenIterables( Iterables.map( new Function<UnitOfWorkDomainEventsValue, Iterable<DomainEventValue>>()
        {
            @Override
            public Iterable<DomainEventValue> map( UnitOfWorkDomainEventsValue unitOfWorkDomainEventsValue )
            {
                return unitOfWorkDomainEventsValue.events().get();
            }
        }, transactions ) );
    }

    public static Iterable<DomainEventValue> events( UnitOfWorkDomainEventsValue... unitOfWorkDomainValues )
    {
        return events( Iterables.iterable( unitOfWorkDomainValues ) );
    }

    // Common specifications
    public static Specification<UnitOfWorkDomainEventsValue> afterDate( final Date afterDate )
    {
        return new Specification<UnitOfWorkDomainEventsValue>()
        {
            @Override
            public boolean satisfiedBy( UnitOfWorkDomainEventsValue eventValue )
            {
                return eventValue.timestamp().get() > afterDate.getTime();
            }
        };
    }

    public static Specification<UnitOfWorkDomainEventsValue> beforeDate( final Date afterDate )
    {
        return new Specification<UnitOfWorkDomainEventsValue>()
        {
            @Override
            public boolean satisfiedBy( UnitOfWorkDomainEventsValue eventValue )
            {
                return eventValue.timestamp().get() < afterDate.getTime();
            }
        };
    }

    public static Specification<UnitOfWorkDomainEventsValue> withUsecases( final String... names )
    {
        return new Specification<UnitOfWorkDomainEventsValue>()
        {
            @Override
            public boolean satisfiedBy( UnitOfWorkDomainEventsValue eventValue )
            {
                for (String name : names)
                {
                    if (eventValue.usecase().get().equals( name ))
                        return true;
                }
                return false;
            }
        };
    }

    public static Specification<UnitOfWorkDomainEventsValue> byUser( final String... by )
    {
        return new Specification<UnitOfWorkDomainEventsValue>()
        {
            @Override
            public boolean satisfiedBy( UnitOfWorkDomainEventsValue eventValue )
            {
                for (String user : by)
                {
                    if (eventValue.user().get().equals( user ))
                        return true;
                }
                return false;
            }
        };
    }

    public static Specification<DomainEventValue> withNames( final Iterable<String> names )
    {
        return new Specification<DomainEventValue>()
        {
            @Override
            public boolean satisfiedBy( DomainEventValue eventValue )
            {
                for (String name : names)
                {
                    if (eventValue.name().get().equals( name ))
                        return true;
                }
                return false;
            }
        };
    }

    public static Specification<DomainEventValue> withNames( final String... names )
    {
        return new Specification<DomainEventValue>()
        {
            @Override
            public boolean satisfiedBy( DomainEventValue eventValue )
            {
                for (String name : names)
                {
                    if (eventValue.name().get().equals( name ))
                        return true;
                }
                return false;
            }
        };
    }

    public static Specification<DomainEventValue> withNames( final Class eventClass )
    {
        return Events.withNames( map( new Function<Method, String>()
        {
            @Override
            public String map( Method method )
            {
                return method.getName();
            }
        }, Iterables.toList( Methods.METHODS_OF.map( eventClass ) ) ));
    }

    public static Specification<DomainEventValue> onEntities( final String... entities )
    {
        return new Specification<DomainEventValue>()
        {
            @Override
            public boolean satisfiedBy( DomainEventValue eventValue )
            {
                for (String entity : entities)
                {
                    if (eventValue.entityId().get().equals( entity ))
                        return true;
                }
                return false;
            }
        };
    }

    public static Specification<DomainEventValue> onEntityTypes( final String... entityTypes )
    {
        return new Specification<DomainEventValue>()
        {
            @Override
            public boolean satisfiedBy( DomainEventValue eventValue )
            {
                for (String entityType : entityTypes)
                {
                    if (eventValue.entityType().get().equals( entityType ))
                        return true;
                }
                return false;
            }
        };
    }

    public static Specification<DomainEventValue> paramIs( final String name, final String value )
    {
        return new Specification<DomainEventValue>()
        {
            @Override
            public boolean satisfiedBy( DomainEventValue eventValue )
            {
                return EventParameters.getParameter( eventValue, name ).equals( value );
            }
        };
    }
}
