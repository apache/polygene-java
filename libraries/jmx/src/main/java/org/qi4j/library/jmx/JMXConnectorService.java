/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.jmx;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.remote.*;
import javax.security.auth.Subject;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service starts a JMX RMI connector. It also creates an RMI-registry
 * to register the connector. The service is configured by changing the
 * settings in the JMXConnectorConfiguration.
 * <p/>
 * Authentication is done with an optional username+password in the configuration.
 */
@Mixins(JMXConnectorService.JmxConnectorMixin.class)
@Activators( JMXConnectorService.Activator.class )
public interface JMXConnectorService
        extends ServiceComposite
{

    void startJMXConnector()
            throws Exception;

    void stopJMXConnector()
            throws Exception;

    static class Activator
            extends ActivatorAdapter<ServiceReference<JMXConnectorService>>
    {

        @Override
        public void afterActivation( ServiceReference<JMXConnectorService> activated )
                throws Exception
        {
            activated.get().startJMXConnector();
        }

        @Override
        public void beforePassivation( ServiceReference<JMXConnectorService> passivating )
                throws Exception
        {
            passivating.get().stopJMXConnector();
        }

    }

    abstract class JmxConnectorMixin
            implements JMXConnectorService
    {
        final Logger logger = LoggerFactory.getLogger( JMXConnectorService.class.getName() );

        @This
        Configuration<JMXConnectorConfiguration> config;

        @Service
        MBeanServer server;

        Registry registry;
        JMXConnectorServer connector;

        @Override
        public void startJMXConnector() throws Exception
        {
            if (config.get().enabled().get())
            {
                // see java.rmi.server.ObjID
                System.setProperty( "java.rmi.server.randomIDs", "true" );

                int jmxAgentPort = config.get().port().get();

                registry = LocateRegistry.createRegistry( jmxAgentPort );

                String hostName = InetAddress.getLocalHost().getHostName();
                JMXServiceURL url = new JMXServiceURL(
                        "service:jmx:rmi://" + hostName + ":" + jmxAgentPort
                                + "/jndi/rmi://" + hostName + ":" + jmxAgentPort + "/jmxrmi" );
                Map env = new HashMap();

                if(config.get().username().get() != null)
                    env.put( JMXConnectorServer.AUTHENTICATOR, new ConfigurationJmxAuthenticator() );

                try
                {
                    connector = JMXConnectorServerFactory.newJMXConnectorServer( url, env, server );
                    connector.start();
                } catch (Exception e)
                {
                    logger.error( "Could not start JMX connector", e );
                }
            }
        }

        @Override
        public void stopJMXConnector() throws Exception
        {
            // Stop connector
            if (connector != null)
            {
                connector.stop();
                connector = null;
            }

            // Remove registry
            if (registry != null)
            {
                UnicastRemoteObject.unexportObject( registry, true );
                registry = null;
            }
        }

        class ConfigurationJmxAuthenticator implements JMXAuthenticator
        {

            @Override
            public Subject authenticate( Object credentials )
            {

                Subject subject = null;

                if (!(credentials instanceof String[]))
                {
                    // Special case for null so we get a more informative message
                    if (credentials == null)
                    {
                        throw new SecurityException( "Credentials required" );
                    }
                    throw new SecurityException( "Credentials should be String[]" );
                }

                final String[] aCredentials = (String[]) credentials;
                if (aCredentials.length != 2)
                {
                    throw new SecurityException( "Credentials should have 2 elements" );
                }

                String username = aCredentials[0];
                String password = aCredentials[1];

                String configUsername = config.get().username().get();

                if (!(configUsername == null || (configUsername.equals( username ) && !password.equals( config.get().password().get() ))))
                {
                    throw new SecurityException( "User/password combination not valid." );
                }


                subject = new Subject( true,
                        Collections.singleton( new JMXPrincipal( username ) ),
                        Collections.EMPTY_SET,
                        Collections.EMPTY_SET );

                return subject;
            }
        }
    }
}
