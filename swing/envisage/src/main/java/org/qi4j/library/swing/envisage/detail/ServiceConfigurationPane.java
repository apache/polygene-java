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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.qi4j.library.swing.envisage.event.LinkEvent;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ValueDetailDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;

/**
 * Implementation of Service Configuration Panel
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class ServiceConfigurationPane extends DetailPane
{
    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private JPanel contentPane;
    private JLabel nameLabel;
    private JLabel classLabel;
    private JButton linkButton;
    private JLabel typeLabel;

    private Object configDesciptor;

    public ServiceConfigurationPane( DetailModelPane detailModelPane )
    {
        super( detailModelPane );
        this.setLayout( new BorderLayout() );
        this.add( contentPane, BorderLayout.CENTER );

        linkButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent evt )
            {
                linkActivated();
            }
        } );

        nameLabel.addMouseListener( new MouseAdapter()
        {
            public void mouseClicked( MouseEvent evt )
            {
                linkActivated();
            }
        } );
    }

    protected void linkActivated()
    {
        if( configDesciptor == null )
        {
            return;
        }
        LinkEvent linkEvt = new LinkEvent( this, configDesciptor );
        detailModelPane.fireLinkActivated( linkEvt );
    }

    protected void clear()
    {
        nameLabel.setText( null );
        classLabel.setText( null );
        typeLabel.setText( null );
        linkButton.setEnabled( false );
        configDesciptor = null;
    }

    public void setDescriptor( Object objectDesciptor )
    {
        clear();

        if( !( objectDesciptor instanceof ServiceDetailDescriptor ) )
        {
            return;
        }

        ServiceDetailDescriptor descriptor = (ServiceDetailDescriptor) objectDesciptor;
        Class<?> configType = descriptor.descriptor().configurationType();

        if( configType == null )
        {
            return;
        }

        // traverse the appDescritor to find the configurationDescriptor
        ApplicationDetailDescriptor appDescriptor = descriptor.module().layer().application();
        Object obj = findConfigurationDescriptor( appDescriptor, configType );
        if( obj == null )
        {
            return;
        }

        configDesciptor = obj;
        ObjectDescriptor spiDescriptor = null;
        String typeString = null;
        if( obj instanceof ServiceDetailDescriptor )
        {
            spiDescriptor = ( (ServiceDetailDescriptor) obj ).descriptor();
            typeString = "Service";
        }
        else if( obj instanceof EntityDetailDescriptor )
        {
            spiDescriptor = ( (EntityDetailDescriptor) obj ).descriptor();
            typeString = "Entity";
        }
        /*
            TODO TransientDescriptor
         */
        else if( obj instanceof ValueDetailDescriptor )
        {
            spiDescriptor = ( (ValueDetailDescriptor) obj ).descriptor();
            typeString = "Value";
        }
        else if( obj instanceof ObjectDetailDescriptor )
        {
            spiDescriptor = ( (ObjectDetailDescriptor) obj ).descriptor();
            typeString = "Object";
        }

        String simpleName = spiDescriptor.type().getSimpleName();
        nameLabel.setText( "<html><a href='" + simpleName + "'>" + simpleName + "</a></html>" );
        classLabel.setText( spiDescriptor.type().getName() );
        typeLabel.setText( typeString );
        linkButton.setEnabled( true );
    }

    private Object findConfigurationDescriptor( ApplicationDetailDescriptor descriptor, Class<?> configType )
    {
        Object configDescriptor = null;
        for( LayerDetailDescriptor childDescriptor : descriptor.layers() )
        {
            Object obj = findInModules( childDescriptor.modules(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }
        }

        return configDescriptor;
    }

    private Object findInModules( Iterable<ModuleDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( ModuleDetailDescriptor descriptor : iter )
        {
            Object obj = findInServices( descriptor.services(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            obj = findInEntities( descriptor.entities(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            obj = findInValues( descriptor.values(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            // TODO findInTransients

            obj = findInObjects( descriptor.objects(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }
        }

        return configDescriptor;
    }


    private Object findInServices( Iterable<ServiceDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( ServiceDetailDescriptor descriptor : iter )
        {
            if( configType.equals( descriptor.descriptor().type() ) )
            {
                configDescriptor = descriptor;
                break;
            }
        }

        return configDescriptor;
    }

    private Object findInEntities( Iterable<EntityDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( EntityDetailDescriptor descriptor : iter )
        {
            if( configType.equals( descriptor.descriptor().type() ) )
            {
                configDescriptor = descriptor;
                break;
            }
        }


        return configDescriptor;
    }

    private Object findInValues( Iterable<ValueDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( ValueDetailDescriptor descriptor : iter )
        {
            if( configType.equals( descriptor.descriptor().type() ) )
            {
                configDescriptor = descriptor;
                break;
            }
        }


        return configDescriptor;
    }

    private Object findInObjects( Iterable<ObjectDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( ObjectDetailDescriptor descriptor : iter )
        {
            if( configType.equals( descriptor.descriptor().type() ) )
            {
                configDescriptor = descriptor;
                break;
            }
        }


        return configDescriptor;
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
        contentPane.setLayout( new GridBagLayout() );
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$( label1, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/ServiceConfigurationPane" ).getString( "CTL_Name.Text" ) );
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add( label1, gbc );
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add( spacer1, gbc );
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        contentPane.add( spacer2, gbc );
        nameLabel = new JLabel();
        nameLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add( nameLabel, gbc );
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        contentPane.add( spacer3, gbc );
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$( label2, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/ServiceConfigurationPane" ).getString( "CTL_Class.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add( label2, gbc );
        classLabel = new JLabel();
        classLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add( classLabel, gbc );
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        contentPane.add( spacer4, gbc );
        linkButton = new JButton();
        this.$$$loadButtonText$$$( linkButton, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/ServiceConfigurationPane" ).getString( "CTL_Link.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add( linkButton, gbc );
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        contentPane.add( spacer5, gbc );
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$( label3, ResourceBundle.getBundle( "org/qi4j/library/swing/envisage/detail/ServiceConfigurationPane" ).getString( "CTL_Type.Text" ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add( label3, gbc );
        typeLabel = new JLabel();
        typeLabel.setText( "Label" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add( typeLabel, gbc );
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.VERTICAL;
        contentPane.add( spacer6, gbc );
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
    private void $$$loadButtonText$$$( AbstractButton component, String text )
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
            component.setMnemonic( mnemonic );
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
