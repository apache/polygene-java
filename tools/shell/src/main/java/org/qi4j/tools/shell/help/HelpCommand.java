package org.qi4j.tools.shell.help;

import java.io.BufferedReader;
import java.io.PrintWriter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.tools.shell.Command;

@Mixins( HelpCommand.Mixin.class )
public interface HelpCommand extends Command
{

    public class Mixin
        implements Command
    {

        @Service
        private Iterable<ServiceReference<Command>> commands;

        @Override
        public void execute( String[] args, BufferedReader input, PrintWriter output )
        {
            for( ServiceReference<Command> ref : commands )
            {
                Command command = ref.get();
                String name = command.name();
                String spacing = createSpacing( name );
                output.println( name + spacing + command.description() );
                output.flush();
            }
        }

        private String createSpacing( String name )
        {
            int length = 20 - name.length();
            StringBuilder builder = new StringBuilder( length + 1 );
            for( int i = 0; i < length; i++ )
            {
                builder.append( ' ' );
            }
            return builder.toString();
        }

        @Override
        public String description()
        {
            return "Prints this help text.";
        }

        @Override
        public String name()
        {
            return "help";
        }
    }
}
