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
package org.qi4j.money.oer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.money.MoneyConversionException;
import org.qi4j.api.money.Rate;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.money.oer.OERConfiguration.AccountLevel;
import org.qi4j.spi.cache.Cache;
import org.qi4j.spi.cache.CachePool;
import org.qi4j.spi.money.DefaultRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Locale.US;
import static org.joda.time.DateTimeFieldType.dayOfMonth;
import static org.joda.time.DateTimeFieldType.monthOfYear;
import static org.joda.time.DateTimeFieldType.year;

/**
 * OpenExchangeRates.org MoneyConversion Service Implementation.
 * <p>See OER terms and conditions: https://openexchangerates.org/terms/</p>
 * <p>Currencies are updated on a hourly basis.</p>
 * <p>Historical EOD rates are starting from 1999.</p>
 * <p>Support free to unlimited account types.</p>
 * <p>Qi4j Cache extension is leveraged if available.</p>
 */
public class OERMoneyConversionMixin
    implements OERMoneyConversionService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( OERMoneyConversionService.class );
    private static final DateTime OER_DATA_FROM = new DateTime( 1999, 1, 1, 0, 0 );
    @This
    private Configuration<OERConfiguration> configuration;
    @Uses
    private ServiceDescriptor descriptor;
    @Optional @Service
    private CachePool caching;
    private Cache<Rate> rateCache;
    private Cache<List> ratesCache;
    private final AtomicLong cacheMisses = new AtomicLong( 0 );
    private final AtomicLong cacheHits = new AtomicLong( 0 );
    private AccountLevel accountLevel;
    private String latest;
    private String endOfDate;

    @Override
    public void activateService()
    {
        configuration.refresh();
        OERConfiguration config = configuration.get();
        if( config.enabled().get() )
        {
            // Configure MoneyConversion if enabled
            String apiKey = config.apiKey().get();
            accountLevel = config.accountLevel().get();
            String baseUrl = ( config.https().get() ? "https" : "http" ) + "://openexchangerates.org/api/";
            latest = baseUrl + "latest.json?app_id=" + apiKey;
            endOfDate = baseUrl + "historical/%04d-%02d-%02d.json?app_id=" + apiKey;

            // Enable caching if available
            if( caching != null )
            {
                String rateCacheId = descriptor.identity() + "-" + UUID.randomUUID().toString();
                rateCache = caching.fetchCache( rateCacheId, Rate.class );
                String ratesCacheId = descriptor.identity() + "-" + UUID.randomUUID().toString();
                ratesCache = caching.fetchCache( ratesCacheId, List.class );
            }

            LOGGER.trace( "Open Exchange Rates Money Conversion Service is now available" );
        }
    }

    @Override
    public void passivateService()
    {
        accountLevel = AccountLevel.free;
        latest = null;
        endOfDate = null;
        if( caching != null )
        {
            caching.returnCache( rateCache );
            rateCache = null;
            caching.returnCache( ratesCache );
            ratesCache = null;
            cacheHits.set( 0L );
            cacheMisses.set( 0L );
        }
    }

    @Override
    public long cacheHits()
    {
        return cacheHits.get();
    }

    @Override
    public long cacheMisses()
    {
        return cacheMisses.get();
    }

    @Override
    public Rate currentRate( CurrencyUnit from, CurrencyUnit to )
        throws MoneyConversionException
    {
        String cacheKey = String.format( "LATEST-%s-%s", from.getCode(), to.getCode() );
        if( caching != null )
        {
            Rate rate = rateCache.get( cacheKey );
            if( rate != null )
            {
                if( rate.when().plusHours( 1 ).isAfterNow() )
                {
                    cacheHits.incrementAndGet();
                    return rate;
                }
                else
                {
                    rateCache.remove( cacheKey );
                }
            }
        }
        Rate rate = getRate( latest, from, to );
        if( caching != null )
        {
            cacheMisses.incrementAndGet();
            rateCache.put( cacheKey, rate );
        }
        return rate;
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
        int year = date.get( year() );
        int month = date.get( monthOfYear() );
        int day = date.get( dayOfMonth() );
        String cacheKey = String.format( US, "EOD-%s-%s-%d-%d-%d", from.getCode(), to.getCode(), year, month, day );
        if( caching != null )
        {
            Rate rate = rateCache.get( cacheKey );
            if( rate != null )
            {
                cacheHits.incrementAndGet();
                return rate;
            }
        }
        String url = String.format( endOfDate, year, month, day );
        Rate rate = getRate( url, from, to );
        if( caching != null )
        {
            cacheMisses.incrementAndGet();
            rateCache.put( cacheKey, rate );
        }
        return rate;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Iterable<Rate> currentRates( CurrencyUnit from )
        throws MoneyConversionException
    {
        String cacheKey = String.format( "LATEST-%s", from.getCode() );
        if( caching != null )
        {
            List<Rate> rates = ratesCache.get( cacheKey );
            if( rates != null )
            {
                if( rates.get( 0 ).when().plusHours( 1 ).isAfterNow() )
                {
                    cacheHits.incrementAndGet();
                    return rates;
                }
                else
                {
                    ratesCache.remove( cacheKey );
                }
            }
        }
        List<Rate> rates = getRates( latest, from );
        if( caching != null )
        {
            cacheMisses.incrementAndGet();
            ratesCache.put( cacheKey, rates );
        }
        return rates;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Iterable<Rate> endOfDateRatesAt( DateTime date, CurrencyUnit from )
        throws MoneyConversionException
    {
        if( date.isBefore( OER_DATA_FROM ) || date.isAfterNow() )
        {
            throw new MoneyConversionException( "End of date exchange rate unavailable before " + OER_DATA_FROM
                                                + ", nor in the future. Was asked for " + date );
        }
        int year = date.get( year() );
        int month = date.get( monthOfYear() );
        int day = date.get( dayOfMonth() );
        String cacheKey = String.format( US, "EOD-%s-%d-%d-%d", from.getCode(), year, month, day );
        if( caching != null )
        {
            List<Rate> rates = ratesCache.get( cacheKey );
            if( rates != null )
            {
                cacheHits.incrementAndGet();
                return rates;
            }
        }
        String url = String.format( endOfDate, year, month, day );
        List<Rate> rates = getRates( url, from );
        if( caching != null )
        {
            cacheMisses.incrementAndGet();
            ratesCache.put( cacheKey, rates );
        }
        return rates;
    }

    private List<Rate> getRates( String url, CurrencyUnit from )
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
                String currencyCode = names.getString( idx );
                try
                {
                    CurrencyUnit currency = CurrencyUnit.of( currencyCode );
                    ratesList.add( extractRate( json, from, currency ) );
                }
                catch( IllegalCurrencyException ex )
                {
                    LOGGER.warn( "Ignored unknown currency '{}' present in OpenExchangeRates data set", currencyCode );
                }
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
        return extractRate( json, from, to );
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

    private Rate extractRate( JSONObject json, CurrencyUnit from, CurrencyUnit to )
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
