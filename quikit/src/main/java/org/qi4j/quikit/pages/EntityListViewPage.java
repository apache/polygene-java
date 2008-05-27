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
package org.qi4j.quikit.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.quikit.panels.entityList.EntityListViewPanel;
import org.qi4j.structure.Module;

public class EntityListViewPage extends WebPage
{
    private static final long serialVersionUID = 1L;

    public static final String PARAM_ENTITY_TYPE = "entityType";

    private static final String WICKET_ID_VIEW_PANEL = "view-panel";

    public EntityListViewPage(
        @Structure ObjectBuilderFactory anOBF,
        @Structure Module aModule,
        @Uses PageParameters parameters )
    {
        Component panel;
        String className = parameters.getString( PARAM_ENTITY_TYPE );
        if( className != null )
        {
            try
            {
                Class entityType = aModule.findClass( className );
                ObjectBuilder<EntityListViewPanel> builder = anOBF.newObjectBuilder( EntityListViewPanel.class );
                builder.use( WICKET_ID_VIEW_PANEL, entityType );
                panel = builder.newInstance();
            }
            catch( ClassNotFoundException e )
            {
                // Display error message
                panel = new Label( WICKET_ID_VIEW_PANEL, "Class [" + className + "] is not found." );
            }
        }
        else
        {
            // Display error message
            panel = new Label( WICKET_ID_VIEW_PANEL, "Parameter [" + PARAM_ENTITY_TYPE + "] must not be [null]." );
        }
        add( panel );
    }
}
