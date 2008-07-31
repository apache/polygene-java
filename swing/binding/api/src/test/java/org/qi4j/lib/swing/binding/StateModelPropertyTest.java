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

import static junit.framework.Assert.assertEquals;
import org.fest.swing.fixture.JTextComponentFixture;
import org.junit.Test;
import org.qi4j.lib.swing.binding.domain.Person;

/**
 * @author edward.yakop@gmail.com
 */
public class StateModelPropertyTest extends AbstractStateModelTest
{
    @Test
    public final void testPropertyBinding()
    {
        JTextComponentFixture nameTextBox = window.textBox( "personName" );
        assertEquals( "", nameTextBox.text() );

        Person person = uow.newEntity( Person.class );
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
        nameTextBox.enterText( "Foo Bar" );

        JTextComponentFixture lostFocusField = window.textBox( "lostFocus" );
        lostFocusField.focus();

        assertEquals( expectedName2, person.name().get() );
        assertEquals( expectedName2, nameTextBox.text() );
    }
}
