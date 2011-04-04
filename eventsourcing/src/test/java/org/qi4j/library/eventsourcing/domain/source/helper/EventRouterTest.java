package org.qi4j.library.eventsourcing.domain.source.helper;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class EventRouterTest
{
    private List<UnitOfWorkDomainEventsValue> list;

    @Before
    public void testData()
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
            ValueBuilder<UnitOfWorkDomainEventsValue> builder = assembler.valueBuilderFactory().newValueBuilder( UnitOfWorkDomainEventsValue.class );
            builder.prototype().events().get().add( assembler.valueBuilderFactory().newValueFromJSON( DomainEventValue.class,"{name:'Test1'}" ) );
            builder.prototype().events().get().add( assembler.valueBuilderFactory().newValueFromJSON( DomainEventValue.class,"{name:'Test2'}" ) );
            builder.prototype().events().get().add( assembler.valueBuilderFactory().newValueFromJSON( DomainEventValue.class,"{name:'Test3'}" ) );
            builder.prototype().version().set( "1.0" );
            builder.prototype().timestamp().set( System.currentTimeMillis() );
            builder.prototype().usecase().set( "Test" );
            list.add( builder.newInstance() );
        }
        {
            ValueBuilder<UnitOfWorkDomainEventsValue> builder = assembler.valueBuilderFactory().newValueBuilder( UnitOfWorkDomainEventsValue.class );
            builder.prototype().events().get().add( assembler.valueBuilderFactory().newValueFromJSON( DomainEventValue.class,"{name:'Test4'}" ) );
            builder.prototype().events().get().add( assembler.valueBuilderFactory().newValueFromJSON( DomainEventValue.class,"{name:'Test5'}" ) );
            builder.prototype().events().get().add( assembler.valueBuilderFactory().newValueFromJSON( DomainEventValue.class,"{name:'Test6'}" ) );
            builder.prototype().version().set( "1.0" );
            builder.prototype().timestamp().set( System.currentTimeMillis() );
            builder.prototype().usecase().set( "Test2" );
            list.add( builder.newInstance() );
        }
    }

    @Test
    public void testRouter() throws IOException
    {
        final List<DomainEventValue> matched = new ArrayList<DomainEventValue>(  );
        EventRouter<IOException> router = new EventRouter<IOException>();
        router.route( Events.withNames( "Test1", "Test2" ), new Receiver<DomainEventValue,IOException>()
        {
            @Override
            public void receive( DomainEventValue item ) throws IOException
            {
                matched.add(item);
            }
        });

        Inputs.iterable( Events.events( list) ).transferTo( router );

        Assert.assertThat(matched.toString(), CoreMatchers.equalTo( "[{\"entityId\":\"\",\"entityType\":\"\",\"name\":\"Test1\",\"parameters\":\"\"}, {\"entityId\":\"\",\"entityType\":\"\",\"name\":\"Test2\",\"parameters\":\"\"}]" ));
    }
}
