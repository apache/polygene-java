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
package org.qi4j.library.shiro.web;

import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Iterables;
import org.qi4j.library.shiro.Shiro;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( EnvironmentLoaderService.Mixin.class )
public interface EnvironmentLoaderService
        extends ServletContextListener, ServiceComposite
{

    public class Mixin
            extends EnvironmentLoader
            implements ServletContextListener
    {

        private static final Logger LOG = LoggerFactory.getLogger( Shiro.LOGGER_NAME );

        @This
        private Configuration<ShiroIniConfiguration> configuration;

        @Optional
        @Service
        private Iterable<ServiceReference<Realm>> realmsRefs;

        @Override
        public void contextInitialized( ServletContextEvent sce )
        {
            configuration.refresh();
            ShiroIniConfiguration config = configuration.get();
            String iniResourcePath = config.iniResourcePath().get() == null ? "classpath:shiro.ini" : config.iniResourcePath().get();
            sce.getServletContext().setInitParameter( "shiroConfigLocations", iniResourcePath );
            WebEnvironment env = initEnvironment( sce.getServletContext() );

            if ( realmsRefs != null && Iterables.count( realmsRefs ) > 0 ) {

                // Register Realms Services
                RealmSecurityManager realmSecurityManager = ( RealmSecurityManager ) env.getSecurityManager();
                Collection<Realm> iniRealms = new ArrayList<Realm>( realmSecurityManager.getRealms() );
                for ( ServiceReference<Realm> realmRef : realmsRefs ) {
                    iniRealms.add( realmRef.get() );
                    LOG.debug( "Realm Service '{}' registered!", realmRef.identity() );
                }
                realmSecurityManager.setRealms( iniRealms );

            }

        }

        @Override
        public void contextDestroyed( ServletContextEvent sce )
        {
            destroyEnvironment( sce.getServletContext() );
        }

    }

}
