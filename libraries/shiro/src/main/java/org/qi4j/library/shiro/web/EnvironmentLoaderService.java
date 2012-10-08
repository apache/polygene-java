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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;

@Mixins( EnvironmentLoaderService.Mixin.class )
public interface EnvironmentLoaderService
        extends ServletContextListener, ServiceComposite
{

    public class Mixin
            extends EnvironmentLoader
            implements ServletContextListener
    {

        @This
        private Configuration<ShiroIniConfiguration> configuration;

        @Override
        public void contextInitialized( ServletContextEvent sce )
        {
            configuration.refresh();
            ShiroIniConfiguration config = configuration.get();
            String iniResourcePath = config.iniResourcePath().get() == null ? "classpath:shiro.ini" : config.iniResourcePath().get();
            sce.getServletContext().setInitParameter( "shiroConfigLocations", iniResourcePath );
            initEnvironment( sce.getServletContext() );
        }

        public void contextDestroyed( ServletContextEvent sce )
        {
            destroyEnvironment( sce.getServletContext() );
        }

    }

}
