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
package org.apache.zest.library.eventsourcing.domain.source.helper;

import java.time.Instant;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.io.Inputs;
import org.apache.zest.io.Receiver;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class UnitOfWorkRouterTest
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
                module.services( OrgJsonValueSerializationService.class );
                module.values( UnitOfWorkDomainEventsValue.class, DomainEventValue.class );
                new DefaultUnitOfWorkAssembler().assemble( module );
            }
        };

        list = new ArrayList<UnitOfWorkDomainEventsValue>(  );
        {
            ValueBuilder<UnitOfWorkDomainEventsValue> builder = assembler.module().newValueBuilder( UnitOfWorkDomainEventsValue.class );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test1" ));
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test2" ));
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test3" ));
            builder.prototype().version().set( "1.0" );
            builder.prototype().timestamp().set( Instant.now() );
            builder.prototype().usecase().set( "Test" );
            list.add( builder.newInstance() );
        }
        {
            ValueBuilder<UnitOfWorkDomainEventsValue> builder = assembler.module().newValueBuilder( UnitOfWorkDomainEventsValue.class );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test4" ));
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test5" ));
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test6" ));
            builder.prototype().version().set( "1.0" );
            builder.prototype().timestamp().set( Instant.now() );
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
    public void testRouter() throws IOException
    {
        final List<String> matched = new ArrayList<String>(  );
        UnitOfWorkRouter<IOException> router = new UnitOfWorkRouter<IOException>();
        router.route( Events.withUsecases( "Test" ), new Receiver<UnitOfWorkDomainEventsValue,IOException>()
        {
            @Override
            public void receive( UnitOfWorkDomainEventsValue item ) throws IOException
            {
                matched.add(item.usecase().get());
            }
        });

        EventRouter<IOException> eventRouter = new EventRouter<IOException>();
        eventRouter.defaultReceiver(new Receiver<DomainEventValue, IOException>()
        {
            @Override
            public void receive( DomainEventValue item ) throws IOException
            {
                System.out.println(item);
            }
        });

        router.defaultReceiver(eventRouter);

        Inputs.iterable( list ).transferTo( router );

        Assert.assertThat( matched.toString(), CoreMatchers.equalTo( "[Test]" ) );
    }

    @Test(expected = IOException.class)
    public void testRouterException() throws IOException
    {
        final List<String> matched = new ArrayList<String>(  );
        UnitOfWorkRouter<IOException> router = new UnitOfWorkRouter<IOException>();
        router.route( Events.withUsecases( "Test2" ), new Receiver<UnitOfWorkDomainEventsValue,IOException>()
        {
            @Override
            public void receive( UnitOfWorkDomainEventsValue item ) throws IOException
            {
                throw new IOException("Failed");
            }
        });

        EventRouter<IOException> eventRouter = new EventRouter<IOException>();
        eventRouter.defaultReceiver(new Receiver<DomainEventValue, IOException>()
        {
            @Override
            public void receive( DomainEventValue item ) throws IOException
            {
                System.out.println(item);
            }
        });

        router.defaultReceiver(eventRouter);

        Inputs.iterable( list ).transferTo( router );

        Assert.assertThat( matched.toString(), CoreMatchers.equalTo( "[Test]" ) );
    }
}
