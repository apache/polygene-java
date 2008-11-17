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
package org.qi4j.library.swing.visualizer.detailPanel.internal.form.layer;

import java.util.Collection;
import static java.util.Collections.singletonList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterComposites;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterObjects;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterServices;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.ListListModel;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.ListListModel.EMPTY_MODEL;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.context.Qi4jContextModel;
import org.qi4j.library.swing.visualizer.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.structure.Visibility;
import static org.qi4j.structure.Visibility.application;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ProvidesQi4jContextModel
    implements Qi4jContextModel
{
    private final DefaultComboBoxModel comboboxModel;

    private ListListModel servicesModel;
    private ListListModel entitiesModel;
    private ListListModel compositesModel;
    private ListListModel objectsModel;

    ProvidesQi4jContextModel()
        throws IllegalArgumentException
    {
        comboboxModel = new DefaultComboBoxModel();

        servicesModel = EMPTY_MODEL;
        entitiesModel = EMPTY_MODEL;
        compositesModel = EMPTY_MODEL;
        objectsModel = EMPTY_MODEL;
    }

    @SuppressWarnings( "unchecked" )
    public final void updateModel( LayerDetailDescriptor aDescriptor )
    {
        if( aDescriptor != null )
        {
            Collection<Visibility> visibilityApplication = singletonList( application );

            List<ServiceDetailDescriptor> serviceList = new LinkedList<ServiceDetailDescriptor>();
            List<EntityDetailDescriptor> entityList = new LinkedList<EntityDetailDescriptor>();
            List<CompositeDetailDescriptor> compositeLists = new LinkedList<CompositeDetailDescriptor>();
            List<ObjectDetailDescriptor> objectList = new LinkedList<ObjectDetailDescriptor>();

            Iterable<ModuleDetailDescriptor> modules = aDescriptor.modules();
            for( ModuleDetailDescriptor module : modules )
            {
                serviceList.addAll( filterServices( module.services(), visibilityApplication ) );
                entityList.addAll( filterComposites( module.entities(), visibilityApplication ) );
                compositeLists.addAll( filterComposites( module.composites(), visibilityApplication ) );
                objectList.addAll( filterObjects( module.objects(), visibilityApplication ) );
            }

            servicesModel = new ListListModel( serviceList );
            entitiesModel = new ListListModel( entityList );
            compositesModel = new ListListModel( compositeLists );
            objectsModel = new ListListModel( objectList );
        }
        else
        {
            servicesModel = EMPTY_MODEL;
            entitiesModel = EMPTY_MODEL;
            compositesModel = EMPTY_MODEL;
            objectsModel = EMPTY_MODEL;
        }
    }

    public final ComboBoxModel filterModel()
    {
        return comboboxModel;
    }

    public final ListListModel servicesModel()
    {
        return servicesModel;
    }

    public final ListListModel entitiesModel()
    {
        return entitiesModel;
    }

    public final ListListModel compositesModel()
    {
        return compositesModel;
    }

    public final ListListModel objectsModel()
    {
        return objectsModel;
    }
}
