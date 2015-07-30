/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.library.eventsourcing.domain.source.helper;

import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.apache.zest.functional.Iterables.count;
import static org.apache.zest.io.Inputs.iterable;
import static org.apache.zest.io.Outputs.systemOut;
import static org.apache.zest.library.eventsourcing.domain.source.helper.Events.events;

/**
 * TODO
 */
public class EventsTest
{
    private List<UnitOfWorkDomainEventsValue> list;

    @Before
    public void testData()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.values( UnitOfWorkDomainEventsValue.class, DomainEventValue.class );
            }
        };

        list = new ArrayList<UnitOfWorkDomainEventsValue>(  );
        {
            ValueBuilder<UnitOfWorkDomainEventsValue> builder = assembler.module().newValueBuilder( UnitOfWorkDomainEventsValue.class );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test1" ) );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test2" ) );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test3" ) );
            builder.prototype().version().set( "1.0" );
            builder.prototype().timestamp().set( System.currentTimeMillis() );
            builder.prototype().usecase().set( "Test" );
            list.add( builder.newInstance() );
        }
        {
            ValueBuilder<UnitOfWorkDomainEventsValue> builder = assembler.module().newValueBuilder( UnitOfWorkDomainEventsValue.class );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test4" ) );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test5" ) );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test6" ) );
            builder.prototype().version().set( "1.0" );
            builder.prototype().timestamp().set( System.currentTimeMillis() );
            builder.prototype().usecase().set( "Test2" );
            list.add( builder.newInstance() );
        }
    }

    private DomainEventValue newDomainEvent( SingletonAssembler assembler, String name )
    {
        ValueBuilder<DomainEventValue> eventBuilder = assembler.module().newValueBuilder( DomainEventValue.class );
        eventBuilder.prototype().entityId().set( "123" );
        eventBuilder.prototype().entityType().set( "Foo" );
        eventBuilder.prototype().parameters().set( "{}" );
        eventBuilder.prototype().name().set( name );
        return eventBuilder.newInstance();
    }

    @Test
    public void testIterablesEvents()
    {
        assertThat( count( events( list ) ), equalTo( 6L ) );

        iterable( events( list ) ).transferTo( systemOut() );
    }
}
