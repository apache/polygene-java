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
package org.qi4j.quikit.panels.entityList.actions.add;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.quikit.panels.entityList.actions.AbstractActionLink;

/**
 * @author edward.yakop@gmail.com
 * @since 0.2.0
 */
public final class AddCompositeLink extends AbstractActionLink<Class<EntityComposite>>
{
    private static final long serialVersionUID = 1L;

    private static final String RESOURCE_KEY_ADD_ENTITY_COMPOSITE = "addEntityComposite";
    private static final String RESOURCE_DEFAULT_ADD_ENTITY_COMPOSITE = "add";
    private static final ResourceModel ADD_LABEL_MODEL =
        new ResourceModel( RESOURCE_KEY_ADD_ENTITY_COMPOSITE, RESOURCE_DEFAULT_ADD_ENTITY_COMPOSITE );

    public AddCompositeLink(
        @Uses String aWicketId,
        @Uses IModel<Class<EntityComposite>> anEntityCompositeType )
    {
        super( aWicketId, anEntityCompositeType, ADD_LABEL_MODEL );
    }

    @Override
    protected final void onBeforeRender()
    {
        // Enabled the link if button exists
        Class<EntityComposite> entityTypeToAdd = getModelObject();
        boolean isEntityTypeSet = entityTypeToAdd != null;
        setEnabled( isEntityTypeSet );

        super.onBeforeRender();
    }

    @Override
    public final void onClick( AjaxRequestTarget aTarget )
    {
        System.err.println( "Add button clicked" );
    }
}
