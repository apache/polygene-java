/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.assembly.StandaloneShiroAssembler;
import org.qi4j.library.shiro.ini.IniSecurityManagerService;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class StandaloneShiroTest
        extends AbstractQi4jTest
{

    public void documentationSupport_before()
    {
        // START SNIPPET: before
        IniSecurityManagerFactory factory = new IniSecurityManagerFactory( "classpath:standalone-shiro.ini" );
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager( securityManager );
        // END SNIPPET: before
    }

    public static class documentationSupport_ThreadContext
    {
        // START SNIPPET: thread-context

        @Service
        private IniSecurityManagerService securityManagerService;

        // END SNIPPET: thread-context
        // START SNIPPET: thread-context
        public void interactionBegins()
        {
            ThreadContext.bind( securityManagerService.getSecurityManager() );
        }

        // END SNIPPET: thread-context
        // START SNIPPET: thread-context
        public void interactionEnds()
        {
            ThreadContext.unbindSubject();
            ThreadContext.unbindSecurityManager();
        }
        // END SNIPPET: thread-context

    }

    private static final Logger LOG = LoggerFactory.getLogger( Shiro.LOGGER_NAME );

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        ModuleAssembly configModule = module;
        // START SNIPPET: assembly
        new StandaloneShiroAssembler().withConfig( configModule ).assemble( module );
        // END SNIPPET: assembly
        configModule.forMixin( ShiroIniConfiguration.class ).
                declareDefaults().
                iniResourcePath().set( "classpath:standalone-shiro.ini" );
    }

    @Test
    public void test()
    {
        // get the currently executing user:
        Subject currentUser = SecurityUtils.getSubject();

        // Do some stuff with a Session (no need for a web or EJB container!!!)
        Session session = currentUser.getSession();
        session.setAttribute( "someKey", "aValue" );
        String value = ( String ) session.getAttribute( "someKey" );
        assertEquals( "aValue", value );
        LOG.info( "Retrieved the correct value! [" + value + "]" );

        // let's login the current user so we can check against roles and permissions:
        if ( !currentUser.isAuthenticated() ) {
            UsernamePasswordToken token = new UsernamePasswordToken( "lonestarr", "vespa" );
            token.setRememberMe( true );
            try {
                currentUser.login( token );
            } catch ( UnknownAccountException uae ) {
                fail( "There is no user with username of " + token.getPrincipal() );
            } catch ( IncorrectCredentialsException ice ) {
                fail( "Password for account " + token.getPrincipal() + " was incorrect!" );
            } catch ( LockedAccountException lae ) {
                fail( "The account for username " + token.getPrincipal() + " is locked.  "
                      + "Please contact your administrator to unlock it." );
            } // ... catch more exceptions here (maybe custom ones specific to your application?
            catch ( AuthenticationException ae ) {
                //unexpected condition?  error?
                throw ae;
            }
        }

        //say who they are:
        //print their identifying principal (in this case, a username):
        assertNotNull( currentUser.getPrincipal() );
        LOG.info( "User [" + currentUser.getPrincipal() + "] logged in successfully." );

        //test a role:
        if ( currentUser.hasRole( "schwartz" ) ) {
            LOG.info( "May the Schwartz be with you!" );
        } else {
            fail( "Hello, mere mortal." );
        }

        //test a typed permission (not instance-level)
        if ( currentUser.isPermitted( "lightsaber:weild" ) ) {
            LOG.info( "You may use a lightsaber ring.  Use it wisely." );
        } else {
            fail( "Sorry, lightsaber rings are for schwartz masters only." );
        }

        //a (very powerful) Instance Level permission:
        if ( currentUser.isPermitted( "winnebago:drive:eagle5" ) ) {
            LOG.info( "You are permitted to 'drive' the winnebago with license plate (id) 'eagle5'.  "
                      + "Here are the keys - have fun!" );
        } else {
            fail( "Sorry, you aren't allowed to drive the 'eagle5' winnebago!" );
        }

        //all done - log out!
        currentUser.logout();
    }

}
