package org.qi4j.tools.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.TreeSet;
import org.qi4j.tools.shell.create.CreateProject;
import org.qi4j.tools.shell.help.HelpCommand;

public class Main
{
    private TreeSet<Command> commands = new TreeSet<Command>();

    public static void main( String[] args )
    {
        new Main().run( args );
    }

    public Main()
    {
        this.commands.add( new HelpCommand() );
        this.commands.add( new CreateProject() );
    }

    private void run( String[] args )
    {
        if( !contains( args, "-q" ) )
        {
            System.out.println( "Qi4j - Classes are Dead. Long Live Interfaces!" );
            System.out.println( "----------------------------------------------\n" );
        }
        if( args.length == 0 )
        {
            HelpCommand helpCommand = new HelpCommand();
            helpCommand.setCommands( commands );
            helpCommand.execute( args, input(), output() );
        }
    }

    private boolean contains( String[] args, String s )
    {
        for( String arg : args )
        {
            if( s.equals( arg ) )
            {
                return true;
            }
        }
        return false;
    }

    private PrintWriter output()
    {
        return new PrintWriter( System.out );
    }

    private BufferedReader input()
    {
        return new BufferedReader( new InputStreamReader( System.in ) );
    }
}
