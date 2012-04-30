package com.marcgrue.dcisample_b.infrastructure.wicket.prevnext;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * PrevNext
 *
 * Panel for displaying and administrating pre/next arrows to browse detail pages
 * of records from a list. Record ids are kept in the session. Also takes care of
 * showing disabled icons at beginning/end.
 */
@StatelessComponent
public class PrevNext extends Panel
{
    private static final MetaDataKey<ArrayList<String>> PREV_NEXT_PANEL_KEY = new MetaDataKey<ArrayList<String>>()
    {
        private static final long serialVersionUID = 1L;
    };

    public static void registerIds( Session session, ArrayList<String> ids )
    {
        if (ids == null || ids.isEmpty())
            throw new RuntimeException( "Please register a list of ids." );

        session.setMetaData( PREV_NEXT_PANEL_KEY, ids );
        session.bind();
    }

    public static void addId( Session session, String id )
    {
        if (id == null || id.isEmpty() )
            throw new RuntimeException( "Can't register empty id." );

        ArrayList<String> ids = session.getMetaData( PREV_NEXT_PANEL_KEY );
        if (ids == null || ids.isEmpty())
            ids = new ArrayList<String>( );

        ids.add( id );
        session.setMetaData( PREV_NEXT_PANEL_KEY, ids );
        session.bind();
    }

    public PrevNext( String id, Class<? extends Page> pageClass, String actualId )
    {
        super( id );

        List<String> ids = getSession().getMetaData( PREV_NEXT_PANEL_KEY );

        if (ids == null || ids.size() == 0)
        {
            setEnabled( false );
            setVisible( false );
            return;
        }

        String prev = null;
        String current;
        for (Iterator<String> it = ids.iterator(); it.hasNext(); )
        {
            current = it.next();
            if (current.equals( actualId ))
            {
                if (prev == null)
                    add( new WebMarkupContainer( "prev" ).add( new AttributeModifier( "class", "prevDisabled" ) ) );
                else
                    add( new BookmarkablePageLink<Void>( "prev", pageClass, new PageParameters().set( 0, prev ) ) );

                if (!it.hasNext())
                    add( new WebMarkupContainer( "next" ).add( new AttributeModifier( "class", "nextDisabled" ) ) );
                else
                    add( new BookmarkablePageLink<Void>( "next", pageClass, new PageParameters().set( 0, it.next() ) ) );

                return;
            }
            prev = current;
        }

        throw new RuntimeException( "Passed tracking id was not found in the PrevNextSession." );
    }
}