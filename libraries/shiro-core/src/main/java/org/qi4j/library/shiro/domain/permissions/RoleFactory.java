package org.qi4j.library.shiro.domain.permissions;

import java.util.Arrays;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.functional.Iterables;

@Mixins( RoleFactory.Mixin.class )
public interface RoleFactory
        extends ServiceComposite
{

    Role create( String name, String... permissions );

    Role create( String name, Iterable<String> permissions );

    abstract class Mixin
            implements RoleFactory
    {

        @Structure
        private Module module;

        @Override
        public Role create( String name, String... permissions )
        {
            return create( name, Arrays.asList( permissions ) );
        }

        @Override
        public Role create( String name, Iterable<String> permissions )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            EntityBuilder<Role> roleBuilder = uow.newEntityBuilder( Role.class );
            Role role = roleBuilder.instance();
            role.name().set( name );
            role.permissions().set( Iterables.toList( permissions ) );
            return roleBuilder.newInstance();
        }

    }

}
