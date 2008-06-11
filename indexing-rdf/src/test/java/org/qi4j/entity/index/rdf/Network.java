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
        EntityBuilder<DomainComposite> domainBuilder = unitOfWork.newEntityBuilder( DomainComposite.class );
        Domain gaming = domainBuilder.newInstance();
        gaming.name().set( "Gaming" );
        gaming.description().set( "Gaming domain" );

        Domain programming = domainBuilder.newInstance();
        programming.name().set( "Programming" );
        programming.description().set( "Programing domain" );

        Domain cooking = domainBuilder.newInstance();
        cooking.name().set( "Cooking" );
        cooking.description().set( "Cooking domain" );

        Domain cars = domainBuilder.newInstance();
        cars.name().set( "Cars" );
        cars.description().set( "Cars" );

        EntityBuilder<CityComposite> cityBuilder = unitOfWork.newEntityBuilder( CityComposite.class );
        City kualaLumpur = cityBuilder.newInstance();
        kualaLumpur.name().set( "Kuala Lumpur" );
        kualaLumpur.country().set( "Malaysia" );
        kualaLumpur.county().set( "Some Jaya" );

        City penang = cityBuilder.newInstance();
        penang.name().set( "Penang" );
        penang.country().set( "Malaysia" );
        penang.county().set( "Some Other Jaya" );

        EntityBuilder<MaleComposite> maleBuilder = unitOfWork.newEntityBuilder( MaleComposite.class );
        EntityBuilder<FemaleComposite> femaleBuilder = unitOfWork.newEntityBuilder( FemaleComposite.class );

        Female annDoe = femaleBuilder.newInstance();
        annDoe.name().set( "Ann Doe" );
        annDoe.placeOfBirth().set( kualaLumpur );
        annDoe.yearOfBirth().set( 1975 );
        annDoe.interests().add( cooking );

        Male joeDoe = maleBuilder.newInstance();
        joeDoe.name().set( "Joe Doe" );
        joeDoe.placeOfBirth().set( kualaLumpur );
        joeDoe.yearOfBirth().set( 1990 );
        joeDoe.mother().set( annDoe );
        joeDoe.interests().add( programming );
        joeDoe.interests().add( gaming );
        joeDoe.email().set( "joe@thedoes.net" );

        Male jackDoe = maleBuilder.newInstance();
        jackDoe.name().set( "Jack Doe" );
        jackDoe.placeOfBirth().set( penang );
        jackDoe.yearOfBirth().set( 1970 );
        jackDoe.interests().add( cars );
        jackDoe.wife().set( annDoe );

        EntityBuilder<CatComposite> catBuilder = unitOfWork.newEntityBuilder( CatComposite.class );

        Cat felix = catBuilder.newInstance();
        felix.name().set( "Felix" );

        unitOfWork.complete();
    }
}