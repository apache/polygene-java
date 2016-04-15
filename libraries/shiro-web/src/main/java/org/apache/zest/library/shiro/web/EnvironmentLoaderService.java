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
package org.apache.zest.library.shiro.web;

import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.functional.Iterables;
import org.apache.zest.library.shiro.Shiro;
import org.apache.zest.library.shiro.ini.ShiroIniConfiguration;
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
