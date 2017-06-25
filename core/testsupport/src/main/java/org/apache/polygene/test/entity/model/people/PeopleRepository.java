/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.test.entity.model.people;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;

import static org.apache.polygene.api.identity.StringIdentity.identityOf;

@Mixins( PeopleRepository.Mixin.class )
public interface PeopleRepository
{
    Person createPerson( String name, Country nationality, @Optional Address address, @Optional Person spouse, @Optional PhoneNumber homeNumber );

    void addChild( Person parent, Person child );

    Person findPersonByName( String name );

    Country createCountry( String countryCode, String countryName );

    Country findCountryByCountryCode( String countryCode );

    Country findCountryByIdentity( Identity countryId );

    Address createAddress( String street, String zipCode, City city, Country country, Rent rent );

    Address findAddress( Identity addressId );

    City createCity( String cityName );

    City findCity( Identity cityId );

    PhoneNumber createPhoneNumber( String phoneNumberString );

    PhoneNumber findPhoneNumberById( Identity phoneNumberId );

    class Mixin
        implements PeopleRepository
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public Person createPerson( String name, Country nationality, Address address, Person spouse, PhoneNumber homeNumber )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Person> builder = uow.newEntityBuilder( Person.class, identityOf( "person-" + name ) );
            Person instance = builder.instance();
            instance.name().set( name );
            instance.nationality().set( nationality );
            instance.address().set( address );
            instance.spouse().set( spouse );
            if( homeNumber != null )
            {
                instance.phoneNumbers().put( "Home", homeNumber );
            }
            return builder.newInstance();
        }

        @Override
        public void addChild( Person parent, Person child )
        {
            parent.children().add( child );
        }

        @Override
        public Person findPersonByName( String name )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get( Person.class, identityOf( "person-" + name ) );
        }

        @Override
        public Country createCountry( String countryCode, String countryName )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Country> builder = uow.newEntityBuilder( Country.class, identityOf( "country-" + countryCode ) );
            builder.instance().name().set( countryName );
            return builder.newInstance();
        }

        @Override
        public Country findCountryByCountryCode( String countryCode )
        {

            return findCountryByIdentity( identityOf( "country-" + countryCode ) );
        }

        @Override
        public Country findCountryByIdentity( Identity countryId )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get( Country.class, countryId );
        }

        @Override
        public Address createAddress( String street, String zipCode, City city, Country country, Rent rent )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Address> builder = uow.newEntityBuilder( Address.class );
            Address prototype = builder.instance();
            prototype.street().set( street );
            prototype.zipCode().set( zipCode );
            prototype.city().set( city );
            prototype.country().set( country );
            prototype.rent().set( rent );
            return builder.newInstance();
        }

        @Override
        public Address findAddress( Identity addressId )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get( Address.class, addressId );
        }

        @Override
        public City createCity( String cityName )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<City> builder = uow.newEntityBuilder( City.class );
            builder.instance().name().set( cityName );
            return builder.newInstance();
        }

        @Override
        public City findCity( Identity cityId )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get( City.class, cityId );
        }

        @Override
        public PhoneNumber createPhoneNumber( String phoneNumberString )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<PhoneNumber> builder = uow.newEntityBuilder( PhoneNumber.class );
            PhoneNumber prototype = builder.instance();

            // Of course better parsing should be done for a real application.
            int pos1 = phoneNumberString.indexOf( '-' );
            int pos2 = phoneNumberString.indexOf( '-', pos1 + 1 );
            String countryCode = phoneNumberString.substring( 1, pos1 );
            String areaCode = phoneNumberString.substring( pos1 + 1, pos2 );
            String number = phoneNumberString.substring( pos2 + 1 );

            prototype.countryCode().set( Integer.parseInt( countryCode ) );
            prototype.areaCode().set( Integer.parseInt( areaCode ) );
            prototype.number().set( number );

            return builder.newInstance();
        }

        @Override
        public PhoneNumber findPhoneNumberById( Identity phoneNumberId )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get( PhoneNumber.class, phoneNumberId );
        }
    }
}
