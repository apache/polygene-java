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
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.lib.swing.binding.domain.Cat;
import org.qi4j.lib.swing.binding.domain.Person;

/**
 * @author edward.yakop@gmail.com
 */
public final class StateModelAssociationTest extends AbstractStateModelTest
{
    @Test
    @Ignore( "Visibility problems in the TestCase itself. Probably also waiting for X Virtual Framebuffer on Server.")
    public final void testAssociationBinding()
    {
        JTextComponentFixture catNameTextBox = window.textBox( "catName" );
        assertEquals( "", catNameTextBox.text() );

        Person person = uow.newEntity( Person.class );
        Cat garfield = uow.newEntity( Cat.class );
        String expectedCatName = "garfield";
        garfield.name().set( expectedCatName );
        person.favoriteCat().set( garfield );
        personModel.use( person );

        assertEquals( expectedCatName, catNameTextBox.text() );

        // Update name by using binding
        SwingBinding<Cat> catBinding = personModel.bind( personModel.state().favoriteCat() );
        Cat hellokitty = uow.newEntity( Cat.class );
        String expectedCatName2 = "Hello Kitty";
        hellokitty.name().set( expectedCatName2 );
        catBinding.stateModel().use( hellokitty );

        assertEquals( expectedCatName2, catNameTextBox.text() );

        // Update name by using UI
        catNameTextBox.deleteText();

        JTextComponentFixture lostFocusBox = window.textBox( "lostFocus" );
        lostFocusBox.focus();

//        assertEquals( "", hellokitty.name().get() );
    }
}
