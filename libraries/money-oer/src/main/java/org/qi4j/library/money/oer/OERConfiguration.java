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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * OpenExchangeRates.org Configuration.
 */
public interface OERConfiguration
    extends Enabled
{
    /**
     * OpenExchangeRates.org account level.
     */
    enum AccountLevel
    {
        free, developer, enterprise, unlimited
    }

    // START SNIPPET: config
    /**
     * Set your OpenExchangeRates.org API key.
     * @return your OpenExchangeRates.org API key.
     */
    Property<String> apiKey();

    /**
     * Define your OpenExchangeRates.org account level.
     * @return OpenExchangeRates.org account level, default to {@link AccountLevel#free}
     */
    @UseDefaults
    Property<AccountLevel> accountLevel();

    /**
     * HTTPS availability depend on your OpenExchangeRates.org account level.
     * @return true to use HTTPS, false to use HTTP, defaults to false
     */
    @UseDefaults
    Property<Boolean> https();
    // END SNIPPET: config
}
