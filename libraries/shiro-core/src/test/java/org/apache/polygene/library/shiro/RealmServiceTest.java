/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.subject.Subject;
import org.junit.Test;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.shiro.assembly.StandaloneShiroAssembler;
import org.apache.polygene.library.shiro.ini.ShiroIniConfiguration;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;

import static org.junit.Assert.assertNotNull;

public class RealmServiceTest
        extends AbstractPolygeneTest
{

    // START SNIPPET: realm-service
    @Mixins( MyRealmMixin.class )
    public interface MyRealmService
            extends Realm, ServiceComposite, ServiceActivation
    {
    }

    // END SNIPPET: realm-service
    // START SNIPPET: realm-service
    public class MyRealmMixin
            extends SimpleAccountRealm
            implements ServiceActivation
    {

        private final PasswordService passwordService;

        public MyRealmMixin()
        {
            super();
            passwordService = new DefaultPasswordService();
            PasswordMatcher matcher = new PasswordMatcher();
            matcher.setPasswordService( passwordService );
            setCredentialsMatcher( matcher );
        }

        public void activateService()
                throws Exception
        {
            // Create a test account
            addAccount( "foo", passwordService.encryptPassword( "bar" ) );
        }

        // END SNIPPET: realm-service
        public void passivateService()
                throws Exception
        {
        }
        // START SNIPPET: realm-service

    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // END SNIPPET: realm-service
        new EntityTestAssembler().assemble( module );
        ModuleAssembly configModule = module;
        // START SNIPPET: realm-service
        new StandaloneShiroAssembler().
            withConfig( configModule, Visibility.layer ).
            assemble( module );
        module.services( MyRealmService.class );

        // END SNIPPET: realm-service
        configModule.forMixin( ShiroIniConfiguration.class ).
                declareDefaults().
                iniResourcePath().set( "classpath:standalone-shiro.ini" );
        // START SNIPPET: realm-service
    }

    // END SNIPPET: realm-service
    @Test
    public void test()
    {
        Subject currentUser = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken( "foo", "bar" );
        currentUser.login( token );
        assertNotNull( "Unable to authenticate against MyRealmService", currentUser.getPrincipal() );
    }

}
