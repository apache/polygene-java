/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.http;

import java.security.Provider;
import java.security.Security;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import org.eclipse.jetty.server.ConnectorStatistics;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.service.ServiceReference;

final class JettyConfigurationHelper
{

    private static final Integer DEFAULT_PORT = 8080;

    private static final String[] DEFAULT_WELCOME_FILES = new String[]
    {
        "index.html"
    };

    private static final String COMA = ",";

    static void configureServer( Server server, JettyConfiguration config )
    {
        // Shutdown
        Integer gracefullShudownTimeout = config.gracefullShutdownTimeout().get();
        if( gracefullShudownTimeout != null )
        {
            server.setStopTimeout( gracefullShudownTimeout );
        }

        // Low resource max idle time
        Integer lowResourceMaxIdleTime = config.lowResourceMaxIdleTime().get();
        if( lowResourceMaxIdleTime != null )
        {
            LowResourceMonitor lowResourceMonitor = new LowResourceMonitor( server );
            lowResourceMonitor.setLowResourcesIdleTimeout( lowResourceMaxIdleTime );
            server.addBean( lowResourceMonitor );
        }

        // Statistics
        if( config.statistics().get() )
        {
            server.addBean( new ConnectorStatistics() );
        }
    }

    static void configureHttp( HttpConfiguration httpConfig, JettyConfiguration config )
    {
        // Date header
        Boolean sendDateHeader = config.sendDateHeader().get();
        if( sendDateHeader != null )
        {
            httpConfig.setSendDateHeader( sendDateHeader );
        }

        // Server version
        Boolean sendServerVersion = config.sendServerVersion().get();
        if( sendServerVersion != null )
        {
            httpConfig.setSendServerVersion( sendServerVersion );
        }

        // Header sizes
        Integer requestHeaderSize = config.requestHeaderSize().get();
        if( requestHeaderSize != null )
        {
            httpConfig.setRequestHeaderSize( requestHeaderSize );
        }
        Integer responseHeaderSize = config.responseHeaderSize().get();
        if( responseHeaderSize != null )
        {
            httpConfig.setResponseHeaderSize( responseHeaderSize );
        }

        // Buffer sizes
        Integer responseBufferSize = config.responseBufferSize().get();
        if( responseBufferSize != null )
        {
            httpConfig.setOutputBufferSize( responseBufferSize );
        }

    }

    static void configureConnector( ServerConnector connector, JettyConfiguration config )
    {
        // Host and Port
        connector.setHost( config.hostName().get() );
        Integer port = config.port().get();
        if( port == null )
        {
            port = DEFAULT_PORT;
        }
        connector.setPort( port );

        // Max idle times
        Integer maxIdleTime = config.maxIdleTime().get();
        if( maxIdleTime != null )
        {
            connector.setIdleTimeout( maxIdleTime );
        }
    }

    static void configureSsl( SslConnectionFactory sslConnFactory, SecureJettyConfiguration config )
    {
        SslContextFactory ssl = sslConnFactory.getSslContextFactory();

        boolean needBouncyCastle = false;

        // KeyStore
        String keystoreType = config.keystoreType().get();
        String keystorePath = config.keystorePath().get();
        String keystorePassword = config.keystorePassword().get();
        ssl.setKeyStoreType( keystoreType );
        if( "PKCS12".equals( keystoreType ) )
        {
            ssl.setKeyStoreProvider( "BC" ); // WARN This one needs BouncyCastle on the classpath
            needBouncyCastle = true;
        }
        ssl.setKeyStorePath( keystorePath );
        ssl.setKeyStorePassword( keystorePassword );

        // Certificate alias
        String certAlias = config.certAlias().get();
        if( certAlias != null )
        {
            ssl.setCertAlias( certAlias );
        }

        // TrustStore
        String truststoreType = config.truststoreType().get();
        String truststorePath = config.truststorePath().get();
        String truststorePassword = config.truststorePassword().get();
        if( truststoreType != null && truststorePath != null )
        {
            ssl.setTrustStoreType( truststoreType );
            if( "PKCS12".equals( truststoreType ) )
            {
                ssl.setTrustStoreProvider( "BC" );
                needBouncyCastle = true;
            }
            ssl.setTrustStorePath( truststorePath );
            ssl.setTrustStorePassword( truststorePassword );
        }

        // Need / Want Client Auth
        Boolean want = config.wantClientAuth().get();
        if( want != null )
        {
            ssl.setWantClientAuth( want );
        }
        Boolean need = config.needClientAuth().get();
        if( need != null )
        {
            ssl.setNeedClientAuth( need );
        }

        // Algorithms
        String secureRandomAlgo = config.secureRandomAlgorithm().get();
        if( secureRandomAlgo != null )
        {
            ssl.setSecureRandomAlgorithm( secureRandomAlgo );
        }
        String cipherExcludesConfigString = config.excludeCipherSuites().get();
        if( cipherExcludesConfigString != null )
        {
            String[] cipherExcludes = cipherExcludesConfigString.split( COMA );
            if( cipherExcludes.length > 0 )
            {
                ssl.setExcludeCipherSuites( cipherExcludes );
            }
        }
        String cipherIncludesConfigString = config.includeCipherSuites().get();
        if( cipherIncludesConfigString != null )
        {
            String[] cipherIncludes = cipherIncludesConfigString.split( COMA );
            if( cipherIncludes.length > 0 )
            {
                ssl.setIncludeCipherSuites( cipherIncludes );
            }
        }

        // SSL Handling
        Boolean cacheSslSessions = config.cacheSslSessions().get();
        if( cacheSslSessions != null )
        {
            ssl.setSessionCachingEnabled( cacheSslSessions );
        }
        ssl.setRenegotiationAllowed( config.allowRenegotiation().get() );

        // Validation Flags
        Integer maxCertPathLength = config.maxCertPathLength().get();
        if( maxCertPathLength != null )
        {
            ssl.setMaxCertPathLength( maxCertPathLength );
        }
        ssl.setValidateCerts( config.validateServerCert().get() );
        ssl.setValidatePeerCerts( config.validatePeerCerts().get() );

        // Validation CRL
        String crlFilePath = config.crlFilePath().get();
        if( crlFilePath != null && crlFilePath.length() > 0 )
        {
            ssl.setCrlPath( crlFilePath );
        }
        ssl.setEnableCRLDP( config.enableCRLDP().get() );

        // Validation OCSP
        ssl.setEnableOCSP( config.enableOCSP().get() );
        String ocspURL = config.ocspResponderURL().get();
        if( ocspURL != null )
        {
            ssl.setOcspResponderURL( ocspURL );
        }

        // Load BouncyCastle ?
        if( needBouncyCastle )
        {
            Provider bc = Security.getProvider( "BC" );
            if( bc == null )
            {
                try
                {
                    Security.addProvider( (Provider) Class.forName( "org.bouncycastle.jce.provider.BouncyCastleProvider" ).newInstance() );
                }
                catch( Exception ex )
                {
                    throw new InvalidApplicationException( "Need to open a PKCS#12 but was unable to register BouncyCastle, check your classpath", ex );
                }
            }
        }
    }

