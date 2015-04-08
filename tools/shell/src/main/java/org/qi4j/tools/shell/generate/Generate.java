package org.qi4j.tools.shell.generate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.tools.shell.Command;
import org.qi4j.tools.shell.FileUtils;
import org.qi4j.tools.shell.HelpNeededException;
import org.qi4j.tools.shell.StringUtils;
import org.qi4j.tools.shell.model.Layer;
import org.qi4j.tools.shell.model.Model;
import org.qi4j.tools.shell.model.Project;
import org.qi4j.tools.shell.templating.TemplateEngine;

@Mixins( Generate.Mixin.class )
@Concerns( UnitOfWorkConcern.class )
public interface Generate extends Command
{

    public class Mixin
        implements Generate
    {
        @Service
        private Model model;

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        @UnitOfWorkPropagation
        public void execute( String[] args, BufferedReader input, PrintWriter output )
            throws HelpNeededException, IOException
        {
            if( args.length < 2 )
            {
                throw new HelpNeededException();
            }

            Project project = Project.Support.get( uowf.currentUnitOfWork(), args[ 1 ] );
            if( project == null )
            {
                System.err.print( "Project " + args[ 1 ] + " does not exist." );
                return;
            }
            model.setHomeDirectory( new File( System.getProperty( "homeDir" ) ) );
            File projectDir = new File( project.applicationName() );
            if( args.length >= 3 )
            {
                projectDir = new File(args[2]).getAbsoluteFile();
            }
            project.generate( projectDir );
        }

        @Override
        public String description()
        {
            return "Generates the file structure from what has been built up in the database.";
        }

        @Override
        public String name()
        {
            return "generate";
        }
    }
}
