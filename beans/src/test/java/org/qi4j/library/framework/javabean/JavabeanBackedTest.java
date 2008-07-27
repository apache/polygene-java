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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;

public class JavabeanBackedTest extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( PersonComposite.class, CountryComposite.class, CityComposite.class );
    }

    @Test
    public void givenPersonPojoWhenDataIsOkThenExpectCorrectResult()
        throws Exception
    {
        CountryPojo malaysia = new CountryPojo( "Malaysia" );
        Set cities = new HashSet();
        CityPojo kl = new CityPojo( "Kuala Lumpur", malaysia );
        cities.add( kl );
        CityPojo jb = new CityPojo( "Johor Bahru", malaysia );
        cities.add( jb );
        CityPojo penang = new CityPojo( "Penang", malaysia );
        cities.add( penang );
        CityPojo kk = new CityPojo( "Kota Kinabalu", malaysia );
        cities.add( kk );
        malaysia.setCities( cities );

        List<PersonPojo> friendsNiclas = new ArrayList<PersonPojo>();
        List<PersonPojo> friendsMakas = new ArrayList<PersonPojo>();
        List<PersonPojo> friendsEdward = new ArrayList<PersonPojo>();
        PersonPojo niclasPojo = new PersonPojo( "Niclas Hedhman", kl, friendsNiclas );
        PersonPojo makasPojo = new PersonPojo( "Makas Lau", kl, friendsMakas );
        PersonPojo edwardPojo = new PersonPojo( "Edward Yakop", kl, friendsEdward );
        friendsEdward.add( makasPojo );
        friendsEdward.add( niclasPojo );
        friendsMakas.add( edwardPojo );
        friendsMakas.add( niclasPojo );
        friendsNiclas.add( makasPojo );
        friendsNiclas.add( edwardPojo );

        CompositeBuilder<Person> builder = compositeBuilderFactory.newCompositeBuilder( Person.class );
        builder.use( niclasPojo );
        Person niclas = builder.newInstance();
        Property<String> stringProperty = niclas.name();
        assertEquals( "Name match.", "Niclas Hedhman", stringProperty.get() );
        Property<City> cityProperty = niclas.city();
        City cityValue = cityProperty.get();
        Association<Country> countryAssociation = cityValue.country();
        Country country = countryAssociation.get();
        assertEquals( "Country match.", "Malaysia", country.name().get() );
        SetAssociation citylist = country.cities();
        Iterator iterator = citylist.iterator();
        while( iterator.hasNext() )
        {
            City city = (City) iterator.next();
            String name = city.name().get();
            assertTrue( name.equals( "Kuala Lumpur" ) ||
                        name.equals( "Johor Bahru" ) ||
                        name.equals( "Kota Kinabalu" ) ||
                        name.equals( "Penang" )
            );
        }
        assertEquals( 4, country.cities().size() );
    }


    public interface PersonComposite extends Person, JavabeanSupport, Composite
    {
    }

    public interface Person
    {
        Property<String> name();

        Property<City> city();

        ListAssociation<Person> friends();
    }

    public interface CityComposite extends City, JavabeanSupport, Composite
    {
    }

    public interface CountryComposite extends Country, JavabeanSupport, Composite
    {
    }

    public interface City
    {
        Property<String> name();

        Association<Country> country();
    }

    public interface Country
    {
        Property<String> name();

        SetAssociation<City> cities();
    }

    public class PersonPojo
    {
        private String name;
        private CityPojo city;
        private List<PersonPojo> friends;

        public PersonPojo( String name, CityPojo city, List<PersonPojo> friends )
        {
            this.name = name;
            this.city = city;
            this.friends = friends;
        }

        public String getName()
        {
            return name;
        }

        public CityPojo getCity()
        {
            return city;
        }

        public List<PersonPojo> getFriends()
        {
            return friends;
        }
    }

    public class CountryPojo
    {
        private String countryName;
        private Set cities;

        public CountryPojo( String countryName )
        {
            this.countryName = countryName;
        }

        public String getName()
        {
            return countryName;
        }

        public Set<CityPojo> getCities()
        {
            return cities;
        }

        public void setCities( Set<CityPojo> cities )
        {
            this.cities = cities;
        }
    }

    public class CityPojo
    {
        private final String name;
        private final CountryPojo country;

        public CityPojo( String name, CountryPojo country )
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
}
