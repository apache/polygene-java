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
package org.apache.zest.library.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.subject.Subject;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.library.shiro.assembly.PasswordDomainAssembler;
import org.apache.zest.library.shiro.assembly.StandaloneShiroAssembler;
import org.apache.zest.library.shiro.domain.passwords.PasswordSecurable;
import org.apache.zest.library.shiro.ini.ShiroIniConfiguration;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.*;

public class PasswordDomainTest
        extends AbstractZestTest
{

    // START SNIPPET: domain
    public interface User
            extends PasswordSecurable
    {
    }

    // END SNIPPET: domain
    // START SNIPPET: domain
    @Mixins( UserFactoryMixin.class )
    public interface UserFactory
    {

        User createNewUser( String username, String password );

    }

    // END SNIPPET: domain
    // START SNIPPET: domain
    public static class UserFactoryMixin
            implements UserFactory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        @Service
        private PasswordService passwordService;

        @Override
        public User createNewUser( String username, String password )
        {
            EntityBuilder<User> userBuilder = uowf.currentUnitOfWork().newEntityBuilder( User.class );
            User user = userBuilder.instance();
            user.subjectIdentifier().set( username );
            user.password().set( passwordService.encryptPassword( password ) );
            return userBuilder.newInstance();
        }

    }

    // END SNIPPET: domain
    // START SNIPPET: assembly
    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // END SNIPPET: assembly
        new EntityTestAssembler().assemble( module );
        new RdfMemoryStoreAssembler().assemble( module );
        ModuleAssembly configModule = module;
        // START SNIPPET: assembly
        new StandaloneShiroAssembler().
            withConfig( configModule, Visibility.layer ).
            assemble( module );
        new PasswordDomainAssembler().
            withConfig( configModule, Visibility.layer ).
            assemble( module );

        module.entities( User.class );
        module.services( UserFactory.class );

        // END SNIPPET: assembly
        configModule.forMixin( ShiroIniConfiguration.class ).
                declareDefaults().
                iniResourcePath().set( "classpath:standalone-shiro.ini" );
        // START SNIPPET: assembly
    }

    // END SNIPPET: assembly
    @Test
    public void test()
            throws UnitOfWorkCompletionException
    {

        UnitOfWork uow = uowf.newUnitOfWork();

        UserFactory userFactory = module.findService( UserFactory.class ).get();
        // START SNIPPET: usage
        User user = userFactory.createNewUser( "foo", "bar" );

        // END SNIPPET: usage
        uow.complete();

        uow = uowf.newUnitOfWork();

        // START SNIPPET: usage
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.login( new UsernamePasswordToken( "foo", "bar" ) );

        // END SNIPPET: usage
        assertNotNull( "Unable to authenticate against PasswordRealmService", currentUser.getPrincipal() );

        assertFalse( currentUser.hasRole( "role-one" ) );

        uow.discard();
    }

}