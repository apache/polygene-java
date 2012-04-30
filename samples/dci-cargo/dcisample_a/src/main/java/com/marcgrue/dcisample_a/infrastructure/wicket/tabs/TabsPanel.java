package com.marcgrue.dcisample_a.infrastructure.wicket.tabs;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tabs panel that stores tab data as application meta data.
 */
@StatelessComponent
public class TabsPanel extends Panel
{
    public static final MetaDataKey<LinkedHashMap<Class, String[]>> TABS_PANEL_KEY = new MetaDataKey<LinkedHashMap<Class, String[]>>()
    {
        public static final long serialVersionUID = 1L;
    };

    public static <T extends Page> void registerTab( Application app, Class<T> clazz, String ref, String label )
    {
        Map<Class, String[]> tabsInfo = app.getMetaData( TABS_PANEL_KEY );

        if(tabsInfo == null || tabsInfo.isEmpty())
            tabsInfo = new LinkedHashMap<Class, String[]>();

        tabsInfo.put( clazz, new String[]{ref, label} );
        app.setMetaData( TABS_PANEL_KEY, tabsInfo );
    }

    @SuppressWarnings( "unchecked" )
    public TabsPanel( String activeTab )
    {
        super( "tabsPanel" );

        Map<Class, String[]> tabs = getApplication().getMetaData( TABS_PANEL_KEY );
        if (tabs == null || tabs.isEmpty())
            throw new RuntimeException( "Please register one or more tabs." );

        RepeatingView tabsView = new RepeatingView( "tabsView" );

        // Loop "mounted" tabs
        for (Map.Entry<Class, String[]> tab : tabs.entrySet())
        {
            Class pageClass = tab.getKey();
            String tabReference = tab.getValue()[0];
            String tabLabel = tab.getValue()[1];

            WebMarkupContainer tabView = new WebMarkupContainer( tabsView.newChildId() );
            if (tabReference.equals( activeTab ))
                tabView.add( new AttributeModifier( "id", "current" ) );

            BookmarkablePageLink link = new BookmarkablePageLink( "link", pageClass );  // unchecked

            Label label = new Label( "label", tabLabel );

            tabsView.add( tabView.add( link.add( label ) ) );
        }
        add( tabsView );
    }
}