package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class WebXmlWriter
{

    public void writeClass( Map<String, String> properties )
        throws IOException
    {
        String rootPackage = properties.get( "root.package" );
        String projectName = properties.get( "project.name" );
        try (PrintWriter pw = createPrinter( properties ))
        {
            pw.println(
                String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "\n" +
                    "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\"\n" +
                    "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\"\n" +
                    "         version=\"3.0\">\n" +
                    "\n" +
                    "  <servlet>\n" +
                    "    <servlet-name>zestrest</servlet-name>\n" +
                    "    <servlet-class>org.restlet.ext.servlet.ServerServlet</servlet-class>\n" +
                    "    <init-param>\n" +
                    "      <param-name>org.sensorsink.kooda.mode</param-name>\n" +
                    "      <param-value>production</param-value>\n" +
                    "    </init-param>\n" +
                    "    <init-param>\n" +
                    "      <!-- Application class name -->\n" +
                    "      <param-name>org.restlet.application</param-name>\n" +
                    "      <param-value>%s</param-value>\n" +
                    "    </init-param>\n" +
                    "    <init-param>\n" +
                    "      <!-- Protocols to be bound to-->\n" +
                    "      <param-name>org.restlet.clients</param-name>\n" +
                    "      <param-value>HTTP HTTPS</param-value>\n" +
                    "    </init-param>\n" +
                    "    <load-on-startup>1</load-on-startup>\n" +
                    "  </servlet>\n" +
                    "\n" +
                    "  <servlet-mapping>\n" +
                    "    <servlet-name>zestrest</servlet-name>\n" +
                    "    <url-pattern>/api/*</url-pattern>\n" +
                    "  </servlet-mapping>\n" +
                    "\n" +
                    "</web-app>\n", rootPackage + ".app." + projectName )
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "app/src/main/webapp/WEB-INF/web.xml" ) ) );
    }
}

