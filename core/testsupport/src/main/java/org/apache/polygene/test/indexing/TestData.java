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
package org.apache.polygene.test.indexing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.test.model.Account;
import org.apache.polygene.test.model.Address;
import org.apache.polygene.test.model.Cat;
import org.apache.polygene.test.model.City;
import org.apache.polygene.test.model.Domain;
import org.apache.polygene.test.model.Female;
import org.apache.polygene.test.model.Male;
import org.apache.polygene.test.model.Person;
import org.apache.polygene.test.model.Protocol;
import org.apache.polygene.test.model.QueryParam;
import org.apache.polygene.test.model.URL;

import static java.time.ZoneOffset.UTC;

/**
 * Utility class to populate Index/Query tests data.
 */
public class TestData
{
    public static void populate( Module module )
        throws UnitOfWorkCompletionException
    {
        try (UnitOfWork unitOfWork = module.unitOfWorkFactory().newUnitOfWork())
        {
            NameableAssert.clear();
            Domain gaming;
            {
                EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class, StringIdentity.identityOf( "Gaming" ) );
                gaming = domainBuilder.instance();
                gaming.name().set( "Gaming" );
                gaming.description().set( "Gaming domain" );
                gaming = domainBuilder.newInstance();
                NameableAssert.trace( gaming );
            }

            Domain programming;
            {
                EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class );
                programming = domainBuilder.instance();
                programming.name().set( "Programming" );
                programming.description().set( "Programing domain" );
                programming = domainBuilder.newInstance();
                NameableAssert.trace( programming );
            }

