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

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.money.Rate;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.cache.ehcache.assembly.EhCacheAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.qi4j.api.common.Visibility.layer;
import static org.qi4j.test.util.Assume.assumeConnectivity;

/**
 * OpenExchangeRates.org MoneyConversion Cache Test.
 */
public class OERMoneyConversionCacheTest
    extends AbstractQi4jTest
{

    private static String apiKey;

    @BeforeClass
    public static void beforeOERMoneyConversionTests()
    {
        assumeConnectivity( "openexchangerates.org", 443 );
        apiKey = System.getProperty( "oer-api-key" );
        assumeThat( apiKey, notNullValue() );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module.layer().module( "Config" );
        new EhCacheAssembler( Visibility.module ).
            withConfig( configModule, layer ).
            assemble( module );
        // START SNIPPET: assembly
        new OERMoneyConversionAssembler().
            withVisibility( layer ).
            withConfigModule( configModule ).
            withConfigVisibility( layer ).
            assemble( module );
        // END SNIPPET: assembly
        OERConfiguration config = configModule.forMixin( OERConfiguration.class ).declareDefaults();
        config.enabled().set( true );
        config.apiKey().set( apiKey );
        new EntityTestAssembler().assemble( configModule );
    }

    @Test
    public void testCurrentRateCache()
    {
        OERMoneyConversionService moneyConversion = module.findService( OERMoneyConversionService.class ).get();
        assertThat( moneyConversion.cacheHits(), equalTo( 0L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 0L ) );

        moneyConversion.currentRate( CurrencyUnit.EUR, CurrencyUnit.of( "MAD" ) );
        assertThat( moneyConversion.cacheHits(), equalTo( 0L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 1L ) );

        moneyConversion.currentRate( CurrencyUnit.EUR, CurrencyUnit.of( "MAD" ) );
        assertThat( moneyConversion.cacheHits(), equalTo( 1L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 1L ) );
    }

    @Test
    public void testEODRateCache()
    {
        OERMoneyConversionService moneyConversion = module.findService( OERMoneyConversionService.class ).get();
        assertThat( moneyConversion.cacheHits(), equalTo( 0L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 0L ) );

        Rate rate = moneyConversion.endOfDateRateAt( new DateTime( 2014, 1, 23, 23, 23 ),
                                                     CurrencyUnit.EUR,
                                                     CurrencyUnit.of( "MAD" ) );
        Money dinars = rate.convert( Money.of( CurrencyUnit.EUR, 10 ) );
        assertThat( dinars.getAmount().doubleValue(), equalTo( 114.92 ) );
        assertThat( moneyConversion.cacheHits(), equalTo( 0L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 1L ) );

        rate = moneyConversion.endOfDateRateAt( new DateTime( 2014, 1, 23, 23, 23 ),
                                                CurrencyUnit.EUR,
                                                CurrencyUnit.of( "MAD" ) );
        dinars = rate.convert( Money.of( CurrencyUnit.EUR, 10 ) );
        assertThat( dinars.getAmount().doubleValue(), equalTo( 114.92 ) );
        assertThat( moneyConversion.cacheHits(), equalTo( 1L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 1L ) );
    }

    @Test
    public void testCurrentRatesCache()
    {
        OERMoneyConversionService moneyConversion = module.findService( OERMoneyConversionService.class ).get();
        assertThat( moneyConversion.cacheHits(), equalTo( 0L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 0L ) );

        moneyConversion.currentRates( CurrencyUnit.EUR );
        assertThat( moneyConversion.cacheHits(), equalTo( 0L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 1L ) );

        moneyConversion.currentRates( CurrencyUnit.EUR );
        assertThat( moneyConversion.cacheHits(), equalTo( 1L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 1L ) );
    }

    @Test
    public void testEODRatesCache()
    {
        OERMoneyConversionService moneyConversion = module.findService( OERMoneyConversionService.class ).get();
        assertThat( moneyConversion.cacheHits(), equalTo( 0L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 0L ) );

        moneyConversion.endOfDateRatesAt( new DateTime( 2014, 1, 23, 23, 23 ), CurrencyUnit.EUR );
        assertThat( moneyConversion.cacheHits(), equalTo( 0L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 1L ) );

        moneyConversion.endOfDateRatesAt( new DateTime( 2014, 1, 23, 23, 23 ), CurrencyUnit.EUR );
        assertThat( moneyConversion.cacheHits(), equalTo( 1L ) );
        assertThat( moneyConversion.cacheMisses(), equalTo( 1L ) );
    }
}
