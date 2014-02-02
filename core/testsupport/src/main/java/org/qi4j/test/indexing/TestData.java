/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2014 Paul Merlin.
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
package org.qi4j.test.indexing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.test.indexing.model.Account;
import org.qi4j.test.indexing.model.Address;
import org.qi4j.test.indexing.model.Cat;
import org.qi4j.test.indexing.model.City;
import org.qi4j.test.indexing.model.Domain;
import org.qi4j.test.indexing.model.Female;
import org.qi4j.test.indexing.model.Male;
import org.qi4j.test.indexing.model.Person;
import org.qi4j.test.indexing.model.Protocol;
import org.qi4j.test.indexing.model.QueryParam;
import org.qi4j.test.indexing.model.URL;
import org.qi4j.test.indexing.model.entities.CatEntity;
import org.qi4j.test.indexing.model.entities.FemaleEntity;
import org.qi4j.test.indexing.model.entities.MaleEntity;

import static org.joda.time.DateTimeZone.UTC;

/**
 * Utility class to populate Index/Query tests data.
 */
class TestData
{
    static void populate( Module module )
        throws UnitOfWorkCompletionException
    {
        try( UnitOfWork unitOfWork = module.newUnitOfWork() )
        {
            NameableAssert.clear();
            Domain gaming;
            {
                EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class, "Gaming" );
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
                EntityBuilder<Account> accountBuilder = unitOfWork.newEntityBuilder( Account.class, "accountOfAnnDoe" );
                annsAccount = accountBuilder.instance();
                annsAccount.number().set( "accountOfAnnDoe" );
                annsAccount = accountBuilder.newInstance();
            }

            Account jacksAccount;
            {
                EntityBuilder<Account> accountBuilder = unitOfWork.newEntityBuilder( Account.class, "accountOfJackDoe" );
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
                EntityBuilder<FemaleEntity> femaleBuilder = unitOfWork.newEntityBuilder( FemaleEntity.class, "anndoe" );
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
                EntityBuilder<MaleEntity> maleBuilder = unitOfWork.newEntityBuilder( MaleEntity.class );
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
                joeDoe.dateValue().set( new DateTime( "2020-03-04T13:24:35", UTC ).toDate() );
                joeDoe.dateTimeValue().set( new DateTime( "2020-03-04T13:24:35", UTC ) );
                joeDoe.localDateTimeValue().set( new LocalDateTime( "2020-03-04T13:23:00" ) );
                joeDoe.localDateValue().set( new LocalDate( "2020-03-04" ) );
                joeDoe.money().set( Money.of( CurrencyUnit.USD, 100 ) );
                joeDoe.bigMoney().set( BigMoney.of( CurrencyUnit.USD, new BigDecimal( "1000000000000.000000" ) ) );
                joeDoe.moneys().set( Arrays.asList( Money.of( CurrencyUnit.USD, 100 ) ) );
                NameableAssert.trace( joeDoe );
            }

            {
                EntityBuilder<MaleEntity> maleBuilder = unitOfWork.newEntityBuilder( MaleEntity.class );
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
                jackDoe.dateValue().set( new DateTime( "2010-03-04T13:24:35", UTC ).toDate() );
                jackDoe.dateTimeValue().set( new DateTime( "2010-03-04T13:24:35", UTC ) );
                jackDoe.localDateTimeValue().set( new LocalDateTime( "2010-03-04T13:23:00" ) );
                jackDoe.localDateValue().set( new LocalDate( "2010-03-04" ) );
                jackDoe.money().set( Money.of( CurrencyUnit.USD, 1000 ) );
                jackDoe.moneys().set( Arrays.asList( Money.of( CurrencyUnit.USD, 100 ),
                                                     Money.of( CurrencyUnit.USD, 1000 ) ) );

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
                EntityBuilder<CatEntity> catBuilder = unitOfWork.newEntityBuilder( CatEntity.class );
                Cat felix = catBuilder.instance();
                felix.name().set( "Felix" );
                catBuilder.newInstance();
            }
            unitOfWork.complete();
        }
    }

    private TestData()
    {
    }
}
