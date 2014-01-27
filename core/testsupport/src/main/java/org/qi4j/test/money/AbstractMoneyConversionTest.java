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
package org.qi4j.test.money;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.money.MoneyConversion;
import org.qi4j.api.money.Rate;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public abstract class AbstractMoneyConversionTest
    extends AbstractQi4jTest
{
    protected ServiceReference<MoneyConversion> moneyConversion;

    @Before
    public void beforeEachMoneyConversionTest()
    {
        moneyConversion = module.findService( MoneyConversion.class );
    }

    @Test
    public void script1()
    {
        Rate rate = moneyConversion.get().currentRate( CurrencyUnit.EUR, CurrencyUnit.of( "MAD" ) );
        System.out.println( "==========================" );
        System.out.println( rate );
        System.out.println( "==========================" );
        try
        {
            rate.convert( Money.of( CurrencyUnit.USD, 10 ) );
            fail( "Expected IllegalArgumentException" );
        }
        catch( IllegalArgumentException expected )
        {
        }
        Money dinars = rate.convert( Money.of( CurrencyUnit.EUR, 10 ) );
        assertThat( dinars.getAmount().intValue(), equalTo( 114 ) );

        Rate eod = moneyConversion.get().endOfDateRateAt( new DateTime(), CurrencyUnit.EUR, CurrencyUnit.of( "MAD" ) );
        assertThat( eod.convert( Money.of( CurrencyUnit.EUR, 10 ) ).getAmount().intValue(), equalTo( 114 ) );
    }
}
