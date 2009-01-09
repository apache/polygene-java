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

import static junit.framework.Assert.assertEquals;
import org.fest.swing.fixture.JTextComponentFixture;
import org.junit.Test;
import org.junit.Ignore;
import org.qi4j.library.swing.binding.domain.Person;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

/**
 * @author edward.yakop@gmail.com
 */
public class StateModelPropertyTest extends AbstractStateModelTest
{
    @Test @Ignore( "Too sensitive of the GUI environment. Need better testing solution.")
    public final void testPropertyBinding() throws UnitOfWorkCompletionException
    {
        JTextComponentFixture nameTextBox = window.textBox( "personName" );
        assertEquals( "", nameTextBox.text() );

        EntityBuilder<Person> builder = uow.newEntityBuilder( Person.class );
        Person prototype = builder.stateFor( Person.class );
        personModel.use( prototype );

        assertEquals( "", nameTextBox.text() );

        // Update name by using binding
        SwingBinding<String> nameBinding = personModel.bind( personModel.state().name() );
        String expectedName = "Bar";
        nameBinding.stateModel().use( expectedName );
        Person person = builder.newInstance();
        personModel.use( person );

        assertEquals( expectedName, person.name().get() );
        assertEquals( expectedName, nameTextBox.text() );

        // Update name by using UI
        String expectedName2 = "Foo " + expectedName;
        nameTextBox.deleteText();
        nameTextBox.enterText( "Foo " + expectedName );

        JTextComponentFixture lostFocusField = window.textBox( "lostFocus" );
        lostFocusField.focus();
        assertEquals( expectedName2, person.name().get() );
        assertEquals( expectedName2, nameTextBox.text() );
    }
}
