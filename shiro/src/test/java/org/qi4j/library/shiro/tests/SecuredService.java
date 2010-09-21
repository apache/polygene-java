package org.qi4j.library.shiro.tests;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.shiro.concerns.RequiresPermissions;
import org.qi4j.library.shiro.concerns.RequiresPermissionsConcern;
import org.qi4j.library.shiro.concerns.RequiresRoles;
import org.qi4j.library.shiro.concerns.RequiresRolesConcern;
import org.qi4j.library.shiro.concerns.RequiresUser;
import org.qi4j.library.shiro.concerns.RequiresUserConcern;
import org.qi4j.library.shiro.tests.username.UsernameFixtures;

@Mixins( value = SecuredService.Mixin.class )
@Concerns( value = { RequiresUserConcern.class, RequiresPermissionsConcern.class, RequiresRolesConcern.class } )
public interface SecuredService
        extends ServiceComposite
{

    void doSomethingThatRequiresNothing();

    @RequiresUser
    void doSomethingThatRequiresUser();

    @RequiresPermissions( value = UsernameFixtures.PERMISSION )
    void doSomethingThatRequiresPermissions();

    @RequiresRoles( value = UsernameFixtures.ROLE )
    void doSomethingThatRequiresRoles();

    public static abstract class Mixin
            implements SecuredService
    {

        public void doSomethingThatRequiresNothing()
        {
            System.out.println( "Doing something that requires nothing" );
        }

        public void doSomethingThatRequiresUser()
        {
            System.out.println( "Doing something that requires a valid user" );
        }

        public void doSomethingThatRequiresPermissions()
        {
            System.out.println( "Doing something that requires permissions" );
        }

        public void doSomethingThatRequiresRoles()
        {
            System.out.println( "Doing something that requires roles" );
        }

    }

}
