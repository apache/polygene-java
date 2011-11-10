package org.qi4j.api.event;

import org.junit.Test;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

/**
 * TODO
 */
public class EventTest
{
    @Test
    public void testActivationEvents() throws Exception
    {
        final StringBuffer events = new StringBuffer();

        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.services( TestService.class ).instantiateOnStartup();
            }
        };

        assembler.application().passivate();

        assembler.application().registerActivationEventListener( new ActivationEventListener()
        {
            @Override
            public void onEvent( ActivationEvent event )
            {
                events.append( event.toString() ).append( "\n" );
            }
        } );

        assembler.application().activate();
        assembler.application().passivate();

        System.out.println(events);
    }

    interface TestService
        extends ServiceComposite
    {

    }
}
