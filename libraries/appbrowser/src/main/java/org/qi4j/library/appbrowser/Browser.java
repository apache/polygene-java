package org.qi4j.library.appbrowser;

import java.util.Stack;
import org.json.JSONException;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.functional.HierarchicalVisitor;

public class Browser
{
    private final ApplicationDescriptor application;
    private final FormatterFactory factory;
    private final Stack<Formatter> stack = new Stack<>();

    public Browser( ApplicationDescriptor application, FormatterFactory factory )
    {
        this.application = application;
        this.factory = factory;
    }

    public void toJson()
        throws BrowserException
    {
        application.accept( new HierarchicalVisitor<Object, Object, BrowserException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws BrowserException
            {
                String simpleName = visited.getClass().getSimpleName();
                Formatter formatter = factory.create( simpleName );
                stack.push(formatter);
                if( formatter == null )
                {
                    System.err.println( "Unknown application component: " + visited.getClass() );
                    return false;
                }
                try
                {
                    System.out.println(visited.getClass().getName());
                    formatter.enter( visited );
                }
                catch( JSONException e )
                {
                    throw new BrowserException( "Formatting failed.", e );
                }
                return true;
            }

            @Override
            public boolean visitLeave( Object visited )
                throws BrowserException
            {
                Formatter formatter = stack.pop();
                if( formatter == null )
                {
                    System.err.println( "Unknown application component: " + visited.getClass() );
                    return false;
                }
                try
                {
                    formatter.leave( visited );
                }
                catch( JSONException e )
                {
                    throw new BrowserException( "Formatting failed.", e );
                }
                return true;
            }

            @Override
            public boolean visit( Object visited )
                throws BrowserException
            {
                Formatter formatter = stack.peek();
                if( formatter == null )
                {
                    System.err.println( "Unknown application component: " + visited.getClass() );
                    return false;
                }
                try
                {
                    formatter.visit( visited );
                }
                catch( JSONException e )
                {
                    throw new BrowserException( "Formatting failed.", e );
                }
                return true;
            }
        } );
    }
}
