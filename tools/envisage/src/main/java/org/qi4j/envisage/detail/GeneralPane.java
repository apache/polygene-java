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
package org.qi4j.envisage.detail;

import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.tools.model.descriptor.*;
import org.qi4j.envisage.util.TableRow;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static org.qi4j.functional.Iterables.first;

/**
 * Implementation of General DetailPane
 */
public class GeneralPane
    extends DetailPane
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private JPanel contentPane;
    private JTable table;
    private GeneralTableModel tableModel;

    public GeneralPane( DetailModelPane detailModelPane )
    {
        super( detailModelPane );
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        tableModel = new GeneralTableModel();
        table.setModel( tableModel );

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn( 0 ).setPreferredWidth( 90 );
        columnModel.getColumn( 1 ).setPreferredWidth( 550 );
    }

    protected void clear()
    {
        tableModel.clear();
    }

    public void setDescriptor( Object objectDesciptor )
    {
        clear();
        tableModel.reload( objectDesciptor );
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
        contentPane = new JPanel();
        contentPane.setLayout( new BorderLayout( 0, 0 ) );
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add( scrollPane1, BorderLayout.CENTER );
        table = new JTable();
        scrollPane1.setViewportView( table );
    }

    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

    public class GeneralTableModel
        extends AbstractTableModel
    {
        /**
         * the column names for this model
         */
        protected String[] columnNames = { bundle.getString( "Name.Column" ), bundle.getString( "Value.Column" ) };
        protected ArrayList<TableRow> rows;

        protected String nameRow = "name";
        protected String classRow = "class";
        protected String visibilityRow = "visibility";
        protected String moduleRow = "module";
        protected String layerRow = "layer";

        public GeneralTableModel()
        {
            rows = new ArrayList<TableRow>();
        }

        public void reload( Object objectDesciptor )
        {
            if( objectDesciptor instanceof ServiceDetailDescriptor )
            {
                ServiceDescriptor descriptor = ( (ServiceDetailDescriptor) objectDesciptor ).descriptor();
                Class<?> type = first( descriptor.types() );
                rows.add( new TableRow( 2, new Object[]{ nameRow, type.getSimpleName() } ) );
                rows.add( new TableRow( 2, new Object[]{ classRow, type.getName() } ) );
                rows.add( new TableRow( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
                rows.add( new TableRow( 2, new Object[]{ moduleRow, ( (ServiceDetailDescriptor) objectDesciptor ).module() } ) );
                rows.add( new TableRow( 2, new Object[]{ layerRow, ( (ServiceDetailDescriptor) objectDesciptor ).module().layer() } ) );
                rows.add( new TableRow( 2, new Object[]{ "startup", ( (ServiceDetailDescriptor) objectDesciptor ).descriptor().isInstantiateOnStartup() } ) );
            }
            else if( objectDesciptor instanceof ImportedServiceDetailDescriptor )
            {
                ImportedServiceCompositeDescriptor descriptor = ( (ImportedServiceDetailDescriptor) objectDesciptor ).descriptor();
                rows.add( new TableRow( 2, new Object[]{ nameRow, descriptor.primaryType().getSimpleName() } ) );
                rows.add( new TableRow( 2, new Object[]{ classRow, descriptor.primaryType().getName() } ) );
                rows.add( new TableRow( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
                rows.add( new TableRow( 2, new Object[]{ moduleRow, ( (ImportedServiceDetailDescriptor) objectDesciptor ).module() } ) );
                rows.add( new TableRow( 2, new Object[]{
                    layerRow, ( (ImportedServiceDetailDescriptor) objectDesciptor ).module().layer()
                } ) );
            }
            else if( objectDesciptor instanceof EntityDetailDescriptor )
            {
                EntityDescriptor descriptor = ( (EntityDetailDescriptor) objectDesciptor ).descriptor();
                Class<?> type = first( descriptor.types() );
                rows.add( new TableRow( 2, new Object[]{ nameRow, type.getSimpleName() } ) );
                rows.add( new TableRow( 2, new Object[]{ classRow, type.getName() } ) );
                rows.add( new TableRow( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
                rows.add( new TableRow( 2, new Object[]{ moduleRow, ( (EntityDetailDescriptor) objectDesciptor ).module() } ) );
                rows.add( new TableRow( 2, new Object[]{ layerRow, ( (EntityDetailDescriptor) objectDesciptor ).module().layer() } ) );
            }
            else if( objectDesciptor instanceof ValueDetailDescriptor )
            {
                ValueDescriptor descriptor = ( (ValueDetailDescriptor) objectDesciptor ).descriptor();
                Class<?> type = first( descriptor.types() );
                rows.add( new TableRow( 2, new Object[]{ nameRow, type.getSimpleName() } ) );
                rows.add( new TableRow( 2, new Object[]{ classRow, type.getName() } ) );
                rows.add( new TableRow( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
                rows.add( new TableRow( 2, new Object[]{ moduleRow, ( (ValueDetailDescriptor) objectDesciptor ).module() } ) );
                rows.add( new TableRow( 2, new Object[]{ layerRow, ( (ValueDetailDescriptor) objectDesciptor ).module().layer() } ) );
            }
            else if( objectDesciptor instanceof ObjectDetailDescriptor )
            {
                ObjectDescriptor descriptor = ( (ObjectDetailDescriptor) objectDesciptor ).descriptor();
                Class<?> type = first( descriptor.types() );
                rows.add( new TableRow( 2, new Object[]{ nameRow, type.getSimpleName() } ) );
                rows.add( new TableRow( 2, new Object[]{ classRow, type.getName() } ) );
                rows.add( new TableRow( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
                rows.add( new TableRow( 2, new Object[]{ moduleRow, ( (ObjectDetailDescriptor) objectDesciptor ).module() } ) );
                rows.add( new TableRow( 2, new Object[]{ layerRow, ( (ObjectDetailDescriptor) objectDesciptor ).module().layer() } ) );
            }
            else if( objectDesciptor instanceof CompositeDetailDescriptor )
            {
                CompositeDescriptor descriptor = ( (CompositeDetailDescriptor) objectDesciptor ).descriptor();
                Class<?> type = first( descriptor.types() );
                rows.add( new TableRow( 2, new Object[]{ nameRow, type.getSimpleName() } ) );
                rows.add( new TableRow( 2, new Object[]{ classRow, type.getName() } ) );
                rows.add( new TableRow( 2, new Object[]{ visibilityRow, descriptor.visibility().toString() } ) );
                rows.add( new TableRow( 2, new Object[]{ moduleRow, ( (CompositeDetailDescriptor) objectDesciptor ).module() } ) );
                rows.add( new TableRow( 2, new Object[]{ layerRow, ( (CompositeDetailDescriptor) objectDesciptor ).module().layer() } ) );
            }

            fireTableDataChanged();
        }

        public Object getValueAt( int rowIndex, int columnIndex )
        {
            TableRow row = this.rows.get( rowIndex );
            return row.get( columnIndex );
        }

        public void clear()
        {
            rows.clear();
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
            return rows.size();
        }
    }
}

