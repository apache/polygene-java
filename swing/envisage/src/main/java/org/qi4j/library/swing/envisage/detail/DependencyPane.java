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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.annotation.Annotation;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.InjectedFieldDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.MixinDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ValueDetailDescriptor;
import org.qi4j.spi.composite.DependencyDescriptor;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class DependencyPane extends DetailPane
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private JPanel contentPane;
    private JSplitPane splitPane;
    private JList fieldList;
    private JLabel classNameLabel;
    private JLabel optionalLabel;
    private JLabel annotationLabel;
    private JLabel injectionClassLabel;
    private JLabel injectionTypeLabel;
    private JLabel injectionTypeRawLabel;


    private DefaultListModel fieldListModel;

    public DependencyPane( DetailModelPane detailModelPane )
    {
        super( detailModelPane );
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        fieldListModel = new DefaultListModel();
        fieldList.setModel( fieldListModel );
        fieldList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        fieldList.setPrototypeCellValue( "12345678901234567890" );

        //splitPane.setResizeWeight( .1 );
        //splitPane.setDividerLocation( .3 );

        fieldList.addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged( ListSelectionEvent evt )
            {
                fieldListValueChanged( evt );
            }
        } );
    }

    protected void clear()
    {
        fieldListModel.clear();
        clearDetail();
    }

    protected void clearDetail()
    {
        classNameLabel.setText( null );
        optionalLabel.setText( null );
        annotationLabel.setText( null );
        injectionClassLabel.setText( null );
        injectionTypeLabel.setText( null );
        injectionTypeRawLabel.setText( null );
    }

    public void setDescriptor( Object objectDesciptor )
    {
        clear();

        if( objectDesciptor instanceof ServiceDetailDescriptor )
        {
            ServiceDetailDescriptor descriptor = ( (ServiceDetailDescriptor) objectDesciptor );
            Iterable<MixinDetailDescriptor> iter = descriptor.mixins();
            for( MixinDetailDescriptor mixinDescriptor : iter )
            {
                reload( mixinDescriptor.injectedFields() );
            }
        }
        else if( objectDesciptor instanceof EntityDetailDescriptor )
        {
            EntityDetailDescriptor descriptor = ( (EntityDetailDescriptor) objectDesciptor );
            Iterable<MixinDetailDescriptor> iter = descriptor.mixins();
            for( MixinDetailDescriptor mixinDescriptor : iter )
            {
                reload( mixinDescriptor.injectedFields() );
            }
        }
        else if( objectDesciptor instanceof ValueDetailDescriptor )
        {
            ValueDetailDescriptor descriptor = ( (ValueDetailDescriptor) objectDesciptor );
            Iterable<MixinDetailDescriptor> iter = descriptor.mixins();
            for( MixinDetailDescriptor mixinDescriptor : iter )
            {
                reload( mixinDescriptor.injectedFields() );
            }
        }
        /* TODO transient once available
         *
         */
        else if( objectDesciptor instanceof ObjectDetailDescriptor )
        {
            // Object does not have this info 

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
        classNameLabel.setText( dependencyDescriptor.injectedClass().getName() );
        annotationLabel.setText( dependencyDescriptor.injectionAnnotation().toString() );
        optionalLabel.setText( Boolean.toString( dependencyDescriptor.optional() ) );
        injectionClassLabel.setText( dependencyDescriptor.injectionClass().getName() );
        injectionTypeLabel.setText( dependencyDescriptor.injectionType().toString() );
        injectionTypeRawLabel.setText( dependencyDescriptor.rawInjectionType().getName() );
    }

    /**
     * Check whether the type is a dependency field (Service and Use only)
     *
     * @param clazz the type
     * @return true or false
     */
    private boolean isDependencyField( Class<? extends Annotation> clazz )
    {
        boolean b = false;

        if( Uses.class.equals( clazz ) || Service.class.equals( clazz ) )
        {
            b = true;
        }

        return b;
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
        fieldList = new JList();
        scrollPane1.setViewportView( fieldList );
        final JPanel panel1 = new JPanel();
        panel1.setLayout( new GridBagLayout() );
        splitPane.setRightComponent( panel1 );
        panel1.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ), null ) );
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$( label1, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/DependencyPane" ).getString( "CTL_ClassName.Text" ) );
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add( label1, gbc );
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add( spacer1, gbc );
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add( spacer2, gbc );
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$( label2, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/DependencyPane" ).getString( "CTL_Optional.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add( label2, gbc );
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$( label3, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/DependencyPane" ).getString( "CTL_Annotation.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add( label3, gbc );
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add( spacer3, gbc );
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add( spacer4, gbc );
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$( label4, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/DependencyPane" ).getString( "CTL_InjectionClass.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add( label4, gbc );
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add( spacer5, gbc );
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$( label5, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/DependencyPane" ).getString( "CTL_InjectionType.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add( label5, gbc );
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add( spacer6, gbc );
        classNameLabel = new JLabel();
        classNameLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add( classNameLabel, gbc );
        annotationLabel = new JLabel();
        annotationLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add( annotationLabel, gbc );
        injectionClassLabel = new JLabel();
        injectionClassLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add( injectionClassLabel, gbc );
        injectionTypeLabel = new JLabel();
        injectionTypeLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add( injectionTypeLabel, gbc );
        optionalLabel = new JLabel();
        optionalLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add( optionalLabel, gbc );
        injectionTypeRawLabel = new JLabel();
        injectionTypeRawLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 14;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add( injectionTypeRawLabel, gbc );
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$( label6, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/DependencyPane" ).getString( "CTL_InjectionTypeRaw.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add( label6, gbc );
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 15;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add( spacer7, gbc );
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add( spacer8, gbc );
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add( spacer9, gbc );
        final JLabel label7 = new JLabel();
        this.$$$loadLabelText$$$( label7, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/DependencyPane" ).getString( "CTL_Injection.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add( label7, gbc );
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add( separator1, gbc );
    }

    /**
     * @noinspection ALL
     */
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

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }
}

