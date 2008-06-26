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
package org.qi4j.lib.swing.binding;

import javax.swing.JFrame;
import javax.swing.JTextField;
import static junit.framework.Assert.assertEquals;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.lib.swing.binding.domain.Person;
import org.qi4j.lib.swing.binding.domain.PersonComposite;
import org.qi4j.object.ObjectBuilder;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author edward.yakop@gmail.com
 */
public class StateModelPropertyTest extends AbstractQi4jTest
{
    private FrameFixture window;
    private StateModel<Person> personModel;


    @Test
    public final void testNameBinding()
    {
        JTextComponentFixture nameTextBox = window.textBox( "name" );
        assertEquals( "", nameTextBox.text() );

        Person person = compositeBuilderFactory.newComposite( Person.class );
        personModel.use( person );

        assertEquals( "", nameTextBox.text() );

        // Update name by using binding
        SwingBinding<String> nameBinding = personModel.bind( personModel.state().name() );
        String expectedName = "Bar";
        nameBinding.stateModel().use( expectedName );

        assertEquals( expectedName, person.name().get() );
        assertEquals( expectedName, nameTextBox.text() );

        // Update name by using UI
        String expectedName2 = "Foo " + expectedName;
        nameTextBox.enterText( "Foo " );

        JTextComponentFixture lostFocusField = window.textBox( "lostFocus" );
        lostFocusField.focus();

        assertEquals( expectedName2, person.name().get() );
        assertEquals( expectedName2, nameTextBox.text() );
    }

    @Before
    @Override
    @SuppressWarnings( "unchecked" )
    public void setUp()
        throws Exception
    {
        super.setUp();

        ObjectBuilder<StateModel> personModelBuilder = objectBuilderFactory.newObjectBuilder( StateModel.class );
        personModelBuilder.use( Person.class );
        personModel = personModelBuilder.newInstance();

        window = new FrameFixture( new PersonFrame( personModel ) );
        window.show();
    }

    public final void assemble( ModuleAssembly aModuleAssembly )
        throws AssemblyException
    {
        aModuleAssembly.addAssembler( new SwingBindingAssembler() );
        aModuleAssembly.addComposites( PersonComposite.class );
    }

    @Override @After
    public void tearDown()
        throws Exception
    {
        window.cleanUp();
        super.tearDown();
    }

    private static class PersonFrame extends JFrame
    {
        public PersonFrame( StateModel<Person> aPersonModel )
        {
            super();

            JTextField nameField = createNameField();
            add( nameField );

            Person template = aPersonModel.state();
            aPersonModel.bind( template.name() ).to( nameField );

            add( createLostFocusField() );
        }

        private JTextField createLostFocusField()
        {
            JTextField lostFocusField = new JTextField();
            lostFocusField.setName( "lostFocus" );
            return lostFocusField;
        }

        private JTextField createNameField()
        {
            JTextField nameField = new JTextField();
            nameField.setName( "name" );
            return nameField;
        }
    }
}
