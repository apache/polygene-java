package org.qi4j.tools.shell.model;

import java.util.List;
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
import org.qi4j.tools.shell.StringUtils;

@Mixins( Layer.Mixin.class )
public interface Layer extends Nameable
{
    // Commands
    void usesLayer( String layer );

    void createModule( String moduleName );

    // Queries
    Iterable<String> uses();

    String packageName();

    interface State
    {
        @UseDefaults
        Property<String> packageName();

        @UseDefaults
        @Aggregated
        ManyAssociation<Module> modules();

        @UseDefaults
        Property<List<String>> uses();
    }

    abstract class Mixin
        implements Layer
    {
        @This
        private Layer self;

        @This
        private State state;

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public Iterable<String> uses()
        {
            return state.uses().get();
        }

        @Override
        public String packageName()
        {
            return state.packageName().get();
        }

        @Override
        @UnitOfWorkPropagation
        public void createModule( String moduleName )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Nameable.Support.create( uow, Module.class, self, moduleName );
            Module module = Nameable.Support.get( uow, Module.class, self, moduleName );
            state.modules().add( module );
        }

        @Override
        public void usesLayer( String layer )
        {
            state.uses().get().add( layer );
        }
    }
}
