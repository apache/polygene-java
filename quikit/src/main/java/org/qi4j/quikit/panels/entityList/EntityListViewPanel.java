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
package org.qi4j.quikit.panels.entityList;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.qi4j.entity.EntityComposite;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.object.ObjectBuilder;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.quikit.application.QuikitSession;
import static org.qi4j.quikit.panels.entityList.EntityPropertyLabelProvider.PropertyLabel;
import static org.qi4j.quikit.panels.entityList.EntityPropertyValueDataProvider.EntityFieldValue;
import org.qi4j.quikit.panels.entityList.actions.add.AddCompositeLink;

public class EntityListViewPanel<T extends EntityComposite>
    extends Panel
{
    private static final long serialVersionUID = 1L;

    private static final String WICKET_ID_HEADERS = "headers";
    private static final String WICKET_ID_ROWS = "rows";
    private static final String WICKET_ID_NAVIGATOR = "navigator";

    public EntityListViewPanel(
        @Uses String aWicketId, @Uses Class<T> anEntityCompositeClass, @Structure ObjectBuilderFactory anOBF )
    {
        super( aWicketId, new Model<Class<T>>( anEntityCompositeClass ) );

        IModel<Class<T>> model = (IModel<Class<T>>) getDefaultModel();

        // Add headers
        HeadersDataView headersDataView = newEntityHeadersDataView( anOBF, model );
        add( headersDataView );

        // Add rows
        RowsDataView rowsDataView = newRowsDataView( anOBF, model );
        add( rowsDataView );

        add( newActionPanel( anOBF, model, rowsDataView ) );

        // Add paging navigator
        add( new PagingNavigator( WICKET_ID_NAVIGATOR, rowsDataView ) );
    }

    private RepeatingView newActionPanel(
        ObjectBuilderFactory anOBF,
        IModel<Class<T>> anEntityCompositeClassModel,
        RowsDataView rowsDataView )
    {
        RepeatingView actionsRepeater = new RepeatingView( "actions" );

        // Add button
        WebMarkupContainer addContainer = new WebMarkupContainer( actionsRepeater.newChildId() );
        actionsRepeater.add( addContainer );

        ObjectBuilder<AddCompositeLink> builder = anOBF.newObjectBuilder( AddCompositeLink.class );
        builder.use( "action", anEntityCompositeClassModel );
        AddCompositeLink addCompositeLink = builder.newInstance();
        addContainer.add( addCompositeLink );

        return actionsRepeater;
    }

    private HeadersDataView newEntityHeadersDataView( ObjectBuilderFactory anOBF, IModel<Class<T>> model )
    {
        ObjectBuilder<EntityPropertyLabelProvider> builder = anOBF.newObjectBuilder( EntityPropertyLabelProvider.class );
        builder.use( model );
        EntityPropertyLabelProvider provider = builder.newInstance();
        return new HeadersDataView( WICKET_ID_HEADERS, provider );
    }

    private static class HeadersDataView extends DataView<PropertyLabel>
    {
        private static final long serialVersionUID = 1L;

        private static final String WICKET_ID_HEADER = "header";

        private HeadersDataView( String aWicketId, EntityPropertyLabelProvider provider )
        {
            super( aWicketId, provider );
        }

        @Override
        protected final void populateItem( Item<PropertyLabel> item )
        {
            IModel<PropertyLabel> propertyLabelModel = item.getModel();
            Label label = new Label( WICKET_ID_HEADER, propertyLabelModel );
            item.add( label );
        }
    }

    private RowsDataView newRowsDataView( ObjectBuilderFactory anOBF, IModel<Class<T>> model )
    {
        ObjectBuilder<EntityDataProvider> entityDataProviderBuilder =
            anOBF.newObjectBuilder( EntityDataProvider.class );
        entityDataProviderBuilder.use( model );
        EntityDataProvider queryDataProvider = entityDataProviderBuilder.newInstance();
        return new RowsDataView( WICKET_ID_ROWS, queryDataProvider, anOBF );
    }

    private static class RowsDataView extends DataView<EntityComposite>
    {
        private static final long serialVersionUID = 1L;

        private static final String WICKET_ID_COLUMNS = "columns";

        // TODO: Remove transient once ObjectBuilderFactory is serializable
        private transient ObjectBuilderFactory obf;

        public RowsDataView( String aWicketId, EntityDataProvider aDataProvider, ObjectBuilderFactory anOBF )
        {
            super( aWicketId, aDataProvider );

            obf = anOBF;
            setItemsPerPage( 10 );
        }

        @Override
        protected void populateItem( final Item<EntityComposite> item )
        {
            // Add fields
            IModel<EntityComposite> entityModel = item.getModel();
            EntityPropertyValueDataView propertyValueDataView = newEntityPropertyValueDataView( entityModel );
            item.add( propertyValueDataView );

            // Row highlighter
            int itemIndex = item.getIndex();
            String cssClassName = ( itemIndex % 2 == 1 ) ? "even-row" : "odd-row";
            item.add( new AttributeModifier( "class", true, new Model<String>( cssClassName ) ) );
        }

        private EntityPropertyValueDataView newEntityPropertyValueDataView(
            IModel<EntityComposite> anEntityModel )
        {
            //TODO: remove next 2 lines once object builder factory is serializable
            QuikitSession quikitSession = QuikitSession.get();
            obf = quikitSession.getObjectBuilderFactory();

            ObjectBuilder<EntityPropertyValueDataProvider> builder =
                obf.newObjectBuilder( EntityPropertyValueDataProvider.class );
            builder.use( anEntityModel );
            EntityPropertyValueDataProvider entityDataProvider = builder.newInstance();
            return new EntityPropertyValueDataView( entityDataProvider );
        }

        private static class EntityPropertyValueDataView extends DataView<EntityFieldValue>
        {
            private static final long serialVersionUID = 1L;

            private static final String WICKET_ID_COLUMN = "column";

            public EntityPropertyValueDataView( EntityPropertyValueDataProvider entityDataProvider )
            {
                super( WICKET_ID_COLUMNS, entityDataProvider );
            }

            @Override
            protected final void populateItem( Item<EntityFieldValue> item )
            {
                IModel<EntityFieldValue> fieldValueModel = item.getModel();
                Label label = new Label( WICKET_ID_COLUMN, fieldValueModel );
                item.add( label );
            }
        }
    }
}
