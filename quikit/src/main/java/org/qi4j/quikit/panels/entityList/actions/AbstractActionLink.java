/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.quikit.panels.entityList.actions;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 * @author edward.yakop@gmail.com
 */
public abstract class AbstractActionLink<T> extends AjaxLink<T>
{
    private static final long serialVersionUID = 1L;

    private static final String WICKET_ID_ACTION_LABEL = "actionLabel";

    protected AbstractActionLink(
        String aWicketId,
        IModel<T> aLinkModel,
        ResourceModel aLabelModel )
    {
        super( aWicketId, aLinkModel );

        setOutputMarkupId( true );

        // Action label
        add( new Label<String>( WICKET_ID_ACTION_LABEL, aLabelModel ) );
    }
}
