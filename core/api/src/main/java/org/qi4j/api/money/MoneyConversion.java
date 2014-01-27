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

import org.joda.money.CurrencyUnit;
import org.joda.time.DateTime;

/**
 * Money conversion service.
 */
// START SNIPPET: conversion
public interface MoneyConversion
{
    /**
     * Get current Rate between two currencies.
     *
     * @param from Source currency
     * @param to Target currency
     * @return Current Rate
     * @throws MoneyConversionException if invocation fails
     */
    Rate currentRate( CurrencyUnit from, CurrencyUnit to )
        throws MoneyConversionException;

    /**
     * Get EOD (End Of Day) Rate between two currencies.
     *
     * @param date Date for EOD Rate
     * @param from Source currency
     * @param to Target currency
     * @return End Of Day Rate
     * @throws MoneyConversionException if invocation fails
     */
    Rate endOfDateRateAt( DateTime date, CurrencyUnit from, CurrencyUnit to )
        throws MoneyConversionException;

    /**
     * Get all current Rates from a currency.
     *
     * @param from Source currency
     * @return All current Rates available
     * @throws MoneyConversionException if invocation fails
     */
    Iterable<Rate> currentRates( CurrencyUnit from )
        throws MoneyConversionException;

    /**
     * Get all EOD (End Of Day) Rates from a currency.
     *
     * @param date Date for EOD Rate
     * @param from Source currency
     * @return All End Of Day Rates available
     * @throws MoneyConversionException if invocation fails
     */
    Iterable<Rate> endOfDateRatesAt( DateTime date, CurrencyUnit from )
        throws MoneyConversionException;

}
// END SNIPPET: conversion
