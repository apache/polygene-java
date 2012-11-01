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
package org.qi4j.test.indexing;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.test.indexing.model.Person;

/**
 * JAVADOC Add JavaDoc
 */
public class GoogleMixin
    implements Google
{

    @Structure
    UnitOfWorkFactory unitOfWorkFactory;

    @Override
    public Iterable<Person> bornIn( String city )
    {
//        QueryBuilder<Person> queryBuilder = module.newUnitOfWork().queryBuilderFactory()
//            .newQueryBuilder( Person.class );
//        Person personTemplate = queryBuilder.parameter( Person.class );
//        Query<Person> query = queryBuilder
//            .where( eq( personTemplate.placeOfBirth(), city ) )
//            .newQuery();
//        query.find();
        return null;
    }
}