/*
 * Copyright 2009-2010 Streamsource AB
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

import org.qi4j.api.specification.Specification;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.replay.DomainEventPlayer;
import org.qi4j.library.eventsourcing.domain.source.EventVisitor;
import org.qi4j.library.eventsourcing.domain.source.UnitOfWorkEventsVisitor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.map;

/**
 * Helper methods for working with Iterables of DomainEvents and UnitOfWorkDomainEventsValue.
 */
public class Events
{
    public static Iterable<DomainEventValue> events( Iterable<UnitOfWorkDomainEventsValue> transactions )
    {
        List<Iterable<DomainEventValue>> events = new ArrayList<Iterable<DomainEventValue>>();
        for (UnitOfWorkDomainEventsValue unitOfWorkDomainValue : transactions)
        {
            events.add( unitOfWorkDomainValue.events().get() );
        }

        Iterable<DomainEventValue>[] iterables = (Iterable<DomainEventValue>[]) new Iterable[events.size()];
        return Iterables.<DomainEventValue>flatten( events.<Iterable<DomainEventValue>>toArray( iterables ) );
    }

    public static Iterable<DomainEventValue> events( UnitOfWorkDomainEventsValue... unitOfWorkDomainValues )
    {
        List<Iterable<DomainEventValue>> events = new ArrayList<Iterable<DomainEventValue>>();
        for (UnitOfWorkDomainEventsValue unitOfWorkDomainValue : unitOfWorkDomainValues)
        {
            events.add( unitOfWorkDomainValue.events().get() );
        }

        Iterable<DomainEventValue>[] iterables = (Iterable<DomainEventValue>[]) new Iterable[events.size()];
        return Iterables.<DomainEventValue>flatten( events.<Iterable<DomainEventValue>>toArray( iterables ) );
    }

    public static UnitOfWorkEventsVisitor adapter( final EventVisitor eventVisitor )
    {
        return new UnitOfWorkEventsVisitor()
        {
            public boolean visit( UnitOfWorkDomainEventsValue unitOfWorkDomainValue )
            {
                for (DomainEventValue domainEventValue : unitOfWorkDomainValue.events().get())
                {
                    if (!eventVisitor.visit( domainEventValue ))
                        return false;
                }
                return true;
            }
        };
    }

    public static boolean matches( Specification<DomainEventValue> specification, Iterable<UnitOfWorkDomainEventsValue> transactions )
    {
        return filter( specification, events( transactions ) ).iterator().hasNext();
    }

    // Common specifications
    public static Specification<UnitOfWorkDomainEventsValue> afterDate( final Date afterDate )
    {
        return new Specification<UnitOfWorkDomainEventsValue>()
        {
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
            public String map( Method method )
            {
                return method.getName();
            }
        }, Classes.methodsOf( eventClass ) ) );
    }

    public static Specification<DomainEventValue> onEntities( final String... entities )
    {
        return new Specification<DomainEventValue>()
        {
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
            public boolean satisfiedBy( DomainEventValue eventValue )
            {
                return EventParameters.getParameter( eventValue, name ).equals( value );
            }
        };
    }

    public static EventVisitor playEvents( final DomainEventPlayer player, final Object eventHandler, final UnitOfWorkFactory uowf, final Usecase usecase )
    {
        return new EventVisitor()
        {
            public boolean visit( DomainEventValue eventValue )
            {
                UnitOfWork uow = uowf.newUnitOfWork( usecase );
                try
                {
                    player.playEvent( eventValue, eventHandler );
                    uow.complete();
                    return true;
                } catch (Exception e)
                {
                    uow.discard();

                    return false;
                }
            }
        };
    }
}
