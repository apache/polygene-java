/*
 * Copyright 2011 Marc Grue.
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
package org.qi4j.sample.dcicargo.sample_b.infrastructure.wicket.link;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * LinkPanel
 *
 * Convenience panel for displaying a bookmarkable link
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