package org.qi4j.tools.shell.model;

import java.io.File;
import java.io.IOException;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.tools.shell.FileUtils;

@Mixins( Project.Mixin.class )
public interface Project extends Nameable
{
    // Commands
    void createLayer( String layerName );

    void applyTemplate( ApplicationTemplate template );

    void setApplicationVersion( String version );

    void setRootPackageName( String name );

    void generate( File directory );

    // Query Methods
    String applicationName();

    String applicationVersion();

    String rootPackageName();

    ApplicationTemplate template();

    Iterable<Layer> layers();

    Layer findLayer( String layerName );

    public class Support
    {
        public static String identity( String name )
        {
            return "Project:" + name;
        }

        public static Project get( UnitOfWork uow, String name )
        {
            return uow.get( Project.class, identity( name ) );
        }
    }

    interface State
    {

        Property<String> name();

        Property<String> version();

        Property<String> rootPackage();

        @UseDefaults
        @Aggregated
        ManyAssociation<Layer> layers();

        @Aggregated
        Association<ApplicationTemplate> template();
    }

    abstract class Mixin
        implements Project
    {
        @This
        private Project self;

        @This
        private State state;

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public String applicationName()
        {
            return state.name().get();
        }

        @Override
        public String applicationVersion()
        {
            return state.version().get();
        }

        @Override
        @UnitOfWorkPropagation
        public void createLayer( String layerName )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Nameable.Support.create( uow, Layer.class, self, layerName );
        }

        @Override
        public void applyTemplate( ApplicationTemplate template )
        {
            state.template().set(template);
        }

        @Override
        public void setApplicationVersion( String version )
        {
            state.version().set( version );
        }

        @Override
        public void setRootPackageName( String name )
        {
            state.rootPackage().set( name );
        }

        @Override
        public void generate( File directory )
        {

        }

        @Override
        public String rootPackageName()
        {
            return state.rootPackage().get();
        }

        @Override
        public ApplicationTemplate template()
        {
            return state.template().get();
        }

        @Override
        @UnitOfWorkPropagation
        public Layer findLayer( String name )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return Nameable.Support.get( uow, Layer.class, self, name );
        }

        @Override
        public Iterable<Layer> layers()
        {
            return state.layers();
        }
    }
}
