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
import org.qi4j.library.swing.binding.domain.Cat;
import org.qi4j.library.swing.binding.domain.Person;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

/**
 * @author edward.yakop@gmail.com
 */
public class StateModelAssociationTest extends AbstractStateModelTest
{
    @Test
    public final void testAssociationBinding()
        throws UnitOfWorkCompletionException
    {
        JTextComponentFixture catNameTextBox = window.textBox( "catName" );
        assertEquals( "", catNameTextBox.text() );

        String expectedCatName = "garfield";
        Cat garfield = createCat( expectedCatName );

        EntityBuilder<Person> personBuilder = uow.newEntityBuilder( Person.class );
        Person personPrototype = personBuilder.stateFor( Person.class );
        personPrototype.name().set( "Niclas" );
        personPrototype.favoriteCat().set( garfield );
        Person person = personBuilder.newInstance();
        uow.apply();
        
        person.favoriteCat().set( garfield );
        personModel.use( person );

        assertEquals( expectedCatName, catNameTextBox.text() );

        // Update name by using binding
        SwingBinding<Cat> catBinding = personModel.bind( personModel.state().favoriteCat() );
        String expectedCatName2 = "Hello Kitty";
        Cat hellokitty = createCat( expectedCatName2 );
        catBinding.stateModel().use( hellokitty );

        assertEquals( expectedCatName2, catNameTextBox.text() );

        // Update name by using UI
        catNameTextBox.deleteText();

        JTextComponentFixture lostFocusBox = window.textBox( "lostFocus" );
        lostFocusBox.focus();

//        assertEquals( "", hellokitty.name().get() );
    }

    private Cat createCat( String expectedCatName )
    {
        EntityBuilder<Cat> catBuilder = uow.newEntityBuilder( Cat.class );
        Cat catPrototype = catBuilder.stateFor( Cat.class );
        catPrototype.name().set( expectedCatName );
        Cat garfield = catBuilder.newInstance();
        return garfield;
    }
}
