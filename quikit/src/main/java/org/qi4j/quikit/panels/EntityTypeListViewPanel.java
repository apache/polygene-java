/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.quikit.panels;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.quikit.pages.EntityListViewPage;
import static org.qi4j.quikit.pages.EntityListViewPage.PARAM_ENTITY_TYPE;

public final class EntityTypeListViewPanel extends Panel
{
    private static final long serialVersionUID = 1L;

    private static final String WICKET_ID_COMPOSITES = "composites";

    public EntityTypeListViewPanel( @Uses String aWicketId, @Structure Module aModule )
    {
        super( aWicketId );

        ArrayList<Class> bindings = new ArrayList<Class>();
/* JAVADOC
        Map<Class<? extends Composite>, CompositeBinding> composites = aModule.getCompositeBindings();
        for( CompositeBinding binding : composites.values() )
        {
            Class<? extends Composite> type = binding.getCompositeType();
            if( EntityComposite.class.isAssignableFrom( type ) )
            {
                bindings.add( type );
            }
        }
*/

        add( new CompositeListView( bindings ) );
    }

    private class CompositeListView extends ListView<Class>
    {
        private static final long serialVersionUID = 1L;

        private static final String WICKET_ID_PAGELINK = "pagelink";
        private static final String WICKET_ID_ENTITY_TYPE_NAME = "entityTypeName";

        public CompositeListView( List<Class> bindings )
        {
            super( WICKET_ID_COMPOSITES, bindings );
        }

        @Override
        protected void populateItem( ListItem<Class> listItem )
        {
            Class compositeClass = listItem.getModelObject();
            PageParameters params = new PageParameters();
            params.put( PARAM_ENTITY_TYPE, compositeClass.getName() );

            Link pageLink = new BookmarkablePageLink( WICKET_ID_PAGELINK, EntityListViewPage.class, params );
            listItem.add( pageLink );

            Label label = new Label( WICKET_ID_ENTITY_TYPE_NAME, compositeClass.getSimpleName() );
            pageLink.add( label );
        }
    }
}