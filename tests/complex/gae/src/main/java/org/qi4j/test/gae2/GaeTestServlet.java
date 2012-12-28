/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.test.gae2;

import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class GaeTestServlet
    extends HttpServlet
{
    private UnitTests unitTests;

    @Override
    public void init()
        throws ServletException
    {
        System.out.println( "Starting GAE EntityStore UnitTests" );
        try
        {
            unitTests = new UnitTests();
            unitTests.setUp();
            unitTests.init();
        }
        catch( Exception e )
        {
            throw new ServletException( "Initialization Failed.", e );
        }
    }

    @Override
    public void destroy()
    {
        try
        {
            unitTests.tearDown();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        StringBuffer buffer = new StringBuffer();
        Method[] methods = UnitTests.class.getMethods();
        for( Method m : methods )
        {
            Test annot = m.getAnnotation( Test.class );
            if( annot != null && Modifier.isPublic( m.getModifiers() ) )
            {
                try
                {
                    long t0 = System.currentTimeMillis();
                    m.invoke( unitTests );
                    long time = System.currentTimeMillis() - t0;
                    add( buffer, m.getName() + " success. " + time + "ms.", true, null );
                }
                catch( InvocationTargetException e )
                {
                    add( buffer, m.getName() + " threw an Exception.", false, e.getTargetException() );
                }
                catch( IllegalAccessException e )
                {
                    e.printStackTrace(); // Can not happen?
                }
            }
        }
        PrintWriter pw = resp.getWriter();
        pw.print( "<html><body><h1>Qi4j Entity Store Unit Tests</h1>" );
        pw.print( buffer );
        pw.print( "</body></html>" );
    }

    private void add( StringBuffer out, String message, boolean success, Throwable exception )
    {
        if( success )
        {
            out.append( "<p style=\"color:green;\">" );
            out.append( message );
            out.append( "</p>" );
        }
        else
        {
            StringWriter trace = new StringWriter();
            PrintWriter pw = new PrintWriter( trace );
            exception.printStackTrace( pw );
            pw.flush();
            pw.close();
            out.append( "<p style=\"color:red;\">" );
            out.append( message );
            out.append( "</p><pre><code>" );
            out.append( trace );
            out.append( "</code></pre>" );
        }

    }
}