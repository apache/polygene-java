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
import java.util.Map;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.EntityComposite;
import org.qi4j.quikit.pages.EntityListViewPage;
import static org.qi4j.quikit.pages.EntityListViewPage.PARAM_ENTITY_TYPE;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.structure.ModuleBinding;

public final class EntityTypeListViewPanel extends Panel
{
    private static final long serialVersionUID = 1L;

    private static final String WICKET_ID_COMPOSITES = "composites";

    public EntityTypeListViewPanel( @Uses String aWicketId, @Structure ModuleBinding aModule )
    {
        super( aWicketId );

        Map<Class<? extends Composite>, CompositeBinding> composites = aModule.getCompositeBindings();
        ArrayList<CompositeBinding> bindings = new ArrayList<CompositeBinding>();
        for( CompositeBinding binding : composites.values() )
        {
            Class<? extends Composite> type = binding.getCompositeType();
            if( EntityComposite.class.isAssignableFrom( type ) )
            {
                bindings.add( binding );
            }
        }

        add( new CompositeListView( bindings ) );
    }

    private class CompositeListView extends ListView<CompositeBinding>
    {
        private static final long serialVersionUID = 1L;

        private static final String WICKET_ID_PAGELINK = "pagelink";
        private static final String WICKET_ID_ENTITY_TYPE_NAME = "entityTypeName";

        public CompositeListView( List<CompositeBinding> bindings )
        {
            super( WICKET_ID_COMPOSITES, bindings );
        }

        @Override
        protected void populateItem( ListItem<CompositeBinding> listItem )
        {
            CompositeBinding item = listItem.getModelObject();
            Class<? extends Composite> type = item.getCompositeType();
            PageParameters params = new PageParameters();
            params.put( PARAM_ENTITY_TYPE, type.getName() );

            Link pageLink = new BookmarkablePageLink( WICKET_ID_PAGELINK, EntityListViewPage.class, params );
            listItem.add( pageLink );

            Label label = new Label( WICKET_ID_ENTITY_TYPE_NAME, type.getSimpleName() );
            pageLink.add( label );
        }
    }
}