/*
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
import java.util.Arrays;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;
import org.qi4j.api.query.Query;
import org.qi4j.test.indexing.model.Person;

import static org.qi4j.api.query.QueryExpressions.contains;
import static org.qi4j.api.query.QueryExpressions.containsAll;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.ge;
import static org.qi4j.api.query.QueryExpressions.gt;
import static org.qi4j.api.query.QueryExpressions.le;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.QueryExpressions.ne;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.test.indexing.NameableAssert.verifyUnorderedResults;

/**
 * Abstract satisfiedBy with tests for Money queries against Index/Query engines.
 */
public class AbstractMoneyQueryTest
    extends AbstractAnyQueryTest
{

    @Test
    public void script1()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( eq( templateFor( Person.class ).money(),
                       Money.of( CurrencyUnit.USD, 100 ) ) ) );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script2()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( ne( templateFor( Person.class ).money(),
                       Money.of( CurrencyUnit.USD, 100 ) ) ) );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script3()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( gt( templateFor( Person.class ).money(),
                       Money.of( CurrencyUnit.USD, 100 ) ) ) );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script4()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( ge( templateFor( Person.class ).money(),
                       Money.of( CurrencyUnit.USD, 100 ) ) ) );

        verifyUnorderedResults( query, "Joe Doe", "Jack Doe" );
    }

    @Test
    public void script5()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( lt( templateFor( Person.class ).money(),
                       Money.of( CurrencyUnit.USD, 1000 ) ) ) );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script6()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( le( templateFor( Person.class ).money(),
                       Money.of( CurrencyUnit.USD, 1000 ) ) ) );

        verifyUnorderedResults( query, "Joe Doe", "Jack Doe" );
    }

    @Test
    public void script7()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( contains( templateFor( Person.class ).moneys(),
                             Money.of( CurrencyUnit.USD, 100 ) ) ) );

        verifyUnorderedResults( query, "Joe Doe", "Jack Doe" );
    }

    @Test
    public void script8()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( containsAll( templateFor( Person.class ).moneys(),
                                Arrays.asList( Money.of( CurrencyUnit.USD, 100 ),
                                               Money.of( CurrencyUnit.USD, 1000 ) ) ) ) );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script9()
    {
        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
            where( eq( templateFor( Person.class ).bigMoney(),
                       BigMoney.of( CurrencyUnit.USD, new BigDecimal( "1000000000000.000000" ) ) ) ) );

        verifyUnorderedResults( query, "Joe Doe" );
    }
}
