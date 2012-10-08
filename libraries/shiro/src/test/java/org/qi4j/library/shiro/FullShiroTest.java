package org.qi4j.library.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.assembly.FullShiroAssembler;
import org.qi4j.library.shiro.concerns.RequiresAuthentication;
import org.qi4j.library.shiro.concerns.SecurityConcern;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.*;

public class FullShiroTest
        extends AbstractQi4jTest
{

    // START SNIPPET: custom-realm
    @Mixins( CustomRealmTest.MyRealmMixin.class )
    public interface MyRealmService
            extends Realm, ServiceComposite, ServiceActivation
    {
    }
    // END SNIPPET: custom-realm

    // START SNIPPET: custom-realm
    public class MyRealmMixin
            extends SimpleAccountRealm
            implements ServiceActivation
    {

        public MyRealmMixin()
        {
            super();
        }

        public void activateService()
                throws Exception
        {
            addAccount( "foo", "bar" );
        }

        public void passivateService()
                throws Exception
        {
        }

    }
    // END SNIPPET: custom-realm

    @Concerns( SecurityConcern.class )
    @Mixins( ServiceUnderTestMixin.class )
    public interface ServiceUnderTest
    {

        @RequiresAuthentication
        void doSomethingAuthenticated();

    }

    public static class ServiceUnderTestMixin
            implements ServiceUnderTest
    {

        public void doSomethingAuthenticated()
        {
            // NOOP
        }

    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new FullShiroAssembler()
        {

            @Override
            protected void assembleShiro( ModuleAssembly module )
            {
                module.services( MyRealmService.class );
            }

        }.withVisibility( Visibility.application ).assemble( module );
        module.services( ServiceUnderTest.class );
    }

    @Test
    public void test()
    {
        ServiceUnderTest underTest = module.findService( ServiceUnderTest.class ).get();

        try {
            underTest.doSomethingAuthenticated();
            fail( "Subject is not authenticated, was expecting UnauthenticatedException." );
        } catch ( UnauthenticatedException expected ) {
        }

        Subject subject = SecurityUtils.getSubject();
        subject.login( new UsernamePasswordToken( "foo", "bar" ) );

        assertNotNull( subject.getPrincipal() );
        assertEquals( subject, SecurityUtils.getSubject() );

        ThreadContext.bind( subject );

        underTest.doSomethingAuthenticated();

        subject.logout();

        ThreadContext.unbindSubject();
    }

}
