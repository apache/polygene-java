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
package org.qi4j.library.money.oer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.money.MoneyConversionException;
import org.qi4j.api.money.Rate;
import org.qi4j.library.money.oer.OERConfiguration.AccountLevel;
import org.qi4j.spi.money.DefaultRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.joda.time.DateTimeFieldType.dayOfMonth;
import static org.joda.time.DateTimeFieldType.monthOfYear;
import static org.joda.time.DateTimeFieldType.year;

/**
 * OpenExchangeRates.org MoneyConversion Service Implementation.
 * <p>See OER terms and conditions: https://openexchangerates.org/terms/</p>
 * <p>Currencies are updated on a hourly basis.</p>
 * <p>Historical EOD rates are starting from 1999.</p>
 * <p>Support free to unlimited account types.</p>
 */
// TODO Use Cache API to minimize calls to the provider
// TODO Add out of the box optional CircuitBreaker support
public class OERMoneyConversionMixin
    implements OERMoneyConversionService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( OERMoneyConversionService.class );
    private static final DateTime OER_DATA_FROM = new DateTime( 1999, 1, 1, 0, 0 );
    @This
    private Configuration<OERConfiguration> configuration;
    private AccountLevel accountLevel = AccountLevel.free;
    private String latest;
    private String endOfDate;

    @Override
    public void activateService()
    {
        configuration.refresh();
        OERConfiguration config = configuration.get();
        if( config.enabled().get() )
        {
            String apiKey = config.apiKey().get();
            accountLevel = config.accountLevel().get();
            String baseUrl = ( config.https().get() ? "https" : "http" ) + "://openexchangerates.org/api/";
            latest = baseUrl + "latest.json?app_id=" + apiKey;
            endOfDate = baseUrl + "historical/%04d-%02d-%02d.json?app_id=" + apiKey;
            LOGGER.trace( "Open Exchange Rates Money Conversion Service is now available" );
        }
    }

    @Override
    public void passivateService()
    {
        accountLevel = AccountLevel.free;
        latest = null;
        endOfDate = null;
    }

    @Override
    public Rate currentRate( CurrencyUnit from, CurrencyUnit to )
        throws MoneyConversionException
    {
        return getRate( latest, from, to );
    }

    @Override
    public Rate endOfDateRateAt( DateTime date, CurrencyUnit from, CurrencyUnit to )
        throws MoneyConversionException
    {
        if( date.isBefore( OER_DATA_FROM ) || date.isAfterNow() )
        {
            throw new MoneyConversionException( "End of date exchange rate unavailable before " + OER_DATA_FROM
                                                + ", nor in the future. Was asked for " + date );
        }
        String url = String.format( endOfDate,
                                    date.get( year() ),
                                    date.get( monthOfYear() ),
                                    date.get( dayOfMonth() ) );
        return getRate( url, from, to );
    }

    @Override
    public Iterable<Rate> currentRates( CurrencyUnit from )
        throws MoneyConversionException
    {
        return getRates( latest, from );
    }

    @Override
    public Iterable<Rate> endOfDateRatesAt( DateTime date, CurrencyUnit from )
        throws MoneyConversionException
    {
        if( date.isBefore( OER_DATA_FROM ) || date.isAfterNow() )
        {
            throw new MoneyConversionException( "End of date exchange rate unavailable before " + OER_DATA_FROM
                                                + ", nor in the future. Was asked for " + date );
        }
        String url = String.format( endOfDate,
                                    date.get( year() ),
                                    date.get( monthOfYear() ),
                                    date.get( dayOfMonth() ) );
        return getRates( url, from );
    }

    private Iterable<Rate> getRates( String url, CurrencyUnit from )
    {
        switch( accountLevel )
        {
            case enterprise:
            case unlimited:
                url += "&base=" + from.getCode();
                break;
            default:
        }
        JSONObject json = getJSON( url );
        try
        {
            JSONObject rates = json.getJSONObject( "rates" );
            List<Rate> ratesList = new ArrayList<>( rates.length() );
            JSONArray names = rates.names();
            for( int idx = 0; idx < names.length(); idx++ )
            {
                CurrencyUnit currency = CurrencyUnit.of( names.getString( idx ) );
                ratesList.add( getRate( json, from, currency ) );
            }
            return ratesList;
        }
        catch( JSONException ex )
        {
            throw new MoneyConversionException( ex );
        }
    }

    private Rate getRate( String url, CurrencyUnit from, CurrencyUnit to )
    {
        switch( accountLevel )
        {
            case enterprise:
            case unlimited:
                url += "&base=" + from.getCode();
                break;
            default:
        }
        JSONObject json = getJSON( url );
        return getRate( json, from, to );
    }

    private JSONObject getJSON( String url )
    {
        try( InputStream input = new URL( url ).openConnection().getInputStream() )
        {
            JSONObject json = new JSONObject( new JSONTokener( input ) );
            if( LOGGER.isTraceEnabled() )
            {
                LOGGER.trace( "GET {}\n{}", url, json.toString( 2 ) );
            }
            return json;
        }
        catch( IOException | JSONException ex )
        {
            throw new MoneyConversionException( ex );
        }
    }

    private Rate getRate( JSONObject json, CurrencyUnit from, CurrencyUnit to )
    {
        try
        {
            DateTime timestamp = new DateTime( json.getLong( "timestamp" ) * 1000 );
            JSONObject rates = json.getJSONObject( "rates" );
            switch( accountLevel )
            {
                case enterprise:
                case unlimited:
                    return new DefaultRate(
                        from, to,
                        timestamp,
                        BigDecimal.valueOf( rates.getDouble( to.getCode() ) )
                    );
                default:
                    CurrencyUnit baseCurrency = CurrencyUnit.of( json.getString( "base" ) );
                    BigDecimal baseMultiplier = BigDecimal.valueOf( rates.getDouble( baseCurrency.getCode() ) );
                    BigDecimal fromMultiplier = BigDecimal.valueOf( rates.getDouble( from.getCode() ) );
                    BigDecimal toMultiplier = BigDecimal.valueOf( rates.getDouble( to.getCode() ) );
                    LOGGER.trace( "Multipliers:\n\t{}: {}\n\t{}: {}\n\t{}: {}",
                                  baseCurrency, baseMultiplier, from, fromMultiplier, to, toMultiplier );

                    BigDecimal computedMultiplier = baseMultiplier.
                        divide( fromMultiplier, RoundingMode.HALF_EVEN ).
                        multiply( toMultiplier );

                    return new DefaultRate(
                        from, to,
                        timestamp,
                        computedMultiplier
                    );
            }
        }
        catch( JSONException ex )
        {
            throw new MoneyConversionException( ex );
        }
    }

}
