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
package org.qi4j.library.swing.envisage.detail;

import java.awt.BorderLayout;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.InjectedFieldDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MixinDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ValueDetailDescriptor;
import org.qi4j.library.swing.envisage.util.TableData;
import org.qi4j.spi.composite.DependencyDescriptor;

/**
 * Service Usage tab, which shows all the 'users' of the Service,
 * meaning the 'reverse' of a Dependency, all 'links' pointing into the Service should be listed here.
 * The list should be in three 'sections' according to the Visibility,
 * i.e. one section for all usages from within the same Module,
 * one section for usages within the same Layer
 * and one section for usages from Layers above.
 * I think the order of these sections
 * should be "LayersAbove", "WithinLayer" and "WithinModule"
 * in that order from top.
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class ServiceUsagePane extends DetailPane
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private ServiceDetailDescriptor descriptor;
    private UsageTableModel usageTableModel;
    private JTable usageTable;
    private JPanel contentPane;

    public ServiceUsagePane( DetailModelPane detailModelPane )
    {
        super( detailModelPane );
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        usageTableModel = new UsageTableModel();
        usageTable.setModel( usageTableModel );

        /*TableColumnModel columnModel = usageTable.getColumnModel();
        columnModel.getColumn( 0 ).setPreferredWidth( 90 );
        columnModel.getColumn( 1 ).setPreferredWidth( 550 );
        */
    }

    public void setDescriptor( Object objectDesciptor )
    {
        clear();

        if( !( objectDesciptor instanceof ServiceDetailDescriptor ) )
        {
            return;
        }

        descriptor = (ServiceDetailDescriptor) objectDesciptor;

        // traverse the appDescritor/model to find the usage
        ApplicationDetailDescriptor appDescriptor = descriptor.module().layer().application();
        collectUsage( appDescriptor );

        if( usageTableModel.getRowCount() > 0 )
        {
            usageTableModel.fireTableDataChanged();
        }
    }

    private void clear()
    {
        descriptor = null;
        usageTableModel.clear();
    }

    private void collectUsage( ApplicationDetailDescriptor descriptor )
    {
        for( LayerDetailDescriptor childDescriptor : descriptor.layers() )
        {
            collectInModules( childDescriptor.modules() );
        }
    }

    private void collectInModules( Iterable<ModuleDetailDescriptor> iter )
    {
        for( ModuleDetailDescriptor descriptor : iter )
        {
            collectInServices( descriptor.services() );
            collectInEntities( descriptor.entities() );
            collectInValues( descriptor.values() );
            // TODO collectInTransients

            // Object doesn not have mixin, so does not have the required info
            //collectInObjects( descriptor.objects() );
        }
    }


    private void collectInServices( Iterable<ServiceDetailDescriptor> iter )
    {
        Object configDescriptor = null;

        for( ServiceDetailDescriptor descriptor : iter )
        {
            if( descriptor.equals( this.descriptor ) )
            {
                continue;
            }
            collectInMixin( descriptor.mixins() );
        }
    }

    private void collectInEntities( Iterable<EntityDetailDescriptor> iter )
    {
        for( EntityDetailDescriptor descriptor : iter )
        {
            collectInMixin( descriptor.mixins() );
        }
    }

    private void collectInValues( Iterable<ValueDetailDescriptor> iter )
    {
        for( ValueDetailDescriptor descriptor : iter )
        {
            collectInMixin( descriptor.mixins() );
        }
    }

    private void collectInMixin( Iterable<MixinDetailDescriptor> iter )
    {
        for( MixinDetailDescriptor descriptor : iter )
        {
            Iterable<InjectedFieldDetailDescriptor> iterField = descriptor.injectedFields();
            for( InjectedFieldDetailDescriptor descriptorField : iterField )
            {
                DependencyDescriptor dependencyDescriptor = descriptorField.descriptor().dependency();
                Annotation annotation = dependencyDescriptor.injectionAnnotation();

                Class<? extends Annotation> clazz = annotation.annotationType();
                if( Uses.class.equals( clazz ) || Service.class.equals( clazz ) )
                {
                    TableData rowData = new TableData( 5 );
                    rowData.set( 0, descriptor.composite() );
                    rowData.set( 1, descriptorField );
                    rowData.set( 2, "@" + annotation.annotationType().getSimpleName() );
                    rowData.set( 3, descriptor.composite().module() );
                    rowData.set( 4, descriptor.composite().module().layer() );
                    usageTableModel.addRowData( rowData );
                }
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
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        contentPane = new JPanel();
        contentPane.setLayout( new BorderLayout( 0, 0 ) );
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add( scrollPane1, BorderLayout.CENTER );
        usageTable = new JTable();
        scrollPane1.setViewportView( usageTable );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

    public class UsageTableModel extends AbstractTableModel
    {
        /**
         * the column names for this model
         */
        protected String[] columnNames = { bundle.getString( "Owner.Column" ), bundle.getString( "Usage.Column" ), bundle.getString( "Annotation.Column" ),  bundle.getString( "Module.Column" ), bundle.getString( "Layer.Column" ) };
        protected ArrayList<TableData> data;

        public UsageTableModel()
        {
            data = new ArrayList<TableData>();
        }

        /**
         * Add row data
         * Note, this methods does not do fireTableDataChanged
         *
         * @param rowData TableData to be added
         */
        public void addRowData( TableData rowData )
        {
            data.add( rowData );
        }

        public Object getValueAt( int rowIndex, int columnIndex )
        {
            TableData row = data.get( rowIndex );
            return row.get( columnIndex );
        }

        public void clear()
        {
            data.clear();
            fireTableDataChanged();
        }

        public int getColumnCount()
        {
            return columnNames.length;
        }

        public String getColumnName( int col )
        {
            return columnNames[ col ];
        }

        public int getRowCount()
        {
            return data.size();
        }
    }

}
