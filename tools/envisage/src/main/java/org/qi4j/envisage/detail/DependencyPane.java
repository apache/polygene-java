/*
 * Copyright (c) 2009, Tony Kohar. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.envisage.detail;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.lang.annotation.Annotation;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import org.qi4j.api.composite.DependencyDescriptor;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.Classes;
import org.qi4j.envisage.event.LinkEvent;
import org.qi4j.tools.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.tools.model.descriptor.InjectedFieldDetailDescriptor;
import org.qi4j.tools.model.descriptor.MixinDetailDescriptor;
import org.qi4j.tools.model.descriptor.ObjectDetailDescriptor;

/* package */ final class DependencyPane
    extends DetailPane
{
    private JPanel contentPane;
    private JSplitPane splitPane;
    private JList fieldList;
    private JLabel classNameLabel;
    private JLabel optionalLabel;
    private JLabel annotationLabel;
    private JLabel injectionTypeLabel;
    private JList injectedServiceList;
    private JPanel detailPane;

    private final DefaultListModel fieldListModel;
    private DefaultListModel injectedServiceListModel;

    private Cursor defaultCursor;
    private Cursor linkCursor;

    /* package */ DependencyPane( DetailModelPane detailModelPane )
    {
        super( detailModelPane );
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        fieldListModel = new DefaultListModel();
        fieldList.setModel( fieldListModel );
        fieldList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        fieldList.setPrototypeCellValue( "123456789012345" );

        injectedServiceListModel = new DefaultListModel();
        injectedServiceList.setModel( injectedServiceListModel );
        injectedServiceList.setCellRenderer( new InjectedServiceListCellRenderer() );
        injectedServiceList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        injectedServiceList.setPrototypeCellValue( "123456789012345" );

        Dimension minSize = new Dimension( 20, 20 );
        detailPane.setMinimumSize( minSize );

        fieldList.addListSelectionListener( new ListSelectionListener()
        {
            @Override
            public void valueChanged( ListSelectionEvent evt )
            {
                fieldListValueChanged( evt );
            }
        } );

        defaultCursor = getCursor();
        linkCursor = LinkEvent.LINK_CURSOR;

        MouseInputAdapter mouseInputListener = new MouseInputAdapter()
        {
            @Override
            public void mouseMoved( MouseEvent evt )
            {
                int i = injectedServiceList.locationToIndex( evt.getPoint() );
                if( i != -1 )
                {
                    setCursor( linkCursor );
                }
                else
                {
                    setCursor( defaultCursor );
                }
            }

            @Override
            public void mouseExited( MouseEvent evt )
            {
                setCursor( defaultCursor );
            }

            @Override
            public void mouseClicked( MouseEvent evt )
            {
                /*if( evt.getClickCount() < 2 )
                 {
                 return;
                 }*/

                int i = injectedServiceList.locationToIndex( evt.getPoint() );
                if( i != -1 )
                {
                    Object linkObject = injectedServiceListModel.get( i );
                    linkActivated( linkObject );
                }
            }
        };

        injectedServiceList.addMouseListener( mouseInputListener );
        injectedServiceList.addMouseMotionListener( mouseInputListener );
    }

    private void linkActivated( Object linkObject )
    {
        if( linkObject == null )
        {
            return;
        }
        LinkEvent linkEvt = new LinkEvent( this, linkObject );
        detailModelPane.fireLinkActivated( linkEvt );
    }

    private void clear()
    {
        fieldListModel.clear();
        clearDetail();
    }

    private void clearDetail()
    {
        classNameLabel.setText( null );
        classNameLabel.setToolTipText( null );
        optionalLabel.setText( null );
        annotationLabel.setText( null );
        injectionTypeLabel.setText( null );
        injectionTypeLabel.setToolTipText( null );
        injectedServiceListModel.clear();
    }

    @Override
    protected void setDescriptor( Object objectDesciptor )
    {
        clear();

        if( objectDesciptor instanceof CompositeDetailDescriptor )
        {
            CompositeDetailDescriptor descriptor = ( (CompositeDetailDescriptor) objectDesciptor );
            Iterable<MixinDetailDescriptor> iter = descriptor.mixins();
            for( MixinDetailDescriptor mixinDescriptor : iter )
            {
                reload( mixinDescriptor.injectedFields() );
            }
        }
        else if( objectDesciptor instanceof ObjectDetailDescriptor )
        {
            ObjectDetailDescriptor descriptor = ( (ObjectDetailDescriptor) objectDesciptor );
            reload( descriptor.injectedFields() );
        }

        if( !fieldListModel.isEmpty() )
        {
            fieldList.setSelectedIndex( 0 );
        }
    }

    private void reload( Iterable<InjectedFieldDetailDescriptor> iter )
    {
        for( InjectedFieldDetailDescriptor descriptor : iter )
        {
            DependencyDescriptor dependencyDescriptor = descriptor.descriptor().dependency();
            Annotation annotation = dependencyDescriptor.injectionAnnotation();

            if( isDependencyField( annotation.annotationType() ) )
            {
                fieldListModel.addElement( descriptor );
            }
        }
    }

    private void reloadDetail( InjectedFieldDetailDescriptor descriptor )
    {
        clearDetail();
        if( descriptor == null )
        {
            return;
        }

        DependencyDescriptor dependencyDescriptor = descriptor.descriptor().dependency();

        classNameLabel.setText( dependencyDescriptor.injectedClass().getSimpleName() );
        classNameLabel.setToolTipText( dependencyDescriptor.injectedClass().getName() );
        annotationLabel.setText( "@" + dependencyDescriptor.injectionAnnotation().annotationType().getSimpleName() );
        optionalLabel.setText( Boolean.toString( dependencyDescriptor.optional() ) );
        injectionTypeLabel.setText( Classes.simpleGenericNameOf( dependencyDescriptor.injectionType() ) );
        injectionTypeLabel.setToolTipText( dependencyDescriptor.injectionType().toString() );
    }

    /**
     * Check whether the type is a dependency field (Service and Use only)
     *
     * @param clazz the type
     *
     * @return true or false
     */
    private boolean isDependencyField( Class<? extends Annotation> clazz )
    {
        return Uses.class.equals( clazz ) || Service.class.equals( clazz );
    }

    private void fieldListValueChanged( ListSelectionEvent evt )
    {
        if( evt.getValueIsAdjusting() )
        {
            return;
        }

        reloadDetail( (InjectedFieldDetailDescriptor) fieldList.getSelectedValue() );
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
        splitPane = new JSplitPane();
        contentPane.add( splitPane, BorderLayout.CENTER );
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane.setLeftComponent( scrollPane1 );
        fieldList = new JList();
        scrollPane1.setViewportView( fieldList );
        detailPane = new JPanel();
        detailPane.setLayout( new GridBagLayout() );
        splitPane.setRightComponent( detailPane );
        detailPane.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ), null ) );
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$( label1, ResourceBundle.getBundle( "org/qi4j/envisage/detail/DependencyPane" ).getString( "CTL_ClassName.Text" ) );
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        detailPane.add( label1, gbc );
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        detailPane.add( spacer1, gbc );
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        detailPane.add( spacer2, gbc );
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$( label2, ResourceBundle.getBundle( "org/qi4j/envisage/detail/DependencyPane" ).getString( "CTL_Optional.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        detailPane.add( label2, gbc );
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$( label3, ResourceBundle.getBundle( "org/qi4j/envisage/detail/DependencyPane" ).getString( "CTL_Annotation.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        detailPane.add( label3, gbc );
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        detailPane.add( spacer3, gbc );
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.VERTICAL;
        detailPane.add( spacer4, gbc );
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.VERTICAL;
        detailPane.add( spacer5, gbc );
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$( label4, ResourceBundle.getBundle( "org/qi4j/envisage/detail/DependencyPane" ).getString( "CTL_InjectionType.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.EAST;
        detailPane.add( label4, gbc );
        classNameLabel = new JLabel();
        classNameLabel.setText( "none" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        detailPane.add( classNameLabel, gbc );
        annotationLabel = new JLabel();
        annotationLabel.setText( "none" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        detailPane.add( annotationLabel, gbc );
        injectionTypeLabel = new JLabel();
        injectionTypeLabel.setText( "none" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        detailPane.add( injectionTypeLabel, gbc );
        optionalLabel = new JLabel();
        optionalLabel.setText( "none" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        detailPane.add( optionalLabel, gbc );
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.VERTICAL;
        detailPane.add( spacer6, gbc );
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$( label5, ResourceBundle.getBundle( "org/qi4j/envisage/detail/DependencyPane" ).getString( "CTL_Injection.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        detailPane.add( label5, gbc );
        final JScrollPane scrollPane2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 12;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        detailPane.add( scrollPane2, gbc );
        injectedServiceList = new JList();
        scrollPane2.setViewportView( injectedServiceList );
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.VERTICAL;
        detailPane.add( spacer7, gbc );
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$( label6, ResourceBundle.getBundle( "org/qi4j/envisage/detail/DependencyPane" ).getString( "CTL_InjectedServices.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        detailPane.add( label6, gbc );
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        detailPane.add( separator1, gbc );
    }

    private void $$$loadLabelText$$$( JLabel component, String text )
    {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for( int i = 0; i < text.length(); i++ )
        {
            if( text.charAt( i ) == '&' )
            {
                i++;
                if( i == text.length() )
                {
                    break;
                }
                if( !haveMnemonic && text.charAt( i ) != '&' )
                {
                    haveMnemonic = true;
                    mnemonic = text.charAt( i );
                    mnemonicIndex = result.length();
                }
            }
            result.append( text.charAt( i ) );
        }
        component.setText( result.toString() );
        if( haveMnemonic )
        {
            component.setDisplayedMnemonic( mnemonic );
            component.setDisplayedMnemonicIndex( mnemonicIndex );
        }
    }

    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

    private static class InjectedServiceListCellRenderer
        extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean cellHasFocus
        )
        {
            if( value != null )
            {
                String id = (String) value;
                value = "<html><a href=\"" + id + "\">" + id + "</a></html>";
            }

            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

            return this;
        }
    }

}
