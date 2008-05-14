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
import org.qi4j.quikit.QueryDataProvider;
import org.qi4j.quikit.QueryMetaDataProvider;

public class EntityListViewPanel extends Panel {
	public EntityListViewPanel(@Uses
	String id, @Uses
	IModel model, @Structure
	final ObjectBuilderFactory factory) {
		super(id, model);
		Query<EntityComposite> query = (Query<EntityComposite>) model
				.getObject();
		createHeaders(factory, query);
		DataView rows = createRows(factory, query);
		add(new PagingNavigator("navigator", rows));
	}

	private void createHeaders(ObjectBuilderFactory factory,
			Query<EntityComposite> query) {

		ObjectBuilder<QueryMetaDataProvider> builder = factory
				.newObjectBuilder(QueryMetaDataProvider.class);
		builder.use(query.resultType());
		QueryMetaDataProvider provider = builder.newInstance();
		DataView headers = new DataView("headers", provider) {
			protected void populateItem(Item item) {
				String header = (String) item.getModelObject();
				Label label = new Label("header", header);
				item.add(label);
			}
		};
		add(headers);
	}

	private DataView createRows(final ObjectBuilderFactory factory,
			final Query<EntityComposite> query) {
		DataView rows = new DataView("rows", new QueryDataProvider(query)) {
			protected void populateItem(final Item item) {
				EntityComposite entity = (EntityComposite) item
						.getModelObject();
				ObjectBuilder<EntityDataProvider> builder = factory
						.newObjectBuilder(EntityDataProvider.class);
				builder.use(entity);
				DataView columns = new DataView("columns", builder
						.newInstance()) {
					protected void populateItem(Item item) {
						Label label = new Label("column", item.getModelObject()
								.toString());
						item.add(label);
					}
				};
				item.add(columns);
				item.add(new AttributeModifier("class", true,
						new AbstractReadOnlyModel() {
							public Object getObject() {
								return (item.getIndex() % 2 == 1) ? "even-row"
										: "odd-row";
							}
						}));
			}

		};
		rows.setItemsPerPage(10);
		add(rows);
		return rows;
	}
}
