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
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.runtime.query.model.City;
import org.qi4j.runtime.query.model.Domain;
import org.qi4j.runtime.query.model.Female;
import org.qi4j.runtime.query.model.Male;
import org.qi4j.runtime.query.model.Nameable;
import org.qi4j.runtime.query.model.Person;
import org.qi4j.runtime.query.model.entities.FemaleEntity;
import org.qi4j.runtime.query.model.entities.MaleEntity;
import org.qi4j.runtime.query.model.entities.PetEntity;
import org.qi4j.runtime.query.model.values.ContactValue;
import org.qi4j.runtime.query.model.values.ContactsValue;

/**
 * JAVADOC Add JavaDoc
 */
class Network
{
    private static List<Domain> domains;
    private static List<Person> persons;
    private static List<Male> males;
    private static List<Female> females;
    private static List<PetEntity> pets;
    private static List<Nameable> nameables;

    static void populate( final UnitOfWork uow, ValueBuilderFactory vbf )
        throws UnitOfWorkCompletionException
    {
        domains = new ArrayList<Domain>();
        persons = new ArrayList<Person>();
        males = new ArrayList<Male>();
        females = new ArrayList<Female>();
        pets = new ArrayList<PetEntity>();
        nameables = new ArrayList<Nameable>();

        Domain gaming = uow.newEntity( Domain.class );
        setName( gaming, "Gaming" );
        gaming.description().set( "Gaming domain" );

        Domain programming = uow.newEntity( Domain.class );
        setName( programming, "Programming" );
        programming.description().set( "Programing domain" );

        Domain cooking = uow.newEntity( Domain.class );
        setName( cooking, "Cooking" );
        cooking.description().set( "Cooking domain" );

        Domain cars = uow.newEntity( Domain.class );
        setName( cars, "Cars" );
        cars.description().set( "Cars" );

        City kualaLumpur = uow.newEntity( City.class, "kualalumpur" );
        setName( kualaLumpur, "Kuala Lumpur" );
        kualaLumpur.country().set( "Malaysia" );
        kualaLumpur.county().set( "Some Jaya" );

        City penang = uow.newEntity( City.class, "penang" );
        setName( penang, "Penang" );
        penang.country().set( "Malaysia" );
        penang.county().set( "Some Other Jaya" );

        Female vivianSmith = uow.newEntity( FemaleEntity.class );
        setName( vivianSmith, "Vivian Smith" );
        vivianSmith.placeOfBirth().set( kualaLumpur );
        vivianSmith.yearOfBirth().set( 1992 );
        vivianSmith.interests().add( 0, gaming );
        vivianSmith.interests().add( 0, programming );
        vivianSmith.email().set( "viv@smith.edu" );
        List<String> vivianTags = new ArrayList<String>();
        vivianTags.add( "Awesome" );
        vivianTags.add( "Pretty" );
        vivianTags.add( "Cool" );
        vivianSmith.tags().set( vivianTags );

        Female annDoe = uow.newEntity( FemaleEntity.class );
        setName( annDoe, "Ann Doe" );
        annDoe.placeOfBirth().set( kualaLumpur );
        annDoe.yearOfBirth().set( 1975 );
        annDoe.interests().add( 0, cooking );
        List<String> annTags = new ArrayList<String>();
        annTags.add( "Conservative" );
        annTags.add( "Pretty" );
        annDoe.tags().set( annTags );

        Male joeDoe = uow.newEntity( MaleEntity.class );
        setName( joeDoe, "Joe Doe" );
        joeDoe.placeOfBirth().set( kualaLumpur );
        joeDoe.yearOfBirth().set( 1990 );
        joeDoe.mother().set( annDoe );
        joeDoe.pastGirlFriends().add( 0, annDoe );
        joeDoe.interests().add( 0, programming );
        joeDoe.interests().add( 0, gaming );
        joeDoe.email().set( "joe@thedoes.net" );
        List<String> joeTags = new ArrayList<String>();
        joeTags.add( "Cool" );
        joeTags.add( "Hunk" );
        joeTags.add( "Awesome" );
        joeDoe.tags().set( joeTags );

        Male jackDoe = uow.newEntity( MaleEntity.class );
        setName( jackDoe, "Jack Doe" );
        jackDoe.placeOfBirth().set( penang );
        jackDoe.yearOfBirth().set( 1970 );
        jackDoe.interests().add( 0, cars );
        jackDoe.wife().set( annDoe );
        List<String> jackTags = new ArrayList<String>();
        jackTags.add( "Conservative" );
        jackTags.add( "Awesome" );
        jackDoe.tags().set( jackTags );

        ValueBuilder<ContactsValue> builder = vbf.newValueBuilder( ContactsValue.class );
        ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder( ContactValue.class );
        contactBuilder.prototype().email().set( "jackdoe@someplace.com" );
        contactBuilder.prototype().phone().set( "555-1234" );
        builder.prototype().contacts().get().add( contactBuilder.newInstance() );
        jackDoe.contacts().set( builder.newInstance() );

        annDoe.husband().set( jackDoe );

        PetEntity rex = uow.newEntity( PetEntity.class );
        setName( rex, "Rex" );
        rex.changeOwner( jackDoe );
        rex.updateDescription( "Rex is a great dog" );

        PetEntity kitty = uow.newEntity( PetEntity.class );
        setName( kitty, "Kitty" );
        kitty.changeOwner( annDoe );

        domains.add( gaming );
        domains.add( programming );
        domains.add( cooking );
        domains.add( cars );

        persons.add( annDoe );
        persons.add( joeDoe );
        persons.add( jackDoe );
        persons.add( vivianSmith );

        females.add( annDoe );
        females.add( vivianSmith );

        males.add( joeDoe );
        males.add( jackDoe );

        pets.add( rex );
        pets.add( kitty );

        nameables.add( gaming );
        nameables.add( programming );
        nameables.add( cooking );
        nameables.add( cars );
        nameables.add( kualaLumpur );
        nameables.add( penang );
        nameables.add( annDoe );
        nameables.add( joeDoe );
        nameables.add( jackDoe );
        nameables.add( vivianSmith );
    }

    static void refresh( UnitOfWork uow )
    {
        refresh( uow, domains );
        refresh( uow, persons );
        refresh( uow, males );
        refresh( uow, females );
        refresh( uow, pets );
        refresh( uow, nameables );
    }

    private static <T> void refresh( UnitOfWork uow, List<T> list )
    {
        for( int i = 0; i < list.size(); i++ )
        {
            T entity = list.get( i );
            list.set( i, uow.get( entity ) );
        }
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

    static Iterable<PetEntity> pets()
    {
        return pets;
    }

    private static void setName( Nameable nameable, String name )
    {
        nameable.name().set( name );
    }
}