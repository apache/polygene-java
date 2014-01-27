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
package org.qi4j.api.money;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.Property;

public class DocumentationSupport
{

    public interface CustomValueOrEntity
    {
        // START SNIPPET: state
        Property<Money> money();

        Property<BigMoney> bigMoney();

        Property<List<Money>> listOfMoney();

        Property<Map<String, Money>> mapOfMoney();
        // END SNIPPET: state
    }

    // START SNIPPET: injection
    @Service
    private MoneyConversion moneyConversion;
    // END SNIPPET: injection

    public void forDocumentationOnly()
    {
        DateTime pastDate = new DateTime( 1999, 7, 23, 12, 00 );
        // START SNIPPET: conversion
        Rate nowRate = moneyConversion.currentRate( CurrencyUnit.USD, CurrencyUnit.EUR );
        Rate pastRate = moneyConversion.endOfDateRateAt( pastDate, CurrencyUnit.USD, CurrencyUnit.EUR );
        // END SNIPPET: conversion
        // START SNIPPET: conversion
        BigMoney someBigMoney = BigMoney.of( CurrencyUnit.USD, 4096 );

        BigMoney pastEuros = pastRate.convert( someBigMoney );
        BigMoney nowEuros = nowRate.convert( someBigMoney );
        BigMoney balance = nowEuros.minus( pastEuros );
        // END SNIPPET: conversion
        // START SNIPPET: conversion
        Money someMoney = Money.of( CurrencyUnit.USD, 4096 );

        Money pastRoundedEuros = pastRate.convert( someMoney, RoundingMode.HALF_EVEN );
        Money nowRoundedEuros = nowRate.convert( someMoney, RoundingMode.HALF_EVEN );
        Money roundedBalance = nowRoundedEuros.minus( pastRoundedEuros );
        // END SNIPPET: conversion
    }

}
