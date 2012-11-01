/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.library.swing.entityviewer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptorBuilder;
import org.qi4j.tools.model.descriptor.EntityDetailDescriptor;
import org.qi4j.tools.model.descriptor.LayerDetailDescriptor;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;

import static org.qi4j.functional.Iterables.first;

/**
 * The Entity Viewer.
 */
public class EntityViewer
{
    private JPanel mainPane;
    private JComboBox entitiesCombo;
    private JPanel propertiesAreaPane;
    private JSplitPane splitPane;
    private PropertiesPanel propertiesPanel;
    private TreePanel treePanel;

    private Qi4jSPI qi4jspi;
    private ApplicationDescriptor model;
    private Application application;

    private JFrame frame;

    public void show( Qi4jSPI qi4jspi, ApplicationDescriptor model, Application application )
    {
        this.qi4jspi = qi4jspi;
        this.model = model;
        this.application = application;

        initUI();

        frame = new JFrame();
        frame.setContentPane( mainPane );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setSize( 600, 600 );
        //frame.pack();
        frame.setVisible( true );
    }

    public void stop()
    {
        if( frame != null & frame.isDisplayable() )
        {
            frame.dispose();
            frame = null;
        }
    }

    private void initUI()
    {
        propertiesPanel = new PropertiesPanel();
        propertiesPanel.initializeQi4J( qi4jspi );
        propertiesAreaPane.add( propertiesPanel, BorderLayout.CENTER );

        treePanel = new TreePanel();
        treePanel.initializeQi4J( qi4jspi, model );
        treePanel.reload();
        splitPane.setLeftComponent( treePanel );

        splitPane.setDividerLocation( 200 );

        DefaultComboBoxModel entityComboModel = new DefaultComboBoxModel();
        entitiesCombo.setModel( entityComboModel );

        entitiesCombo.addItemListener( new ItemListener()
        {
            @Override
            public void itemStateChanged( ItemEvent evt )
            {
                entitiesComboItemStateChanged( evt );
            }
        } );

        treePanel.getTreeComponent().addTreeSelectionListener( new TreeSelectionListener()
        {
            @Override
            public void valueChanged( TreeSelectionEvent evt )
            {
                treePanelValueChanged( evt );
            }
        } );

        initEntityCombo( entityComboModel );
    }

    private void initEntityCombo( DefaultComboBoxModel entityComboModel )
    {
        // create the visitor to traverse the QI4J to find the module
        ApplicationDetailDescriptor visitor = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor( model );

        // find all entities
        Iterable<LayerDetailDescriptor> layerDescIter = visitor.layers();
        for( LayerDetailDescriptor layerDesc : layerDescIter )
        {
            Iterable<ModuleDetailDescriptor> moduleDescIter = layerDesc.modules();
            for( ModuleDetailDescriptor moduleDesc : moduleDescIter )
            {
                Iterable<EntityDetailDescriptor> entityDescIter = moduleDesc.entities();
                for( EntityDetailDescriptor entity : entityDescIter )
                {
                    entityComboModel.addElement( entity );
                }
            }
        }
    }

    /**
     * Create simple query (display all properties) based on the supplied class.
     *
     * @param module the module to create the query
     * @param clazz  the class to be queried
     *
     * @return query
     */
    @SuppressWarnings( "unchecked" )
    protected Query createQuery( Module module, Class clazz )
    {
        UnitOfWork uow = module.newUnitOfWork();
        QueryBuilder qb = module.newQueryBuilder( clazz );
        return uow.newQuery( qb );
    }

    private Module findModule( EntityDetailDescriptor descriptor )
    {
        String lName = descriptor.module().layer().descriptor().name();
        String mName = descriptor.module().descriptor().name();

        return application.findModule( lName, mName );
    }

    /**
     * Event Handler for EntitiesComboBox
     *
     * @param evt the Event
     */
    private void entitiesComboItemStateChanged( ItemEvent evt )
    {
        if( evt.getStateChange() == ItemEvent.DESELECTED )
        {
            return;
        }

        EntityDetailDescriptor entityDescriptor = (EntityDetailDescriptor) entitiesCombo.getSelectedItem();
        Class clazz = first( entityDescriptor.descriptor().types() );

        Module module = findModule( entityDescriptor );
        Query query = createQuery( module, clazz );
        propertiesPanel.reload( query );
    }

    /**
     * Event Handler for TreePanel
     *
     * @param evt the Event
     */
    public void treePanelValueChanged( TreeSelectionEvent evt )
    {
        TreePath path = evt.getPath();
        Object source = path.getLastPathComponent();
        if( source == null )
        {
            return;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) source;
        Object obj = node.getUserObject();

        if( obj == null )
        {
            return;
        }

        Class<?> clazz = obj.getClass();
        if( EntityDetailDescriptor.class.isAssignableFrom( clazz ) )
        {
            EntityDetailDescriptor entityDesc = (EntityDetailDescriptor) obj;
            Class entityType = first( entityDesc.descriptor().types());

            // Update the selected item on the combo box, which in turn update the properties table
            ComboBoxModel comboModel = entitiesCombo.getModel();
            int index = -1;
            for( int i = 0; i < comboModel.getSize(); i++ )
            {
                EntityDetailDescriptor entityDesc1 = (EntityDetailDescriptor) comboModel.getElementAt( i );
                Class entityType1 = first( entityDesc1.descriptor().types());

                if( entityType1.equals( entityType ) )
                {
                    index = i;
                    break;
                }
            }

            if( index >= 0 )
            {
                entitiesCombo.setSelectedIndex( index );
            }
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     */
    private void $$$setupUI$$$()
    {
        mainPane = new JPanel();
        mainPane.setLayout( new BorderLayout( 0, 0 ) );
        splitPane = new JSplitPane();
        mainPane.add( splitPane, BorderLayout.CENTER );
        propertiesAreaPane = new JPanel();
        propertiesAreaPane.setLayout( new BorderLayout( 0, 0 ) );
        splitPane.setRightComponent( propertiesAreaPane );
        final JPanel panel1 = new JPanel();
        panel1.setLayout( new GridBagLayout() );
        propertiesAreaPane.add( panel1, BorderLayout.NORTH );
        entitiesCombo = new JComboBox();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add( entitiesCombo, gbc );
        final JLabel label1 = new JLabel();
        label1.setText( "Entity" );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add( label1, gbc );
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add( spacer1, gbc );
    }

    public JComponent $$$getRootComponent$$$()
    {
        return mainPane;
    }
}
