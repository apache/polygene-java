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
package org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.context;

import java.util.Collection;
import static java.util.Collections.singletonList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterComposites;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterObjects;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterServices;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.ListListModel;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.structure.Visibility;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ProvidesQi4jContextModel
    implements Qi4jContextModel
{
    private Iterable<ModuleDetailDescriptor> descriptors;

    private final Collection<Visibility> nullFilterMeans;
    private final DefaultComboBoxModel comboboxModel;

    private ListListModel servicesModel;
    private ListListModel entitiesModel;
    private ListListModel compositesModel;
    private ListListModel objectsModel;

    public ProvidesQi4jContextModel( Visibility[] filters, Collection<Visibility> nullFilterMeans )
        throws IllegalArgumentException
    {
        validateNotNull( "nullFilterMeans", nullFilterMeans );

        this.nullFilterMeans = nullFilterMeans;

        if( filters == null )
        {
            comboboxModel = new DefaultComboBoxModel();
        }
        else
        {
            comboboxModel = new DefaultComboBoxModel( filters );
            comboboxModel.addListDataListener( new ComboboxSelectionListener() );
        }
    }

    @SuppressWarnings( "unchecked" )
    public final void updateModel( Iterable<ModuleDetailDescriptor> descriptors )
    {
        servicesModel = ListListModel.EMPTY_MODEL;
        entitiesModel = ListListModel.EMPTY_MODEL;
        compositesModel = ListListModel.EMPTY_MODEL;
        objectsModel = ListListModel.EMPTY_MODEL;

        this.descriptors = descriptors;

        if( descriptors != null )
        {
            Collection<Visibility> filterBy = getFilterBy();

            List<ServiceDetailDescriptor> serviceList = new LinkedList<ServiceDetailDescriptor>();
            List<EntityDetailDescriptor> entityList = new LinkedList<EntityDetailDescriptor>();
            List<CompositeDetailDescriptor> compositeLists = new LinkedList<CompositeDetailDescriptor>();
            List<ObjectDetailDescriptor> objectList = new LinkedList<ObjectDetailDescriptor>();

            for( ModuleDetailDescriptor descriptor : descriptors )
            {
                serviceList.addAll( filterServices( descriptor.services(), filterBy ) );
                entityList.addAll( filterComposites( descriptor.entities(), filterBy ) );
                compositeLists.addAll( filterComposites( descriptor.composites(), filterBy ) );
                objectList.addAll( filterObjects( descriptor.objects(), filterBy ) );
            }

            servicesModel = new ListListModel( serviceList );
            entitiesModel = new ListListModel( entityList );
            compositesModel = new ListListModel( compositeLists );
            objectsModel = new ListListModel( objectList );
        }
    }

    private Collection<Visibility> getFilterBy()
    {
        Visibility filter = (Visibility) comboboxModel.getSelectedItem();
        if( filter == null )
        {
            return nullFilterMeans;
        }
        else
        {
            return singletonList( filter );
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
