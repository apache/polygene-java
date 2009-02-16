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
package org.qi4j.library.swing.envisage.tree;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.api.structure.Application;
import org.qi4j.library.swing.visualizer.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ApplicationDetailDescriptorBuilder;

/**
 * Application Model View as Swing Component.
 * It allow 2 view:
 * - by Structure
 * - by Type
 * 
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class ApplicationModelPanel extends JPanel
{
    public static final String STRUCTURE_VIEW = "Structure";
    public static final String TYPE_VIEW = "Type";

    protected JPanel mainPane;
    protected CardLayout cardLayout;
    protected JTree structureTree;
    protected JTree typeTree;

    protected Energy4Java qi4j;
    protected Application application;

    public ApplicationModelPanel( )
    {
        setLayout(new BorderLayout());

        // init mainPane        
        mainPane = new JPanel();
        cardLayout = new CardLayout( );
        mainPane.setLayout( cardLayout );
        structureTree = new JTree();
        typeTree = new JTree();
        mainPane.add(new JScrollPane( structureTree ), STRUCTURE_VIEW);
        mainPane.add(new JScrollPane( typeTree), TYPE_VIEW);
        add(mainPane,BorderLayout.CENTER);

        // init combo chooser 
        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        comboModel.addElement( STRUCTURE_VIEW );
        comboModel.addElement( TYPE_VIEW );
        JComboBox typeCombo = new JComboBox(comboModel);

        typeCombo.addItemListener( new ItemListener()
        {
            public void itemStateChanged( ItemEvent evt)
            {
                if (evt.getStateChange() == ItemEvent.DESELECTED) { return; }
                cardLayout.show( mainPane, evt.getItem().toString());
                repaint();
            }
        });

        add(typeCombo,BorderLayout.NORTH);
        
        cardLayout.show( mainPane, STRUCTURE_VIEW );
    }

    /** Initialize Qi4J for this component
     * @param qi4j the Energy4Java
     * @param app the Application 
     * */
    public void initQi4J( Energy4Java qi4j, Application app )
    {
        this.qi4j = qi4j;
        this.application = app;

        //ApplicationInstance appInstance = ( ApplicationInstance )app;

        ApplicationSPI applicationSPI = (ApplicationSPI) application;
        ApplicationDetailDescriptor descriptor = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor( applicationSPI );

        // traverse the model and build JTree representation
        MutableTreeNode rootNode1 = StructureModelBuilder.build( descriptor );
        MutableTreeNode rootNode2 = TypeModelBuilder.build( descriptor );

        structureTree.setModel( new DefaultTreeModel(rootNode1) );
        typeTree.setModel( new DefaultTreeModel(rootNode2) );
    }
}
