package org.qi4j.tools.shell.model;

import java.io.File;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;

@Mixins( Model.Mixin.class )
public interface Model
{
    // Commands
    void setHomeDirectory( File homeDir );

    void createProject( String name )
        throws ProjectAlreadyExistsException;

    void addTemplate( String name, String template )
        throws TemplateAlreadyExistsException;

    // Queries
    File homeDirectory();

    Project findProject( String name );

    interface State
    {
        Property<File> homeDirectory();
    }

    class Mixin
        implements Model
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @This
        private State state;

        @Override
        public void setHomeDirectory( File homeDir )
        {
            state.homeDirectory().set( homeDir );
        }

        @UnitOfWorkPropagation
        public void createProject( String name )
            throws ProjectAlreadyExistsException
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Project existing = Project.Support.get( uow, name );
            if( existing != null )
            {
                throw new ProjectAlreadyExistsException( name );
            }
            Nameable.Support.create( uow, Project.class, null, name );
        }

        @Override
        public void addTemplate( String name, String template )
            throws TemplateAlreadyExistsException
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            ApplicationTemplate existing = ApplicationTemplate.Support.get( uow, name );
            if( existing != null )
            {
                throw new TemplateAlreadyExistsException( name );
            }
            EntityBuilder<ApplicationTemplate> builder =
                uow.newEntityBuilder( ApplicationTemplate.class, "Template(" + name + ")" );
            ApplicationTemplate.State prototype = builder.instanceFor( ApplicationTemplate.State.class );
            prototype.template().set( template );
            Nameable.State naming = builder.instanceFor( Nameable.State.class );
            naming.name().set( name );
            builder.newInstance();
        }

        @Override
        public File homeDirectory()
        {
            return state.homeDirectory().get();
        }

        @Override
        @UnitOfWorkPropagation
        public Project findProject( String name )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return Nameable.Support.get(uow, Project.class, null, name );
        }
    }
}
