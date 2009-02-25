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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.library.swing.envisage.model.descriptor.CompositeMethodDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MethodConcernDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MethodSideEffectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.util.TableData;

/**
 * Implementation of Composite State Panel
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class StatePane extends DetailPane
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private JPanel contentPane;
    private JList methodList;
    private JSplitPane splitPane;
    private JTable methodDetailTable;

    private DefaultListModel methodListModel;
    private MethodDetailTableModel methodDetailTableModel;

    public StatePane()
    {
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        methodListModel = new DefaultListModel();
        methodList.setModel( methodListModel );
        methodList.setCellRenderer( new MethodListCellRenderer() );
        methodList.setPrototypeCellValue( "12345678901234567890" );
        methodList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        methodDetailTableModel = new MethodDetailTableModel();
        methodDetailTable.setModel( methodDetailTableModel );

        TableColumnModel columnModel = methodDetailTable.getColumnModel();
        columnModel.getColumn( 0 ).setPreferredWidth( 75 );
        columnModel.getColumn( 1 ).setPreferredWidth( 400 );

        //splitPane.setDividerLocation( .1 );

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

        // TODO for other type wait until QI-195 solved
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
            // Object does not have state
            return;
        }
    }

    private void reload( Iterable<CompositeMethodDetailDescriptor> iter )
    {
        List<CompositeMethodDetailDescriptor> publicList = new ArrayList<CompositeMethodDetailDescriptor>();
        List<CompositeMethodDetailDescriptor> privateList = new ArrayList<CompositeMethodDetailDescriptor>();

        for( CompositeMethodDetailDescriptor descriptor : iter )
        {
            Class compositeClass = descriptor.composite().descriptor().type();
            Class mixinMethodClass = descriptor.descriptor().method().getDeclaringClass();
            if( mixinMethodClass.isAssignableFrom( compositeClass ) )
            {
                publicList.add( descriptor );
            }
            else
            {
                privateList.add( descriptor );
            }
        }

        // filter Property, Association, and ManyAssociation
        doFilter( publicList );
        doFilter( privateList );

        // list public first, then private
        for( CompositeMethodDetailDescriptor descriptor : publicList )
        {
            methodListModel.addElement( descriptor );
        }

        for( CompositeMethodDetailDescriptor descriptor : privateList )
        {
            methodListModel.addElement( descriptor );
        }

        if( !methodListModel.isEmpty() )
        {
            methodList.setSelectedIndex( 0 );
        }

    }

    protected void clear()
    {
        methodListModel.clear();
        methodDetailTableModel.clear();
    }

    /**
     * Do the filter for method return type (Property, Association, ManyAssociation)
     * by removing the entry from the list if not the above.
     *
     * @param list list of CompositeMethodDetailDescriptor
     */
    private void doFilter( List<CompositeMethodDetailDescriptor> list )
    {
        if( list.isEmpty() )
        {
            return;
        }

        Iterator<CompositeMethodDetailDescriptor> iter = list.iterator();
        while( iter.hasNext() )
        {
            CompositeMethodDetailDescriptor descriptor = iter.next();
            Method method = descriptor.descriptor().method();
            if( Property.class.isAssignableFrom( method.getReturnType() )
                || Association.class.isAssignableFrom( method.getReturnType() )
                || ManyAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                continue;
            }
            iter.remove();
        }
    }

    public void methodListValueChanged( ListSelectionEvent evt )
    {
        if( evt.getValueIsAdjusting() )
        {
            return;
        }
        Object obj = methodList.getSelectedValue();
        if( obj == null )
        {
            methodDetailTableModel.clear();
            return;
        }
        methodDetailTableModel.reload( (CompositeMethodDetailDescriptor) obj );
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
        contentPane.add( splitPane, BorderLayout.CENTER );
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane.setLeftComponent( scrollPane1 );
        methodList = new JList();
        scrollPane1.setViewportView( methodList );
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPane.setRightComponent( scrollPane2 );
        methodDetailTable = new JTable();
        scrollPane2.setViewportView( methodDetailTable );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

    class MethodDetailTableModel extends AbstractTableModel
    {
        /**
         * the column names for this model
         */
        //protected String[] columnNames = { bundle.getString( "Name.Column" ), bundle.getString( "Value.Column" ) };
        protected String[] columnNames = { "Name", "Value" };
        protected ArrayList<TableData> data;

        public MethodDetailTableModel()
        {
            data = new ArrayList<TableData>();
        }

        public void reload( CompositeMethodDetailDescriptor descriptor )
        {
            clear();

            // mixin type
            data.add( new TableData( 2, new Object[]{ "mixin", descriptor.descriptor().mixin().mixinClass() } ) );
            data.add( new TableData( 2, new Object[]{ "return", descriptor.descriptor().method().getGenericReturnType() } ) );

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

    class MethodListCellRenderer extends DefaultListCellRenderer
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
            Class compositeClass = descriptor.composite().descriptor().type();
            Class mixinMethodClass = descriptor.descriptor().method().getDeclaringClass();
            if( mixinMethodClass.isAssignableFrom( compositeClass ) )
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


            Method method = descriptor.descriptor().method();
            Class<?> methodReturnType = method.getReturnType();

            if( Property.class.isAssignableFrom( methodReturnType ) )
            {
                Type t = GenericPropertyInfo.getPropertyType( method );
                setText( method.getName() + ":" + ( (Class) t ).getSimpleName() );
            }
            else if( Association.class.isAssignableFrom( methodReturnType ) )
            {
                Type t = GenericAssociationInfo.getAssociationType( method );
                setText( method.getName() + "->" + ( (Class) t ).getSimpleName() );
            }
            else if( ManyAssociation.class.isAssignableFrom( methodReturnType ) )
            {
                Type t = GenericAssociationInfo.getAssociationType( method );
                setText( method.getName() + "=>" + ( (Class) t ).getSimpleName() );
            }

            return this;
        }
    }


}
