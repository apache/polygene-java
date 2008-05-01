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

import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.Uses;
import org.qi4j.composite.scope.Structure;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.entity.EntityComposite;
import org.qi4j.quikit.pages.EntityListViewPage;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.PageParameters;
import java.util.Map;
import java.util.ArrayList;

public class EntityTypeListViewPanel extends Panel
{
    public EntityTypeListViewPanel( @Uses String id, @Structure ModuleBinding module )
    {
        super( id );
        Map<Class<? extends Composite>,CompositeBinding> composites = module.getCompositeBindings();
        ArrayList<CompositeBinding> bindings = new ArrayList<CompositeBinding>();
        for( CompositeBinding binding : composites.values() )
        {
            Class<? extends Composite> type = binding.getCompositeType();
            if( EntityComposite.class.isAssignableFrom( type ) )
            {
                bindings.add( binding );
            }
        }
        final ListView view = new ListView( "composites", bindings )
        {
            protected void populateItem( ListItem listItem )
            {
                CompositeBinding item = ((ArrayList<CompositeBinding>) getModelObject()).get( listItem.getIndex() );
                Class<? extends Composite> type = item.getCompositeType();
                PageParameters params = new PageParameters();
                params.put( "entityType", type.getName() );
                BookmarkablePageLink pageLink = new BookmarkablePageLink( "pagelink", EntityListViewPage.class, params );
                listItem.add( pageLink );
                Label label = new Label( "entityTypeName", type.getSimpleName() );
                pageLink.add( label );
            }
        };
        add( view );
    }
}
