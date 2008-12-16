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

import java.util.ArrayList;
import java.util.Collections;
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
import org.qi4j.library.swing.visualizer.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.api.common.Visibility;
import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;

/**
 * @author edward.yakop@gmail.com
 */
final class AccessiblesQi4jContextModel
    implements Qi4jContextModel
{
    private static final Visibility[] FILTER_VISIBILITY;

    static
    {
        FILTER_VISIBILITY = new Visibility[]
            {
                null,
                layer,
                application
            };
    }

    private LayerDetailDescriptor descriptor;

    private final DefaultComboBoxModel filterModel;

    private ListListModel servicesModel;
    private ListListModel entitiesModel;
    private ListListModel compositesModel;
    private ListListModel objectsModel;

    public AccessiblesQi4jContextModel()
    {
        filterModel = new DefaultComboBoxModel( FILTER_VISIBILITY );
        filterModel.addListDataListener( new FilterSelectionListener() );

        servicesModel = EMPTY_MODEL;
        entitiesModel = EMPTY_MODEL;
        compositesModel = EMPTY_MODEL;
        objectsModel = EMPTY_MODEL;
    }

    @SuppressWarnings( "unchecked" )
    final void updateModel( LayerDetailDescriptor aDescriptor )
    {
        descriptor = aDescriptor;

        if( aDescriptor == null )
        {
            servicesModel = EMPTY_MODEL;
            entitiesModel = EMPTY_MODEL;
            compositesModel = EMPTY_MODEL;
            objectsModel = EMPTY_MODEL;
        }
        else
        {
            Visibility visibility = (Visibility) filterModel.getSelectedItem();

            List<ServiceDetailDescriptor> services = new LinkedList<ServiceDetailDescriptor>();
            List<EntityDetailDescriptor> entities = new LinkedList<EntityDetailDescriptor>();
            List<CompositeDetailDescriptor> composites = new LinkedList<CompositeDetailDescriptor>();
            List<ObjectDetailDescriptor> objects = new LinkedList<ObjectDetailDescriptor>();

            if( visibility == null || visibility == layer )
            {
                List<Visibility> layerFilter = new ArrayList<Visibility>();
                Collections.addAll( layerFilter, layer, application );

                addLayerArtifacts( aDescriptor, layerFilter, services, entities, composites, objects );
            }

            if( visibility == null || visibility == application )
            {
                List<Visibility> filter = singletonList( application );

                Iterable<LayerDetailDescriptor> usedLayers = aDescriptor.usedLayers();
                for( LayerDetailDescriptor usedLayer : usedLayers )
                {
                    addLayerArtifacts( usedLayer, filter, services, entities, composites, objects );
                }
            }

            servicesModel = new ListListModel( services );
            entitiesModel = new ListListModel( entities );
            compositesModel = new ListListModel( composites );
            objectsModel = new ListListModel( objects );
        }
    }

    private void addLayerArtifacts(
        LayerDetailDescriptor layer,
        List<Visibility> filter,
        List<ServiceDetailDescriptor> services,
        List<EntityDetailDescriptor> entities,
        List<CompositeDetailDescriptor> composites,
        List<ObjectDetailDescriptor> objects )
    {
        Iterable<ModuleDetailDescriptor> modules = layer.modules();
        for( ModuleDetailDescriptor module : modules )
        {

            Iterable<ServiceDetailDescriptor> moduleServices = module.services();
            services.addAll( filterServices( moduleServices, filter ) );

            Iterable<EntityDetailDescriptor> moduleEntities = module.entities();
            entities.addAll( filterComposites( moduleEntities, filter ) );

            Iterable<CompositeDetailDescriptor> moduleComposites = module.composites();
            composites.addAll( filterComposites( moduleComposites, filter ) );

            Iterable<ObjectDetailDescriptor> moduleObjects = module.objects();
            objects.addAll( filterObjects( moduleObjects, filter ) );
        }
    }

    public final ComboBoxModel filterModel()
    {
        return filterModel;
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


    private class FilterSelectionListener
        implements ListDataListener
    {
        public void intervalAdded( ListDataEvent e )
        {
            // Ignore
        }

        public void intervalRemoved( ListDataEvent e )
        {
            // Ignore
        }

        public void contentsChanged( ListDataEvent e )
        {
            if( e.getIndex0() == -1 && e.getIndex1() == -1 )
            {
                updateModel( descriptor );
            }
        }
    }
}
