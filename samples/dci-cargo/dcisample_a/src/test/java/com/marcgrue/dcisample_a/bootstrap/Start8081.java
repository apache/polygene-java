package com.marcgrue.dcisample_a.bootstrap;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Javadoc
 */
public class Start8081
{
    private Server jetty;

    public static void main( String[] args ) throws Exception
    {
        new Start8081().start();
    }

    public void start() throws Exception
    {
        jetty = new Server();
        SocketConnector connector = new SocketConnector();
        connector.setMaxIdleTime( 1000 * 60 * 60 );
        connector.setSoLingerTime( -1 );
        connector.setPort( 8081 );
        jetty.setConnectors( new Connector[]{connector} );

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setServer( jetty );
        webAppContext.setContextPath( "/" );
        webAppContext.setWar( "src/main/webapp" );
        jetty.addHandler( webAppContext );

        try
        {
            jetty.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit( 100 );
        }
    }

    public void stop() throws Exception
    {
        jetty.stop();
        jetty.join();
    }
}