    static void configureContext( ServletContextHandler root, JettyConfiguration config )
    {
        // Context path
        String contextPath = config.contextPath().get();
        if( contextPath != null && contextPath.length() > 0 )
        {
            root.setContextPath( contextPath );
        }

        // Root resource base
        String resourcePath = config.resourcePath().get();
        if( resourcePath != null && resourcePath.length() > 0 )
        {
            root.addServlet( DefaultServlet.class, "/" );
            root.setResourceBase( resourcePath );
        }

        // Max form content size
        Integer maxFormContentSize = config.maxFormContentSize().get();
        if( maxFormContentSize != null )
        {
            root.setMaxFormContentSize( maxFormContentSize );
        }

        // Virtual hosts
        String virtualHostsConfigString = config.virtualHosts().get();
        if( virtualHostsConfigString != null )
        {
            String[] virtualHosts = virtualHostsConfigString.split( COMA );
            if( virtualHosts.length > 0 )
            {
                root.setVirtualHosts( virtualHosts );
            }
        }

        // Welcome files
        String welcomeFilesConfigString = config.welcomeFiles().get();
        if( welcomeFilesConfigString == null )
        {
            root.setWelcomeFiles( DEFAULT_WELCOME_FILES );
        }
        else
        {
            String[] welcomeFiles = welcomeFilesConfigString.split( COMA );
            if( welcomeFiles.length == 0 )
            {
                root.setWelcomeFiles( DEFAULT_WELCOME_FILES );
            }
            else
            {
                root.setWelcomeFiles( welcomeFiles );
            }
        }
    }

    static void addContextListeners( ServletContextHandler root, Iterable<ServiceReference<ServletContextListener>> contextListeners )
    {
        // Iterate the available context listeners and add them to the server
        for( ServiceReference<ServletContextListener> contextListener : contextListeners )
        {
            ContextListenerInfo contextListenerInfo = contextListener.metaInfo( ContextListenerInfo.class );
            Map<String, String> initParams = contextListenerInfo.initParams();
            for( Map.Entry<String, String> entry : initParams.entrySet() )
            {
                root.setInitParameter( entry.getKey(), entry.getValue() );
            }
            root.addEventListener( contextListener.get() );
        }
    }

    static void addServlets( ServletContextHandler root, Iterable<ServiceReference<Servlet>> servlets )
    {
        // Iterate the available servlets and add it to the server
        for( ServiceReference<Servlet> servlet : servlets )
        {
            ServletInfo servletInfo = servlet.metaInfo( ServletInfo.class );
            String servletPath = servletInfo.getPath();
            Servlet servletInstance = servlet.get();
            ServletHolder holder = new ServletHolder( servletInstance );
            holder.setInitParameters( servletInfo.initParams() );
            root.addServlet( holder, servletPath );
        }
    }

    static void addFilters( ServletContextHandler root, Iterable<ServiceReference<Filter>> filters )
    {
        // Iterate the available filters and add them to the server
        for( ServiceReference<Filter> filter : filters )
        {
            FilterInfo filterInfo = filter.metaInfo( FilterInfo.class );
            String filterPath = filterInfo.getPath();

            Filter filterInstance = filter.get();
            FilterHolder holder = new FilterHolder( filterInstance );
            holder.setInitParameters( filterInfo.initParameters() );
            root.addFilter( holder, filterPath, filterInfo.dispatchers() );
        }
    }

    private JettyConfigurationHelper()
    {
    }

}
