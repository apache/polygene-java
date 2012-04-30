package com.marcgrue.dcisample_a.infrastructure.wicket.link;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Link panel for displaying a bookmarkable link
 */
public class LinkPanel extends Panel
{
    public <T extends Page> LinkPanel( String id, Class<T> pageClass, String rowId )
    {
        this( id, pageClass, rowId, rowId );
    }

    public <T extends Page> LinkPanel( String id, Class<T> pageClass, String rowId, String label )
    {
        super( id );

        PageParameters pageParameters = new PageParameters().set( 0, rowId );
        BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>( "link", pageClass, pageParameters );
        link.add( new Label( "label", label ) );
        add( link );
    }
}