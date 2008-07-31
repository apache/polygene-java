package org.qi4j.lib.struts2.example;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class JettyLauncher
{

    public static void main( String[] args )
    {
        Server server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort( 8080 );
        server.setConnectors( new Connector[]{ connector } );

        WebAppContext web = new WebAppContext();

        web.setContextPath( "/example" );

        web.setWar( "example/src/main/webapp/" );
        server.addHandler( web );

        try
        {
            server.start();
            server.join();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.exit( 100 );
        }
    }
}
