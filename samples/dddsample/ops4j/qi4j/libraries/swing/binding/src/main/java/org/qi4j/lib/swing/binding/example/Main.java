/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.lib.swing.binding.example;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.SwingBindingAssembler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class Main
{

    public static void main( String[] args )
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addComposites( BoundPersonComposite.class );
                module.addComposites( AddressComposite.class );
                module.addAssembler( new SwingBindingAssembler() );
            }
        };

        CompositeBuilderFactory cbf = assembler.getCompositeBuilderFactory();
        Address address1 = cbf.newComposite( Address.class );
        Address address2 = cbf.newComposite( Address.class );

        CompositeBuilder<BoundPersonComposite> builder = cbf.newCompositeBuilder( BoundPersonComposite.class );
        BoundPersonComposite prototype = builder.stateOfComposite();
        prototype.address().set( address1 );

        prototype.firstName().set( "Niclas" );
        prototype.address().get().line1().set( "Vista Damai" );
        prototype.address().get().line2().set( "Jalan Tun Razak" );
        final BoundPersonComposite p1 = builder.newInstance();

        prototype.address().set( address2 );
        prototype.firstName().set( "Edward" );
        prototype.address().get().line1().set( "Mutiara" );
        prototype.address().get().line2().set( "Jalan Ceylon" );
        final BoundPersonComposite p2 = builder.newInstance();

        ObjectBuilderFactory builderFactory = assembler.getObjectBuilderFactory();
        ObjectBuilder<StateModel> objectBuilder = builderFactory.newObjectBuilder( StateModel.class );
        objectBuilder.use( BoundPersonComposite.class );
        final StateModel<BoundPersonComposite> model = objectBuilder.newInstance();
        Form form = new Form( model );

        JFrame frame = new JFrame( "testing" );

        frame.add( form, BorderLayout.CENTER );
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setFocusable( true );
        checkBoxPanel.addFocusListener( new FocusListener()
        {

            public void focusGained( FocusEvent e )
            {
                System.out.println( "Gained!" );
            }

            public void focusLost( FocusEvent e )
            {
                System.out.println( "LOst!" );
            }
        } );
        JCheckBox sw = new JCheckBox( "Toggle" );
        sw.addActionListener( new ActionListener()
        {

            public void actionPerformed( ActionEvent e )
            {
                if( ((JCheckBox) e.getSource()).isSelected() )
                {
                    model.use( p1 );
                    System.out.println( p1.firstName().get() );
                }
                else
                {
                    model.use( p2 );
                    System.out.println( p2.firstName().get() );
                }
            }
        }
        );
        checkBoxPanel.add( sw );
        frame.add( checkBoxPanel, BorderLayout.EAST );
        frame.pack();
        System.out.println( model );
        frame.setVisible( true );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }
}
