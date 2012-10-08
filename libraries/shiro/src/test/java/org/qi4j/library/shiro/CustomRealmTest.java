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
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.subject.Subject;
import org.junit.Test;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.assembly.StandaloneShiroAssembler;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.assertNotNull;

public class CustomRealmTest
        extends AbstractQi4jTest
{

    // START SNIPPET: custom-realm
    @Mixins( MyRealmMixin.class )
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

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        ModuleAssembly configModule = module;
        new StandaloneShiroAssembler().withConfig( configModule ).assemble( module );
        configModule.forMixin( ShiroIniConfiguration.class ).
                declareDefaults().
                iniResourcePath().set( "classpath:standalone-shiro.ini" );
        // START SNIPPET: custom-realm
        module.services( MyRealmService.class );
        // END SNIPPET: custom-realm
    }

    @Test
    public void test()
    {
        Subject currentUser = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken( "foo", "bar" );
        currentUser.login( token );
        assertNotNull( "Unable to authenticate against MyRealmService", currentUser.getPrincipal() );
    }

}
