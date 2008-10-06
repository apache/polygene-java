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
package org.qi4j.library.swing.visualizer.detailPanel.internal.form.common;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import static java.util.Collections.singletonList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterComposites;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterObjects;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.CollectionUtils.filterServices;
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
public final class ModuleProvidesForm
{
    private Iterable<ModuleDetailDescriptor> descriptors;

    private final Collection<Visibility> nullFilterMeans;
    private JLabel filterLabel;
    private JComboBox visibilityFilter;

    private JList services;
    private JList entities;
    private JList composites;
    private JList objects;

    private JPanel contextForm;

    public ModuleProvidesForm( Visibility[] filters, Collection<Visibility> nullFilterMeans )
        throws IllegalArgumentException
    {
        $$$setupUI$$$();

        this.nullFilterMeans = nullFilterMeans;
        if( filters != null )
        {
            visibilityFilter.setModel( new DefaultComboBoxModel( filters ) );
            visibilityFilter.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed( ActionEvent e )
                    {
                        updateModel( descriptors );
                    }
                }
            );
        }
        else
        {
            filterLabel.setVisible( false );
            visibilityFilter.setVisible( false );
        }
    }

    @SuppressWarnings( "unchecked" )
    public final void updateModel( Iterable<ModuleDetailDescriptor> descriptors )
    {
        ListListModel servicesModel = ListListModel.EMPTY_MODEL;
        ListListModel entitiesModel = ListListModel.EMPTY_MODEL;
        ListListModel compositesModel = ListListModel.EMPTY_MODEL;
        ListListModel objectsModel = ListListModel.EMPTY_MODEL;

        this.descriptors = descriptors;

        boolean isDescriptorNotNull = false;
        if( descriptors != null )
        {
            Collection<Visibility> filterBy = getFilterBy();

            List<ServiceDetailDescriptor> serviceList = new LinkedList<ServiceDetailDescriptor>();
            List<EntityDetailDescriptor> entityList = new LinkedList<EntityDetailDescriptor>();
            List<CompositeDetailDescriptor> compositeLists = new LinkedList<CompositeDetailDescriptor>();
            List<ObjectDetailDescriptor> objectList = new LinkedList<ObjectDetailDescriptor>();

            for( ModuleDetailDescriptor descriptor : descriptors )
            {
                isDescriptorNotNull = true;

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
        visibilityFilter.setEnabled( isDescriptorNotNull );

        services.setModel( servicesModel );
        entities.setModel( entitiesModel );
        composites.setModel( compositesModel );
        objects.setModel( objectsModel );
    }

    private Collection<Visibility> getFilterBy()
    {
        Visibility visibility = (Visibility) visibilityFilter.getSelectedItem();
        if( visibility == null )
        {
            return nullFilterMeans;
        }
        else
        {
            return singletonList( visibility );
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        contextForm = new JPanel();
        contextForm.setLayout( new FormLayout( "fill:max(d;4px):noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:max(p;75dlu):noGrow,left:4px:noGrow,fill:max(d;4px):grow,fill:max(d;4px):noGrow", "center:max(d;4px):noGrow,center:p:noGrow,top:4px:noGrow,center:d:noGrow,fill:p:grow,top:4px:noGrow" ) );
        final JPanel panel1 = new JPanel();
        panel1.setLayout( new FormLayout( "fill:max(p;50dlu):grow,left:5dlu:noGrow,fill:max(p;50dlu):grow,left:4dlu:noGrow,fill:max(p;50dlu):grow,left:4dlu:noGrow,fill:max(p;50dlu):grow", "center:d:noGrow,top:4dlu:noGrow,center:d:grow" ) );
        ( (FormLayout) panel1.getLayout() ).setColumnGroups( new int[][]{ new int[]{ 1, 3, 5, 7 } } );
        CellConstraints cc = new CellConstraints();
        contextForm.add( panel1, cc.xywh( 2, 4, 5, 2 ) );
        final JLabel label1 = new JLabel();
        label1.setText( "Services" );
        panel1.add( label1, cc.xy( 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT ) );
        final JLabel label2 = new JLabel();
        label2.setText( "Entities" );
        panel1.add( label2, cc.xy( 3, 1, CellConstraints.CENTER, CellConstraints.DEFAULT ) );
        final JLabel label3 = new JLabel();
        label3.setText( "Composites" );
        panel1.add( label3, cc.xy( 5, 1, CellConstraints.CENTER, CellConstraints.DEFAULT ) );
        final JLabel label4 = new JLabel();
        label4.setText( "Objects" );
        panel1.add( label4, cc.xy( 7, 1, CellConstraints.CENTER, CellConstraints.DEFAULT ) );
        services = new JList();
        panel1.add( services, cc.xy( 1, 3, CellConstraints.DEFAULT, CellConstraints.FILL ) );
        entities = new JList();
        panel1.add( entities, cc.xy( 3, 3, CellConstraints.DEFAULT, CellConstraints.FILL ) );
        composites = new JList();
        panel1.add( composites, cc.xy( 5, 3, CellConstraints.DEFAULT, CellConstraints.FILL ) );
        objects = new JList();
        panel1.add( objects, cc.xy( 7, 3, CellConstraints.DEFAULT, CellConstraints.FILL ) );
        filterLabel = new JLabel();
        filterLabel.setText( "Filter" );
        contextForm.add( filterLabel, cc.xy( 2, 2 ) );
        visibilityFilter = new JComboBox();
        visibilityFilter.setEnabled( false );
        contextForm.add( visibilityFilter, cc.xy( 4, 2 ) );
        filterLabel.setLabelFor( visibilityFilter );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contextForm;
    }
}
