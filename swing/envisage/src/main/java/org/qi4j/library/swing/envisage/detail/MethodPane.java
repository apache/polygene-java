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
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.CompositeMethodDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MixinDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MethodConcernDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MethodConcernsDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MethodSideEffectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.InjectedMethodDetailDescriptor;
import org.qi4j.library.swing.envisage.util.TableData;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.api.composite.Composite;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class MethodPane extends DetailPane
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private JPanel contentPane;
    private JList methodList;
    private JPanel infoPane;
    private JTable detailTable;
    private JSplitPane splitPane;
    private DetailTableModel detailTableModel;

    private DefaultListModel listModel;

    public MethodPane()
    {
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        listModel = new DefaultListModel();
        methodList.setModel( listModel );
        methodList.setCellRenderer( new MethodListCellRenderer() );
        methodList.setPrototypeCellValue( "12345678901234567890" );

        detailTableModel = new DetailTableModel();
        detailTable.setModel( detailTableModel );

        TableColumnModel columnModel = detailTable.getColumnModel();
        columnModel.getColumn( 0 ).setPreferredWidth( 30 );
        columnModel.getColumn( 1 ).setPreferredWidth( 400 );

        splitPane.setDividerLocation( .3 );

        methodList.addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged( ListSelectionEvent evt )
            {
                methodListValueChanged( evt );
            }
        } );
    }

    public void setDescriptor( Object objectDesciptor )
    {
        clear();

        // TODO
        if( objectDesciptor instanceof ServiceDetailDescriptor )
        {
            ServiceDetailDescriptor descriptor = ( (ServiceDetailDescriptor) objectDesciptor );
        }
        else if( objectDesciptor instanceof EntityDetailDescriptor )
        {
            EntityDetailDescriptor descriptor = ( (EntityDetailDescriptor) objectDesciptor );
            reload( descriptor.methods() );
        }
        else if( objectDesciptor instanceof ObjectDetailDescriptor )
        {
            ObjectDetailDescriptor descriptor = ( (ObjectDetailDescriptor) objectDesciptor );
            //reload( descriptor.injectedMethods() );
        }
    }

    private void reload( Iterable<CompositeMethodDetailDescriptor> iter )
    {
        List<CompositeMethodDetailDescriptor> publicList = new ArrayList<CompositeMethodDetailDescriptor>();
        List<CompositeMethodDetailDescriptor> privateList = new ArrayList<CompositeMethodDetailDescriptor>();

        for( CompositeMethodDetailDescriptor descriptor : iter )
        {
            /* TODO, The Methods tab should show all the methods of all Mixins (private and public separated)
             * that don't return one of Property, Association or ManyAssociation.
             *
             * "private" and "public" refers to if the interface they are declared in is extended by the Composite.
             * If yes, then it is a public method, meaning, clients can call it.
             * If no, then it is a private mixin type, and can only be used internally through @This injections. 
             */
            Class clazz = descriptor.descriptor().method().getDeclaringClass();
            if( Composite.class.isAssignableFrom( clazz ) )
            {
                publicList.add( descriptor );
            }
            else
            {
                privateList.add( descriptor );
            }
        }

        // list public first, then private
        for( CompositeMethodDetailDescriptor descriptor : publicList )
        {
            listModel.addElement( descriptor );
        }

        for( CompositeMethodDetailDescriptor descriptor : privateList )
        {
            listModel.addElement( descriptor );
        }

        if( !listModel.isEmpty() )
        {
            methodList.setSelectedIndex( 0 );
        }
    }


    protected void clear()
    {
        listModel.clear();
        detailTableModel.clear();
    }

    protected void methodListValueChanged( ListSelectionEvent evt )
    {
        if( evt.getValueIsAdjusting() )
        {
            return;
        }
        Object obj = methodList.getSelectedValue();
        if( obj == null )
        {
            detailTableModel.clear();
            return;
        }
        detailTableModel.reload( (CompositeMethodDetailDescriptor) obj );
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
        splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable( true );
        contentPane.add( splitPane, BorderLayout.CENTER );
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane.setLeftComponent( scrollPane1 );
        methodList = new JList();
        scrollPane1.setViewportView( methodList );
        infoPane = new JPanel();
        infoPane.setLayout( new BorderLayout( 0, 0 ) );
        splitPane.setRightComponent( infoPane );
        final JScrollPane scrollPane2 = new JScrollPane();
        infoPane.add( scrollPane2, BorderLayout.CENTER );
        detailTable = new JTable();
        scrollPane2.setViewportView( detailTable );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

    public class DetailTableModel extends AbstractTableModel
    {
        /**
         * the column names for this model
         */
        //protected String[] columnNames = { bundle.getString( "Name.Column" ), bundle.getString( "Value.Column" ) };
        protected String[] columnNames = { "Name", "Value" };
        protected ArrayList<TableData> data;

        public DetailTableModel()
        {
            data = new ArrayList<TableData>();
        }

        public void reload( CompositeMethodDetailDescriptor descriptor )
        {
            clear();

            // mixin type
            data.add( new TableData( 2, new Object[]{ "class", descriptor.descriptor().mixin().mixinClass() } ) );

            // TODO constraint

            // concern
            boolean first = true;
            for( MethodConcernDetailDescriptor concern : descriptor.concerns().concerns() )
            {
                if( first )
                {
                    data.add( new TableData( 2, new Object[]{ "concern", concern.toString() } ) );
                    first = false;
                }
                else
                {
                    data.add( new TableData( 2, new Object[]{ "", concern.toString() } ) );
                }
            }

            // sideEffect
            first = false;
            for( MethodSideEffectDetailDescriptor sideEffect : descriptor.sideEffects().sideEffects() )
            {
                if( first )
                {
                    data.add( new TableData( 2, new Object[]{ "sideEffect", sideEffect.toString() } ) );
                    first = false;
                }
                else
                {
                    data.add( new TableData( 2, new Object[]{ "", sideEffect.toString() } ) );
                }
            }

            fireTableDataChanged();
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

    public class MethodListCellRenderer extends DefaultListCellRenderer
    {
        protected Icon publicIcon;
        protected Icon privateIcon;

        public MethodListCellRenderer()
        {
            try
            {
                publicIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Public" ) ) );
                privateIcon = new ImageIcon( getClass().getResource( bundle.getString( "ICON_Private" ) ) );

            }
            catch( Exception ex )
            {
                throw new RuntimeException( ex );
            }
        }

        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
        {
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            if( !( value instanceof CompositeMethodDetailDescriptor ) )
            {
                return this;
            }

            Icon icon = null;
            CompositeMethodDetailDescriptor descriptor = (CompositeMethodDetailDescriptor) value;
            Class clazz = descriptor.descriptor().method().getDeclaringClass();
            if( Composite.class.isAssignableFrom( clazz ) )
            {
                icon = publicIcon;
            }
            else
            {
                icon = privateIcon;
            }

            if( icon != null )
            {
                setIcon( icon );
            }

            return this;
        }
    }
}