            Domain cooking;
            {
                EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class );
                cooking = domainBuilder.instance();
                cooking.name().set( "Cooking" );
                cooking.description().set( "Cooking domain" );
                cooking = domainBuilder.newInstance();
                NameableAssert.trace( cooking );
            }

            Domain cars;
            {
                EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class );
                cars = domainBuilder.instance();
                cars.name().set( "Cars" );
                cars.description().set( "Cars" );
                cars = domainBuilder.newInstance();
                NameableAssert.trace( cars );
            }

            City kualaLumpur;
            {
                EntityBuilder<City> cityBuilder = unitOfWork.newEntityBuilder( City.class );
                kualaLumpur = cityBuilder.instance();
                kualaLumpur.name().set( "Kuala Lumpur" );
                kualaLumpur.country().set( "Malaysia" );
                kualaLumpur.county().set( "Some Jaya" );
                kualaLumpur = cityBuilder.newInstance();
                NameableAssert.trace( kualaLumpur );
            }

            City penang;
            {
                EntityBuilder<City> cityBuilder = unitOfWork.newEntityBuilder( City.class );
                penang = cityBuilder.instance();
                penang.name().set( "Penang" );
                penang.country().set( "Malaysia" );
                penang.county().set( "Some Other Jaya" );
                penang = cityBuilder.newInstance();
                NameableAssert.trace( penang );
            }

            Account annsAccount;
            {
                EntityBuilder<Account> accountBuilder = unitOfWork.newEntityBuilder( Account.class, StringIdentity.identityOf( "accountOfAnnDoe" ) );
                annsAccount = accountBuilder.instance();
                annsAccount.number().set( "accountOfAnnDoe" );
                annsAccount = accountBuilder.newInstance();
            }

            Account jacksAccount;
            {
                EntityBuilder<Account> accountBuilder = unitOfWork.newEntityBuilder( Account.class, StringIdentity.identityOf( "accountOfJackDoe" ) );
                jacksAccount = accountBuilder.instance();
                jacksAccount.number().set( "accountOfJackDoe" );
                jacksAccount = accountBuilder.newInstance();
            }

            ValueBuilder<Address> addressBuilder = module.newValueBuilder( Address.class );
            Address address = addressBuilder.prototype();
            address.line1().set( "Qi Street 4j" );
            address.line2().set( "Off main Java Street" );
            address.zipcode().set( "12345" );

            Female annDoe;
            {
                EntityBuilder<Female> femaleBuilder = unitOfWork.newEntityBuilder( Female.class, StringIdentity.identityOf( "anndoe" ) );
                annDoe = femaleBuilder.instance();
                annDoe.name().set( "Ann Doe" );
                annDoe.title().set( Person.Title.MRS );
                annDoe.placeOfBirth().set( kualaLumpur );
                annDoe.yearOfBirth().set( 1975 );
                annDoe.interests().add( 0, cooking );
                annDoe.password().set( "passwordOfAnnDoe" );
                annDoe.mainAccount().set( annsAccount );
                annDoe.accounts().put( "anns", annsAccount );
                annDoe.accounts().put( "jacks", jacksAccount );
                annDoe.address().set( addressBuilder.newInstance() );
                annDoe = femaleBuilder.newInstance();
                NameableAssert.trace( annDoe );
            }

            {
                EntityBuilder<Male> maleBuilder = unitOfWork.newEntityBuilder( Male.class );
                Male joeDoe = maleBuilder.instance();
                joeDoe.name().set( "Joe Doe" );
                joeDoe.title().set( Person.Title.MR );
                joeDoe.placeOfBirth().set( kualaLumpur );
                joeDoe.yearOfBirth().set( 1990 );
                joeDoe.mother().set( annDoe );
                joeDoe.interests().add( 0, programming );
                joeDoe.interests().add( 0, gaming );
                joeDoe.email().set( "joe@thedoes.net" );
                joeDoe.password().set( "passwordOfJoeDoe" );
                joeDoe = maleBuilder.newInstance();
                address = module.newValueBuilderWithPrototype( address ).prototype();
                address.line1().set( "Qi Alley 4j" );
                joeDoe.address().set( address );
                joeDoe.bigInteger().set( new BigInteger( "23232323232323232323232323" ) );
                joeDoe.bigDecimal().set( new BigDecimal( "23.4276931348623157e+309" ) );
                joeDoe.instantValue().set( ZonedDateTime.of( 2020, 3, 4, 13, 24, 35, 0, UTC ).toInstant() );
                joeDoe.dateTimeValue().set( ZonedDateTime.of( 2020, 3, 4, 13, 24, 35, 0, UTC ) );
                joeDoe.localDateTimeValue().set( LocalDateTime.of( 2020, 3, 4, 13, 23, 0 ) );
                joeDoe.localDateValue().set( LocalDate.of( 2020, 3, 4 ) );
                NameableAssert.trace( joeDoe );
            }

            {
                EntityBuilder<Male> maleBuilder = unitOfWork.newEntityBuilder( Male.class );
                Male jackDoe = maleBuilder.instance();
                jackDoe.name().set( "Jack Doe" );
                jackDoe.title().set( Person.Title.DR );
                jackDoe.placeOfBirth().set( penang );
                jackDoe.yearOfBirth().set( 1970 );
                jackDoe.interests().add( 0, cars );
                jackDoe.wife().set( annDoe );
                jackDoe.password().set( "passwordOfJohnDoe" );
                jackDoe.mainAccount().set( jacksAccount );
                jackDoe.accounts().put( "anns", annsAccount );
                jackDoe.accounts().put( "jacks", jacksAccount );
                address = module.newValueBuilderWithPrototype( address ).prototype();
                address.line1().set( "Qi Avenue 4j" );
                jackDoe.address().set( address );
                jackDoe.bigInteger().set( new BigInteger( "42424242424242424242424242" ) );
                jackDoe.bigDecimal().set( new BigDecimal( "42.2376931348623157e+309" ) );
                jackDoe.instantValue().set( ZonedDateTime.of( 2010, 3, 4, 13, 24, 35, 0, UTC ).toInstant() );
                jackDoe.dateTimeValue().set( ZonedDateTime.of( 2010, 3, 4, 13, 24, 35, 0, UTC ) );
                jackDoe.localDateTimeValue().set( LocalDateTime.of( 2010, 3, 4, 13, 23, 0 ) );
                jackDoe.localDateValue().set( LocalDate.of( 2010, 3, 4 ) );

                ValueBuilder<URL> urlBuilder = module.newValueBuilder( URL.class );
                ValueBuilder<Protocol> protocolBuilder = module.newValueBuilder( Protocol.class );
                ValueBuilder<QueryParam> queryParamBuilder = module.newValueBuilder( QueryParam.class );

                Protocol protocol = protocolBuilder.prototype();
                protocol.value().set( "http" );

                List<QueryParam> queryParams = new ArrayList<>( 2 );
                QueryParam param = queryParamBuilder.prototype();
                param.name().set( "user" );
                param.value().set( "jackdoe" );
                queryParams.add( queryParamBuilder.newInstance() );
                queryParamBuilder = module.newValueBuilder( QueryParam.class );
                param = queryParamBuilder.prototype();
                param.name().set( "password" );
                param.value().set( "somepassword" );
                queryParams.add( queryParamBuilder.newInstance() );

                URL url = urlBuilder.prototype();
                url.protocol().set( protocolBuilder.newInstance() );
                url.queryParams().set( queryParams );

                jackDoe.personalWebsite().set( urlBuilder.newInstance() );

                jackDoe = maleBuilder.newInstance();
                NameableAssert.trace( jackDoe );
            }

            {
                EntityBuilder<Cat> catBuilder = unitOfWork.newEntityBuilder( Cat.class );
                Cat felix = catBuilder.instance();
                felix.name().set( "Felix" );
                catBuilder.newInstance();
                NameableAssert.trace( felix );
            }
            unitOfWork.complete();
        }
    }

    private TestData()
    {
    }
}
