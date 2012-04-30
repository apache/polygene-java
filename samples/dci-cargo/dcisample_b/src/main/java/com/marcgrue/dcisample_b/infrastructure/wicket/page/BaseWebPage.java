package com.marcgrue.dcisample_b.infrastructure.wicket.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseWebPage
 *
 * Convenience base page to provide access to common resources
 */
public class BaseWebPage extends WebPage
{
    public Logger logger = LoggerFactory.getLogger( getClass() );

    static protected TransientBuilderFactory tbf;

    public BaseWebPage( PageParameters pageParameters )
    {
        super( pageParameters );
    }

    public BaseWebPage()
    {
    }

    public static <T extends TransientComposite> T query( Class<T> queryClass )
    {
        return tbf.newTransient( queryClass );
    }

    public static void prepareBaseWebPageClass( TransientBuilderFactory transientBuilderFactory )
    {
        tbf = transientBuilderFactory;
    }
}