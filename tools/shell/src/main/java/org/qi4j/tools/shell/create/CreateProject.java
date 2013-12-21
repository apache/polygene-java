package org.qi4j.tools.shell.create;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import org.qi4j.tools.shell.AbstractCommand;
import org.qi4j.tools.shell.FileUtils;
import org.qi4j.tools.shell.HelpNeededException;

public class CreateProject extends AbstractCommand
{

    @Override
    public void execute( String[] args, BufferedReader input, PrintWriter output )
        throws HelpNeededException
    {
        if( args.length < 1 )
            throw new HelpNeededException();
        String projectName = args[0];
        String template = "defaultproject";
        if( args.length < 2 )
            template = args[1];
        FileUtils.createDir( projectName );
        Map<String, String> props = FileUtils.readPropertiesResource( "templates/" + template + "/project.properties" );
        for( Map.Entry<String,String> p: props.entrySet() )
        {

        }
    }

    @Override
    public String description()
    {
        return "create-project";
    }

    @Override
    public String name()
    {
        return "create-project";
    }
}
