package org.qi4j.tools.shell.model;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;

@Mixins( Nameable.Mixin.class )
public interface Nameable
{
    String name();

    interface State
    {
        Property<String> name();
    }

    public class Mixin
        implements Nameable
    {
        @This
        private State state;

        @Override
        public String name()
        {
            return state.name().get();
        }
    }

    public class Support
    {
        public static <T extends Nameable> String identity( Class<T> type, Nameable parent, String name )
        {
            if( parent == null )
            {
                return type.getSimpleName() + "(" + name + ")";
            }
            return parent.name() + "." + type.getSimpleName() + "(" + name + ")";
        }

        public static <T extends Nameable> T get( UnitOfWork uow, Class<T> type, Nameable parent, String name )
        {
            String identity = identity( type, parent, name );
            return uow.get( type, identity );
        }

        public static <T extends Nameable> void create( UnitOfWork uow, Class<T> type, Nameable parent, String name )
        {
            EntityBuilder<T> builder = uow.newEntityBuilder( type, identity( type, parent, name ) );
            Nameable.State prototype = builder.instanceFor( Nameable.State.class );
            prototype.name().set( name );
            builder.newInstance();
        }
    }
}
