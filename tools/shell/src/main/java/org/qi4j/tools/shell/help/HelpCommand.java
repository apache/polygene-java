package org.qi4j.tools.shell.help;

import java.io.BufferedReader;
import java.io.PrintWriter;
import org.qi4j.tools.shell.AbstractCommand;
import org.qi4j.tools.shell.Command;

public class HelpCommand extends AbstractCommand
{
    private Iterable<Command> commands;

    public HelpCommand()
    {
    }

    public void setCommands( Iterable<Command> comands )
    {
        this.commands = commands;
    }

    @Override
    public void execute( String[] args, BufferedReader input, PrintWriter output )
    {
        for( Command command : commands )
        {
            output.println( command.name() + "\t" + command.description() );
        }
    }

    @Override
    public String description()
    {
        return "help";
    }

    @Override
    public String name()
    {
        return "Prints this help text.";
    }
}
