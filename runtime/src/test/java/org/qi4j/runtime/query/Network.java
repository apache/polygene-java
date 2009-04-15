/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.runtime.query;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.runtime.query.model.City;
import org.qi4j.runtime.query.model.Domain;
import org.qi4j.runtime.query.model.Female;
import org.qi4j.runtime.query.model.Male;
import org.qi4j.runtime.query.model.Nameable;
import org.qi4j.runtime.query.model.Person;
import org.qi4j.runtime.query.model.entities.FemaleEntity;
import org.qi4j.runtime.query.model.entities.MaleEntity;


/**
 * JAVADOC Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 20, 2008
 */
class Network
{
    private static List<Domain> domains;
    private static List<Person> persons;
    private static List<Male> males;
    private static List<Female> females;
    private static List<Nameable> nameables;

    static void populate( final UnitOfWork uow ) throws UnitOfWorkCompletionException
    {
        domains = new ArrayList<Domain>();
        persons = new ArrayList<Person>();
        males = new ArrayList<Male>();
        females = new ArrayList<Female>();
        nameables = new ArrayList<Nameable>();

        EntityBuilder<Domain> domainBuilder = uow.newEntityBuilder( Domain.class );
        Domain gaming = domainBuilder.newInstance();
        setName( gaming, "Gaming" );
        gaming.description().set( "Gaming domain" );

        Domain programming = domainBuilder.newInstance();
        setName( programming, "Programming" );
        programming.description().set( "Programing domain" );

        Domain cooking = domainBuilder.newInstance();
        setName( cooking, "Cooking" );
        cooking.description().set( "Cooking domain" );

        Domain cars = domainBuilder.newInstance();
        setName( cars, "Cars" );
        cars.description().set( "Cars" );

        EntityBuilder<City> cityBuilder = uow.newEntityBuilder( City.class );
        City kualaLumpur = cityBuilder.newInstance();
        setName( kualaLumpur, "Kuala Lumpur" );
        kualaLumpur.country().set( "Malaysia" );
        kualaLumpur.county().set( "Some Jaya" );

        City penang = cityBuilder.newInstance();
        setName( penang, "Penang" );
        penang.country().set( "Malaysia" );
        penang.county().set( "Some Other Jaya" );

        EntityBuilder<MaleEntity> maleBuilder = uow.newEntityBuilder( MaleEntity.class );
        EntityBuilder<FemaleEntity> femaleBuilder = uow.newEntityBuilder( FemaleEntity.class );

        Female annDoe = femaleBuilder.newInstance();
        setName( annDoe, "Ann Doe" );
        annDoe.placeOfBirth().set( kualaLumpur );
        annDoe.yearOfBirth().set( 1975 );
        annDoe.interests().add( 0, cooking );

        Male joeDoe = maleBuilder.newInstance();
        setName( joeDoe, "Joe Doe" );
        joeDoe.placeOfBirth().set( kualaLumpur );
        joeDoe.yearOfBirth().set( 1990 );
        joeDoe.mother().set( annDoe );
        joeDoe.interests().add( 0, programming );
        joeDoe.interests().add( 0, gaming );
        joeDoe.email().set( "joe@thedoes.net" );

        Male jackDoe = maleBuilder.newInstance();
        setName( jackDoe, "Jack Doe" );
        jackDoe.placeOfBirth().set( penang );
        jackDoe.yearOfBirth().set( 1970 );
        jackDoe.interests().add( 0, cars );
        jackDoe.wife().set( annDoe );

        domains.add( gaming );
        domains.add( programming );
        domains.add( cooking );
        domains.add( cars );

        persons.add( annDoe );
        persons.add( joeDoe );
        persons.add( jackDoe );

        females.add( annDoe );

        males.add( joeDoe );
        males.add( jackDoe );

        nameables.add( gaming );
        nameables.add( programming );
        nameables.add( cooking );
        nameables.add( cars );
        nameables.add( kualaLumpur );
        nameables.add( penang );
        nameables.add( annDoe );
        nameables.add( joeDoe );
        nameables.add( jackDoe );
    }

    static Iterable<Domain> domains()
    {
        return domains;
    }

    static Iterable<Person> persons()
    {
        return persons;
    }

    static Iterable<Nameable> nameables()
    {
        return nameables;
    }

    static Iterable<Male> males()
    {
        return males;
    }

    static Iterable<Female> females()
    {
        return females;
    }

    private static void setName( Nameable nameable, String name )
    {
        nameable.name().set( name );
    }

}