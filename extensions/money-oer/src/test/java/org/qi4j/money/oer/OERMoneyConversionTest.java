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

import org.junit.BeforeClass;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.money.AbstractMoneyConversionTest;

import static org.qi4j.api.common.Visibility.layer;
import static org.qi4j.test.util.Assume.assumeConnectivity;
import static org.qi4j.test.util.Assume.assumeSystemPropertyNotNull;

/**
 * OpenExchangeRates.org MoneyConversion Test.
 * <p>If openexchangerates.org cannot be reached, the test is skipped.</p>
 * <p>Test run only if the {@literal oer-api-key} SystemProperty is set and use its value to set the
 * OpenExchangeRates.org API key. If the property does not exists, the test is skipped.</p>
 */
public class OERMoneyConversionTest
    extends AbstractMoneyConversionTest
{
    private static String apiKey;

    @BeforeClass
    public static void beforeOERMoneyConversionTests()
    {
        assumeConnectivity( "openexchangerates.org", 443 );
        apiKey = assumeSystemPropertyNotNull( "oer-api-key" );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        ModuleAssembly configModule = module;
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
    }
}
