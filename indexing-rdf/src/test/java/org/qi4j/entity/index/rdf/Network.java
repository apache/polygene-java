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
package org.qi4j.entity.index.rdf;

import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.index.rdf.model.Account;
import org.qi4j.entity.index.rdf.model.Cat;
import org.qi4j.entity.index.rdf.model.City;
import org.qi4j.entity.index.rdf.model.Domain;
import org.qi4j.entity.index.rdf.model.Female;
import org.qi4j.entity.index.rdf.model.Male;
import org.qi4j.entity.index.rdf.model.Nameable;
import org.qi4j.entity.index.rdf.model.entities.CatEntity;
import org.qi4j.entity.index.rdf.model.entities.FemaleEntity;
import org.qi4j.entity.index.rdf.model.entities.MaleEntity;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 20, 2008
 */
class Network
{
    static void populate( UnitOfWork unitOfWork )
        throws UnitOfWorkCompletionException
    {
        NameableAssert.clear();
        EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class );
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

        EntityBuilder<City> cityBuilder = unitOfWork.newEntityBuilder( City.class );
        City kualaLumpur = cityBuilder.newInstance();
        setName( kualaLumpur, "Kuala Lumpur" );
        kualaLumpur.country().set( "Malaysia" );
        kualaLumpur.county().set( "Some Jaya" );

        City penang = cityBuilder.newInstance();
        setName( penang, "Penang" );
        penang.country().set( "Malaysia" );
        penang.county().set( "Some Other Jaya" );

        EntityBuilder<Account> accountBuilder = unitOfWork.newEntityBuilder( Account.class );
        Account annsAccount = accountBuilder.newInstance();
        annsAccount.number().set( "accountOfAnnDoe" );

        Account jacksAccount = accountBuilder.newInstance();
        jacksAccount.number().set( "accountOfJackDoe" );

        EntityBuilder<MaleEntity> maleBuilder = unitOfWork.newEntityBuilder( MaleEntity.class );
        EntityBuilder<FemaleEntity> femaleBuilder = unitOfWork.newEntityBuilder( FemaleEntity.class );

        Female annDoe = femaleBuilder.newInstance();
        setName( annDoe, "Ann Doe" );
        annDoe.placeOfBirth().set( kualaLumpur );
        annDoe.yearOfBirth().set( 1975 );
        annDoe.interests().add( cooking );
        annDoe.password().set( "passwordOfAnnDoe" );
        annDoe.mainAccount().set( annsAccount );
        annDoe.accounts().add( annsAccount );
        annDoe.accounts().add( jacksAccount );

        Male joeDoe = maleBuilder.newInstance();
        setName( joeDoe, "Joe Doe" );
        joeDoe.placeOfBirth().set( kualaLumpur );
        joeDoe.yearOfBirth().set( 1990 );
        joeDoe.mother().set( annDoe );
        joeDoe.interests().add( programming );
        joeDoe.interests().add( gaming );
        joeDoe.email().set( "joe@thedoes.net" );
        joeDoe.password().set( "passwordOfJoeDoe" );

        Male jackDoe = maleBuilder.newInstance();
        setName( jackDoe, "Jack Doe" );
        jackDoe.placeOfBirth().set( penang );
        jackDoe.yearOfBirth().set( 1970 );
        jackDoe.interests().add( cars );
        jackDoe.wife().set( annDoe );
        jackDoe.password().set( "passwordOfJohnDoe" );
        jackDoe.mainAccount().set( jacksAccount );
        jackDoe.accounts().add( annsAccount );
        jackDoe.accounts().add( jacksAccount );

        EntityBuilder<CatEntity> catBuilder = unitOfWork.newEntityBuilder( CatEntity.class );

        Cat felix = catBuilder.newInstance();
        felix.name().set( "Felix" );

        unitOfWork.complete();
    }

    private static void setName( Nameable nameable, String name )
    {
        nameable.name().set( name );
        NameableAssert.trace( nameable );
    }

}