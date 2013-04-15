package org.qi4j.library.eventsourcing.domain.source.helper;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.io.Inputs;
import org.qi4j.io.Receiver;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

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
                module.values( UnitOfWorkDomainEventsValue.class, DomainEventValue.class );
            }
        };

        list = new ArrayList<UnitOfWorkDomainEventsValue>(  );
        {
            ValueBuilder<UnitOfWorkDomainEventsValue> builder = assembler.module().newValueBuilder( UnitOfWorkDomainEventsValue.class );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test1" ));
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test2" ));
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test3" ));
            builder.prototype().version().set( "1.0" );
            builder.prototype().timestamp().set( System.currentTimeMillis() );
            builder.prototype().usecase().set( "Test" );
            list.add( builder.newInstance() );
        }
        {
            ValueBuilder<UnitOfWorkDomainEventsValue> builder = assembler.module().newValueBuilder( UnitOfWorkDomainEventsValue.class );
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test4" ));
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test5" ));
            builder.prototype().events().get().add( newDomainEvent( assembler, "Test6" ));
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
