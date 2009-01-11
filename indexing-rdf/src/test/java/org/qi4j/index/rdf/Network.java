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
package org.qi4j.index.rdf;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.index.rdf.model.Account;
import org.qi4j.index.rdf.model.Cat;
import org.qi4j.index.rdf.model.City;
import org.qi4j.index.rdf.model.Domain;
import org.qi4j.index.rdf.model.Female;
import org.qi4j.index.rdf.model.Male;
import org.qi4j.index.rdf.model.entities.CatEntity;
import org.qi4j.index.rdf.model.entities.FemaleEntity;
import org.qi4j.index.rdf.model.entities.MaleEntity;

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
        Domain gaming;
        {
            EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class );
            gaming = domainBuilder.stateOfComposite();
            gaming.name().set( "Gaming" );
            gaming.description().set( "Gaming domain" );
            gaming = domainBuilder.newInstance();
            NameableAssert.trace( gaming );
        }

        Domain programming;
        {
            EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class );
            programming = domainBuilder.stateOfComposite();
            programming.name().set("Programming");
            programming.description().set( "Programing domain" );
            programming = domainBuilder.newInstance();
            NameableAssert.trace( programming );
        }

        Domain cooking;
        {
            EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class );
            cooking = domainBuilder.stateOfComposite();
            cooking.name().set( "Cooking" );
            cooking.description().set( "Cooking domain" );
            cooking = domainBuilder.newInstance();
            NameableAssert.trace( cooking );
        }

        Domain cars;
        {
            EntityBuilder<Domain> domainBuilder = unitOfWork.newEntityBuilder( Domain.class );
            cars = domainBuilder.stateOfComposite();
            cars.name().set( "Cars" );
            cars.description().set( "Cars" );
            cars = domainBuilder.newInstance();
            NameableAssert.trace( cars );
        }

        City kualaLumpur;
        {
            EntityBuilder<City> cityBuilder = unitOfWork.newEntityBuilder( City.class );
            kualaLumpur = cityBuilder.stateOfComposite();
            kualaLumpur.name().set("Kuala Lumpur" );
            kualaLumpur.country().set( "Malaysia" );
            kualaLumpur.county().set( "Some Jaya" );
            kualaLumpur = cityBuilder.newInstance();
            NameableAssert.trace( kualaLumpur );
        }

        City penang;
        {
            EntityBuilder<City> cityBuilder = unitOfWork.newEntityBuilder( City.class );
            penang = cityBuilder.stateOfComposite();
            penang.name().set( "Penang" );
            penang.country().set( "Malaysia" );
            penang.county().set( "Some Other Jaya" );
            penang = cityBuilder.newInstance();
            NameableAssert.trace( penang );
        }

        Account annsAccount;
        {
            EntityBuilder<Account> accountBuilder = unitOfWork.newEntityBuilder( Account.class );
            annsAccount = accountBuilder.stateOfComposite();
            annsAccount.number().set( "accountOfAnnDoe" );
            annsAccount = accountBuilder.newInstance();
        }

        Account jacksAccount;
        {
            EntityBuilder<Account> accountBuilder = unitOfWork.newEntityBuilder( Account.class );
            jacksAccount = accountBuilder.stateOfComposite();
            jacksAccount.number().set( "accountOfJackDoe" );
            jacksAccount = accountBuilder.newInstance();
        }

        Female annDoe;
        {
            EntityBuilder<FemaleEntity> femaleBuilder = unitOfWork.newEntityBuilder( FemaleEntity.class );
            annDoe = femaleBuilder.stateOfComposite();
            annDoe.name().set( "Ann Doe" );
            annDoe.placeOfBirth().set( kualaLumpur );
            annDoe.yearOfBirth().set( 1975 );
            annDoe.interests().add( cooking );
            annDoe.password().set( "passwordOfAnnDoe" );
            annDoe.mainAccount().set( annsAccount );
            annDoe.accounts().add( annsAccount );
            annDoe.accounts().add( jacksAccount );
            annDoe = femaleBuilder.newInstance();
            NameableAssert.trace( annDoe );
        }

        {
            EntityBuilder<MaleEntity> maleBuilder = unitOfWork.newEntityBuilder( MaleEntity.class );
            Male joeDoe = maleBuilder.stateOfComposite();
            joeDoe.name().set( "Joe Doe" );
            joeDoe.placeOfBirth().set( kualaLumpur );
            joeDoe.yearOfBirth().set( 1990 );
            joeDoe.mother().set( annDoe );
            joeDoe.interests().add( programming );
            joeDoe.interests().add( gaming );
            joeDoe.email().set( "joe@thedoes.net" );
            joeDoe.password().set( "passwordOfJoeDoe" );
            joeDoe = maleBuilder.newInstance();
            NameableAssert.trace( joeDoe );
        }

        {
            EntityBuilder<MaleEntity> maleBuilder = unitOfWork.newEntityBuilder( MaleEntity.class );
            Male jackDoe = maleBuilder.stateOfComposite();
            jackDoe.name().set( "Jack Doe" );
            jackDoe.placeOfBirth().set( penang );
            jackDoe.yearOfBirth().set( 1970 );
            jackDoe.interests().add( cars );
            jackDoe.wife().set( annDoe );
            jackDoe.password().set( "passwordOfJohnDoe" );
            jackDoe.mainAccount().set( jacksAccount );
            jackDoe.accounts().add( annsAccount );
            jackDoe.accounts().add( jacksAccount );
            jackDoe = maleBuilder.newInstance();
            NameableAssert.trace( jackDoe );
        }

        {
            EntityBuilder<CatEntity> catBuilder = unitOfWork.newEntityBuilder( CatEntity.class );
            Cat felix = catBuilder.stateOfComposite();
            felix.name().set( "Felix" );
            felix = catBuilder.newInstance();
        }

        unitOfWork.complete();
    }
}