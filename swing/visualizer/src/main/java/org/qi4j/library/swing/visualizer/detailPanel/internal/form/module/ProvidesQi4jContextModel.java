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
package org.qi4j.library.swing.visualizer.detailPanel.internal.form.module;

import java.util.Collection;
import static java.util.Collections.singletonList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterComposites;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterObjects;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterServices;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.ListListModel;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.ListListModel.EMPTY_MODEL;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.context.Qi4jContextModel;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.structure.Visibility;
import static org.qi4j.structure.Visibility.application;
import static org.qi4j.structure.Visibility.layer;
import static org.qi4j.structure.Visibility.module;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ProvidesQi4jContextModel
    implements Qi4jContextModel
{
    private static final Visibility[] FILTERS;

    static
    {
        FILTERS = new Visibility[]
            {
                module,
                layer,
                application
            };
    }

    private Iterable<ModuleDetailDescriptor> descriptors;

    private final DefaultComboBoxModel comboboxModel;

    private ListListModel servicesModel;
    private ListListModel entitiesModel;
    private ListListModel compositesModel;
    private ListListModel objectsModel;

    public ProvidesQi4jContextModel()
        throws IllegalArgumentException
    {
        comboboxModel = new DefaultComboBoxModel( FILTERS );
        comboboxModel.addListDataListener( new ComboboxSelectionListener() );

        servicesModel = EMPTY_MODEL;
        entitiesModel = EMPTY_MODEL;
        compositesModel = EMPTY_MODEL;
        objectsModel = EMPTY_MODEL;
    }

    @SuppressWarnings( "unchecked" )
    public final void updateModel( Iterable<ModuleDetailDescriptor> descriptors )
    {
        this.descriptors = descriptors;

        if( descriptors != null )
        {
            Visibility selectedFilter = (Visibility) comboboxModel.getSelectedItem();
            Collection<Visibility> filters = singletonList( selectedFilter );

            List<ServiceDetailDescriptor> serviceList = new LinkedList<ServiceDetailDescriptor>();
            List<EntityDetailDescriptor> entityList = new LinkedList<EntityDetailDescriptor>();
            List<CompositeDetailDescriptor> compositeLists = new LinkedList<CompositeDetailDescriptor>();
            List<ObjectDetailDescriptor> objectList = new LinkedList<ObjectDetailDescriptor>();

            for( ModuleDetailDescriptor descriptor : descriptors )
            {
                serviceList.addAll( filterServices( descriptor.services(), filters ) );
                entityList.addAll( filterComposites( descriptor.entities(), filters ) );
                compositeLists.addAll( filterComposites( descriptor.composites(), filters ) );
                objectList.addAll( filterObjects( descriptor.objects(), filters ) );
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

    private final class ComboboxSelectionListener
        implements ListDataListener
    {
        public final void intervalAdded( ListDataEvent e )
        {
            // Ignore
        }

        public final void intervalRemoved( ListDataEvent e )
        {
            // Ignore
        }

        public final void contentsChanged( ListDataEvent e )
        {
            // Selection change
            if( e.getIndex0() == -1 &&
                e.getIndex1() == -1 )
            {
                updateModel( descriptors );
            }
        }
    }
}
