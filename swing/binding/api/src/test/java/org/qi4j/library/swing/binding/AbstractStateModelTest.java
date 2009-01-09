/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.library.swing.binding;

import java.awt.FlowLayout;
import static java.awt.FlowLayout.CENTER;
import static java.lang.System.setProperty;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.entity.association.Association;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.swing.binding.domain.Cat;
import org.qi4j.library.swing.binding.domain.Person;
import org.qi4j.library.swing.binding.domain.entities.CatEntity;
import org.qi4j.library.swing.binding.domain.entities.PersonEntity;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author edward.yakop@gmail.com
 */
public abstract class AbstractStateModelTest extends AbstractQi4jTest
{
    protected FrameFixture window;
    protected StateModel<Person> personModel;
    protected UnitOfWork uow;

    @Before
    @Override
    @SuppressWarnings( "unchecked" )
    public final void setUp()
        throws Exception
    {
        super.setUp();

        setProperty( "java.awt.headless", "true" );

        ObjectBuilder<StateModel> personModelBuilder = objectBuilderFactory.newObjectBuilder( StateModel.class );
        personModelBuilder.use( Person.class );
        personModel = personModelBuilder.newInstance();

        window = new FrameFixture( new PersonFrame( personModel ) );
        window.show();

        uow = unitOfWorkFactory.newUnitOfWork();
    }

    public final void assemble( ModuleAssembly aModuleAssembly )
        throws AssemblyException
    {
        aModuleAssembly.addAssembler( new SwingBindingAssembler() );

        aModuleAssembly.addEntities(
            PersonEntity.class,
            CatEntity.class
        );

        aModuleAssembly.addServices(
            UuidIdentityGeneratorService.class,
            MemoryEntityStoreService.class,
            TextFieldToCatAdapter.class
        );
    }

    @Override @After
    public final void tearDown()
        throws Exception
    {
        if( window != null )
        {
            window.cleanUp();
        }
        if( uow != null && uow.isOpen() )
        {
            uow.complete();
        }
        super.tearDown();
    }

    @Mixins( TextFieldToCatAdapter.TextFieldToCatAdapterMixin.class )
    private static interface TextFieldToCatAdapter extends SwingAdapter<Association<Cat>>, ServiceComposite
    {
        abstract class TextFieldToCatAdapterMixin
            implements TextFieldToCatAdapter
        {
            private static final long serialVersionUID = 1L;

            public final Set<Capabilities> canHandle()
            {
                HashSet<Capabilities> capabilitieses = new HashSet<Capabilities>();
                capabilitieses.add( new Capabilities( JTextField.class, Cat.class, false, true, false, false ) );
                return capabilitieses;
            }

            public void fromSwingToData( JComponent aComponent, Association<Cat> anAssociation )
            {
                if( anAssociation != null )
                {
                    Cat cat = anAssociation.get();

                    if( cat != null )
                    {
                        JTextField textField = (JTextField) aComponent;
                        String newCatName = textField.getText();

                        cat.name().set( newCatName );
                    }
                }
            }

            public void fromDataToSwing( JComponent aComponent, Association<Cat> anAssociation )
            {
                JTextField textField = (JTextField) aComponent;
                if( anAssociation != null )
                {
                    Cat cat = anAssociation.get();

                    String catName = null;
                    if( cat != null )
                    {
                        catName = cat.name().get();
                    }
                    textField.setText( catName );
                    textField.setEnabled( true );
                }
                else
                {
                    textField.setText( null );
                    textField.setEnabled( false );
                }
            }
        }
    }

    protected static class PersonFrame extends JFrame
    {
        protected PersonFrame( StateModel<Person> aPersonModel )
        {
            super();

            setLayout( new FlowLayout( CENTER ) );

            add( createLostFocusField() );
            add( createPersonNameField( aPersonModel ) );
            add( createCatNameField( aPersonModel ) );
        }

        private JTextField createLostFocusField()
        {
            JTextField lostFocusField = new JTextField();
            lostFocusField.setName( "lostFocus" );
            return lostFocusField;
        }

        private JTextField createPersonNameField( StateModel<Person> aPersonModel )
        {
            JTextField personNameField = new JTextField();
            personNameField.setName( "personName" );

            Person template = aPersonModel.state();
            aPersonModel.bind( template.name() ).to( personNameField );

            return personNameField;
        }

        private JTextField createCatNameField( StateModel<Person> aPersonModel )
        {
            JTextField catNameField = new JTextField();
            catNameField.setName( "catName" );

            Person template = aPersonModel.state();
            aPersonModel.bind( template.favoriteCat() ).to( catNameField );

            return catNameField;
        }
    }
}
