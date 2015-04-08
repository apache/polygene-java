package org.qi4j.tools.shell.create.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.tools.shell.Command;
import org.qi4j.tools.shell.HelpNeededException;
import org.qi4j.tools.shell.model.Model;
import org.qi4j.tools.shell.model.Project;
import org.qi4j.tools.shell.model.ProjectDescriptorByProperties;

@Mixins( CreateProject.Mixin.class )
@Concerns( UnitOfWorkConcern.class )
public interface CreateProject extends Command
{
    public class Mixin
        implements Command
    {
        @Service
        private Model model;

        @Service
        private ProjectDescriptorByProperties loadFromProperties;

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
            String projectName = args[ 1 ];

            String rootPackage = "com.example." + projectName.toLowerCase().replace( '-', '_' );
            if( args.length >= 3 )
            {
                rootPackage = args[ 2 ];
            }
            String template = "simple";
            if( args.length >= 4 )
            {
                template = args[ 3 ];
            }
            model.setHomeDirectory( new File( System.getProperty( "homeDir" ) ) );
            String propsLocation = System.getProperty( "homeDir" ) + "/project-templates/" + template + "/project.properties";

            loadFromProperties.parse( projectName, new File( propsLocation ) );
            Project project = loadFromProperties.findProject( projectName );
            project.setApplicationVersion( System.getProperty( "version", "1" ) );
            project.setRootPackageName( rootPackage );
        }

        @Override
        public String description()
        {
            return "Creates a managed project.";
        }

        @Override
        public String name()
        {
            return "create-project";
        }
    }
}
