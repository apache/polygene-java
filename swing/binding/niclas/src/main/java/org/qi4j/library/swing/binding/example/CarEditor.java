/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.swing.binding.example;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.swing.binding.StateModel;
import org.qi4j.library.swing.binding.bindings.TableBinder;
import org.qi4j.library.swing.binding.bindings.TextFieldBinder;

public class CarEditor extends JFrame
{
    private static SingletonAssembler app;
    private static CarEditor carEditor;

    public CarEditor( @Structure ObjectBuilderFactory obf )
        throws HeadlessException
    {
        ObjectBuilder<StateModel> builder = obf.newObjectBuilder( StateModel.class );
        builder.use( Car.class );
        StateModel<Car> carModel = builder.newInstance();
        add( new CarEditPanel( carModel ) );
        pack();
        setVisible( true );
    }

    public class CarListPanel extends JPanel
    {
        public CarListPanel( StateModel<Cars> carsModel )
        {
            createCarsTable( carsModel );
        }

        private JTable createCarsTable( StateModel<Cars> carsModel )
        {
            JTable carsTable = new JTable();
            carsTable.setName( "manufacturer" );
            Cars template = carsModel.state();
            carsModel.bind( template.carCollection() ).using( TableBinder.class ).to( carsTable );
            return carsTable;
        }
    }

    public class CarEditPanel extends JPanel
    {
        public CarEditPanel( StateModel<Car> carModel )
        {
            LayoutManager layout = new BoxLayout( this, BoxLayout.Y_AXIS );
            setLayout( layout );

            JPanel manufacturerPanel = new JPanel();
            manufacturerPanel.setLayout( new BoxLayout( manufacturerPanel, BoxLayout.X_AXIS ) );
            JLabel manufacturerLabel = new JLabel( "Manufacturer:" );
            manufacturerLabel.setAlignmentX( -1.0f );
            setSizes( manufacturerLabel );
            manufacturerPanel.add( manufacturerLabel );
            manufacturerPanel.add( createManufacturerField( carModel ) );

            JPanel modelPanel = new JPanel();
            modelPanel.setLayout( new BoxLayout( modelPanel, BoxLayout.X_AXIS ) );
            JLabel modelLabel = new JLabel( "Model:" );
            modelLabel.setAlignmentX( -1.0f );
            setSizes( modelLabel );
            modelPanel.add( modelLabel );
            modelPanel.add( createModelField( carModel ) );
            add( manufacturerPanel );
            add( modelPanel );
        }

        private JTextField createManufacturerField( StateModel<Car> carModel )
        {
            JTextField manufacturerField = new JTextField();
            setSizes( manufacturerField );
            manufacturerField.setName( "manufacturer" );
            Car template = carModel.state();
            carModel.bind( template.manufacturer() ).using( TextFieldBinder.class ).to( manufacturerField );
            return manufacturerField;
        }

        private JTextField createModelField( StateModel<Car> carModel )
        {
            JTextField modelField = new JTextField();
            setSizes( modelField );
            modelField.setName( "model" );
            Car template = carModel.state();
            carModel.bind( template.model() ).using( TextFieldBinder.class ).to( modelField );
            return modelField;
        }

        private void setSizes( JTextField tf )
        {
            Dimension pref = tf.getPreferredSize();
            tf.setPreferredSize( new Dimension( 100, pref.height ) );
            Dimension max = tf.getMaximumSize();
            tf.setMaximumSize( new Dimension( max.width, pref.height ) );
        }

        private void setSizes( JLabel label )
        {
            Dimension pref = label.getPreferredSize();
            label.setPreferredSize( new Dimension( 100, pref.height ) );
            Dimension max = label.getMaximumSize();
            label.setMaximumSize( new Dimension( max.width, pref.height ) );
        }
    }

    public interface Cars
    {
        ManyAssociation<Car> carCollection();
    }

    public interface Car
    {
        Property<String> manufacturer();

        Property<String> model();
    }

    public interface CarEntity extends Car, EntityComposite
    {
    }

    public static void main( String[] args )
        throws Exception
    {
        app = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addEntities( CarEntity.class );
                module.addObjects( CarEditor.class );
            }
        };
        app.application().activate();
        ObjectBuilderFactory objectBuilderFactory = app.module().objectBuilderFactory();
        carEditor = objectBuilderFactory.newObject( CarEditor.class );
    }
}
