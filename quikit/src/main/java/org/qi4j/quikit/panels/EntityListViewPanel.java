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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.EntityComposite;
import org.qi4j.query.Query;
import org.qi4j.quikit.EntityDataProvider;
import org.qi4j.quikit.EntityField;
import org.qi4j.quikit.QueryDataProvider;
import org.qi4j.quikit.QueryMetaDataProvider;

public class EntityListViewPanel extends Panel<Query<EntityComposite>>
{
    private static final long serialVersionUID = 1L;

    private static final String WICKET_ID_HEADERS = "headers";
    private static final String WICKET_ID_ROWS = "rows";
    private static final String WICKET_ID_NAVIGATOR = "navigator";

    public EntityListViewPanel(
        @Uses String id,
        @Uses IModel<Query<EntityComposite>> model,
        @Structure ObjectBuilderFactory factory )
    {
        super( id, model );

        Query<EntityComposite> query = getModelObject();

        // Add headers
        add( createHeaders( factory, query ) );

        // Add rows
        add( createRows( factory, query ) );

        // Add paging navigator
        add( new PagingNavigator( WICKET_ID_NAVIGATOR, createRows( factory, query ) ) );
    }

    private DataView<String> createHeaders( ObjectBuilderFactory factory, Query<EntityComposite> query )
    {
        ObjectBuilder<QueryMetaDataProvider> builder = factory.newObjectBuilder( QueryMetaDataProvider.class );
        builder.use( query.resultType() );
        QueryMetaDataProvider provider = builder.newInstance();

        return new DataView<String>( WICKET_ID_HEADERS, provider )
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected final void populateItem( Item<String> item )
            {
                String header = item.getModelObject();
                Label label = new Label( "header", header );
                item.add( label );
            }
        };
    }

    private DataView createRows( final ObjectBuilderFactory factory,
                                 final Query<EntityComposite> query )
    {
        DataView<EntityComposite> rows = new DataView<EntityComposite>( WICKET_ID_ROWS, new QueryDataProvider( query ) )
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem( final Item<EntityComposite> item )
            {
                EntityComposite entity = item.getModelObject();
                ObjectBuilder<EntityDataProvider> builder = factory.newObjectBuilder( EntityDataProvider.class );
                builder.use( entity );
                EntityDataProvider entityDataProvider = builder.newInstance();
                DataView<EntityField> columns = new DataView<EntityField>( "columns", entityDataProvider )
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected final void populateItem( Item<EntityField> item )
                    {
                        EntityField field = item.getModelObject();
                        Label label = new Label( "column", field.toString() );
                        item.add( label );
                    }
                };
                item.add( columns );
                item.add( new AttributeModifier( "class", true, new AbstractReadOnlyModel<String>()
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject()
                    {
                        int itemIndex = item.getIndex();
                        return ( itemIndex % 2 == 1 ) ? "even-row" : "odd-row";
                    }
                } ) );
            }

        };
        rows.setItemsPerPage( 10 );
        return rows;
    }
}
