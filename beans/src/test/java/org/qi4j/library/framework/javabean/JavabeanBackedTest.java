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

package org.qi4j.library.framework.javabean;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;

public class JavabeanBackedTest extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( PersonComposite.class, CountryComposite.class );
    }

    @Test
    public void givenPersonPojoWhenDataIsOkThenExpectCorrectResult()
        throws Exception
    {
        CountryPojo malaysia = new CountryPojo( "Malaysia" );
        PersonPojo pojo = new PersonPojo( "Niclas Hedhman", malaysia );
        CompositeBuilder<Person> builder = compositeBuilderFactory.newCompositeBuilder( Person.class );
        builder.use( pojo );
        Person person = builder.newInstance();
        Property<String> stringProperty = person.name();
        assertEquals( "Name match.", "Niclas Hedhman", stringProperty.get() );
        assertEquals( "Country match.", "Malaysia", person.country().get().name().get() );

    }


    public interface PersonComposite extends Person, JavabeanBacked, Composite
    {
    }

    public interface Person
    {
        Property<String> name();

        Property<Country> country();
    }

    public interface CountryComposite extends Country, JavabeanBacked, Composite
    {
    }

    public interface Country
    {
        Property<String> name();
    }

    public class PersonPojo
    {
        private String name;
        private CountryPojo country;

        public PersonPojo( String name, CountryPojo country )
        {
            this.name = name;
            this.country = country;
        }

        public String getName()
        {
            return name;
        }

        public CountryPojo getCountry()
        {
            return country;
        }
    }

    public class CountryPojo
    {
        private String countryName;

        public CountryPojo( String countryName )
        {
            this.countryName = countryName;
        }

        public String getName()
        {
            return countryName;
        }
    }
}
