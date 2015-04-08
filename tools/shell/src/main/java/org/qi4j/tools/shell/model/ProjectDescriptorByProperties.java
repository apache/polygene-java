package org.qi4j.tools.shell.model;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.tools.shell.FileUtils;

public interface ProjectDescriptorByProperties
{
    void parse( String projectName, File propertiesFile );

    Project findProject( String projectName );

    public class Mixin
        implements ProjectDescriptorByProperties
    {
        @Service
        private Model model;

        @Structure
        private UnitOfWorkFactory uowf;

        public void parse( String projectName, File file )
        {
            Map<String, String> props = FileUtils.readPropertiesFile( file );
            Project project = findProject( projectName );

            int layers = Integer.parseInt( props.get( "layers" ) );
            for( int i = 0; i < layers; i++ )
            {
                String layerName = props.get( "layer." + i + ".name" );
                project.createLayer( layerName );
                Layer layer = project.findLayer( layerName );

                for( String use : extractUses( props, i ) )
                {
                    layer.usesLayer( use );
                }

                int modules = Integer.parseInt( props.get( "layer." + i + ".modules" ) );
                for( int j = 0; j < modules; j++ )
                {
                    String moduleName = props.get( "layer." + i + ".module." + j + ".name" );
                    layer.createModule( moduleName );
                }
            }
        }

        private List<String> extractUses( Map<String, String> props, int i )
        {
            String uses = props.get( "layer." + i + ".uses" );
            if( uses == null )
            {
                return Collections.emptyList();
            }
            return Arrays.asList( uses.split( "," ) );
        }

        @UnitOfWorkPropagation
        public Project findProject( String projectName )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Project project = Project.Support.get( uow, projectName );
            if( project == null )
            {
                try
                {
                    model.createProject( projectName );
                }
                catch( ProjectAlreadyExistsException e )
                {
                    // Can not happen
                    e.printStackTrace();
                }
            }
            return project;
        }
    }
}
