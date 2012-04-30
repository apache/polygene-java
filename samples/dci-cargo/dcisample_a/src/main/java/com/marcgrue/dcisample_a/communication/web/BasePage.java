package com.marcgrue.dcisample_a.communication.web;

import com.google.code.joliratools.StatelessAjaxFallbackLink;
import com.marcgrue.dcisample_a.infrastructure.WicketQi4jApplication;
import com.marcgrue.dcisample_a.infrastructure.wicket.page.BaseWebPage;
import com.marcgrue.dcisample_a.infrastructure.wicket.tabs.TabsPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
//import org.wicketstuff.stateless.StatelessAjaxFallbackLink;

/**
 * Base Wicket page of the DCI Sample application
 */
public class BasePage extends BaseWebPage
{
    private final Link toggleLinks;
    private static boolean showLInks = false;
    private Fragment links = new Fragment( "links", "linksFragment", this );

    public BasePage( String activeTab )
    {
        this( activeTab, null );
    }

    public BasePage( String activeTab, PageParameters pageParameters )
    {
        super( pageParameters );

        toggleLinks = new StatelessAjaxFallbackLink<Void>( "toggleLinks" )
        {
            @Override
            public void onClick( AjaxRequestTarget target )
            {
                // Open/close triangle of this toggle link
                add( new AttributeModifier( "class", Model.of( showLInks ? "closed" : "open" ) ) );

                // Show/hide links
                links.setVisible( showLInks = !showLInks );

                // Update with ajax if browser allows
                if (target != null)
                {
                    target.add( links, toggleLinks );
                }
            }
        };
        add( toggleLinks );
        add( links.setOutputMarkupPlaceholderTag( true ).setVisible( false ) );

        add( new Label( "version", ( (WicketQi4jApplication) getApplication() ).appVersion() ) );

        add( new TabsPanel( activeTab ) );
    }
}
