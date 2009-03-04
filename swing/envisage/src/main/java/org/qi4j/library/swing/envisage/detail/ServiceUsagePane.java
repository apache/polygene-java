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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.swing.envisage.event.LinkEvent;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.InjectedFieldDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MixinDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
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

    private Object linkObject;
    private Cursor defaultCursor;
    private Cursor linkCursor;

    public ServiceUsagePane( DetailModelPane detailModelPane )
    {
        super( detailModelPane );
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        usageTableModel = new UsageTableModel();
        usageTable.setModel( usageTableModel );
        usageTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        TableColumnModel columnModel = usageTable.getColumnModel();
        columnModel.getColumn( 0 ).setCellRenderer( new OwnerCellRenderer() );
        columnModel.getColumn( 1 ).setCellRenderer( new FieldCellRenderer() );

        /*columnModel.getColumn( 0 ).setPreferredWidth( 90 );
        columnModel.getColumn( 1 ).setPreferredWidth( 550 );
        */

        defaultCursor = getCursor();
        linkCursor = LinkEvent.LINK_CURSOR;

        MouseInputAdapter mouseInputListener = new MouseInputAdapter()
        {
            public void mouseMoved( MouseEvent evt )
            {
                // Column 1 is the Owner Column
                int col = usageTable.columnAtPoint( evt.getPoint() );
                if( col == 0 )
                {
                    setCursor( linkCursor );
                }
                else
                {
                    if( !getCursor().equals( defaultCursor ) )
                    {
                        setCursor( defaultCursor );
                    }
                }
            }

            public void mouseClicked( MouseEvent evt )
            {
                /*if( evt.getClickCount() < 2 )
                {
                    return;
                }*/

                int col = usageTable.columnAtPoint( evt.getPoint() );
                if( col != 0 )
                {
                    return;
                }

                int row = usageTable.rowAtPoint( evt.getPoint() );
                if( row < 0 )
                {
                    return;
                }

                linkObject = usageTableModel.getValueAt( row, col );
                linkActivated();
                linkObject = null;
            }
        };

        usageTable.addMouseMotionListener( mouseInputListener );
        usageTable.addMouseListener( mouseInputListener );
    }

    protected void linkActivated()
    {
        if( linkObject == null )
        {
            return;
        }
        LinkEvent linkEvt = new LinkEvent( this, linkObject );
        detailModelPane.fireLinkActivated( linkEvt );
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
        linkObject = null;
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
            collectInTransients( descriptor.composites() );
            collectInObjects( descriptor.objects() );
        }
    }


    private void collectInServices( Iterable<ServiceDetailDescriptor> iter )
    {
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

    private void collectInTransients( Iterable<CompositeDetailDescriptor> iter )
    {
        for( CompositeDetailDescriptor descriptor : iter )
        {
            collectInMixin( descriptor.mixins() );
        }
    }

    private void collectInObjects( Iterable<ObjectDetailDescriptor> iter )
    {
        for( ObjectDetailDescriptor descriptor : iter )
        {
            collectInInjectedField( descriptor.injectedFields(), descriptor );
        }
    }

    private void collectInMixin( Iterable<MixinDetailDescriptor> iter )
    {
        for( MixinDetailDescriptor descriptor : iter )
        {
            collectInInjectedField( descriptor.injectedFields(), descriptor );
        }
    }

    private void collectInInjectedField( Iterable<InjectedFieldDetailDescriptor> iter, Object ownerDescriptor )
    {
        for( InjectedFieldDetailDescriptor descriptorField : iter )
        {
            DependencyDescriptor dependencyDescriptor = descriptorField.descriptor().dependency();
            Annotation annotation = dependencyDescriptor.injectionAnnotation();

            Class<? extends Annotation> clazz = annotation.annotationType();
            if( Uses.class.equals( clazz ) || Service.class.equals( clazz ) )
            {
                boolean used = false;
                if( dependencyDescriptor.injectionClass().equals( this.descriptor.descriptor().type() ) )
                {
                    used = true;
                }
                else
                {
                    // collect in injectedServices
                    for( String name : dependencyDescriptor.injectedServices() )
                    {
                        if( name.equals( this.descriptor.descriptor().identity() ) )
                        {
                            used = true;
                        }
                    }
                }

                if( used )
                {
                    TableData rowData = new TableData( 5 );
                    if( ownerDescriptor instanceof MixinDetailDescriptor )
                    {
                        MixinDetailDescriptor mixinDescriptor = (MixinDetailDescriptor) ownerDescriptor;
                        rowData.set( 0, mixinDescriptor.composite() );
                        rowData.set( 1, descriptorField );
                        rowData.set( 2, mixinDescriptor.composite().module() );
                        rowData.set( 3, mixinDescriptor.composite().module().layer() );
                    }
                    else
                    {
                        // assume ObjectDetailDescriptor
                        ObjectDetailDescriptor objectDescriptor = (ObjectDetailDescriptor) ownerDescriptor;
                        rowData.set( 0, objectDescriptor );
                        rowData.set( 1, descriptorField );
                        rowData.set( 2, objectDescriptor.module() );
                        rowData.set( 3, objectDescriptor.module().layer() );
                    }
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
        protected String[] columnNames = { bundle.getString( "Owner.Column" ), bundle.getString( "Usage.Column" ), bundle.getString( "Module.Column" ), bundle.getString( "Layer.Column" ) };
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

    public class OwnerCellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public final Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
        {
            if( value != null )
            {
                CompositeDetailDescriptor descriptor = (CompositeDetailDescriptor) value;
                value = "<html><a href=\"" + descriptor.toString() + "\">" + descriptor.toString() + "</a></html>";
            }

            super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

            return this;
        }
    }

    public class FieldCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public final Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
        {
            if( value != null )
            {
                InjectedFieldDetailDescriptor descriptor = (InjectedFieldDetailDescriptor) value;
                DependencyDescriptor dependencyDescriptor = descriptor.descriptor().dependency();
                Annotation annotation = dependencyDescriptor.injectionAnnotation();
                value = descriptor.toString() + " (@" + annotation.annotationType().getSimpleName() + ")";
            }

            return super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
        }
    }

}
