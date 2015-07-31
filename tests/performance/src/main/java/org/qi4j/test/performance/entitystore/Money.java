/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.test.performance.entitystore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

public class Money
    implements Serializable
{
    private final BigDecimal amount;
    private final Currency currency;
    private final boolean prefixed;

    public Money( BigDecimal amount, Currency currency, boolean prefixed )
    {
        this.amount = amount;
        this.currency = currency;
        this.prefixed = prefixed;
    }

    public boolean isPrefixed()
    {
        return prefixed;
    }

    public BigDecimal amount()
    {
        return amount;
    }

    public Currency currency()
    {
        return currency;
    }

    @Override
    public String toString()
    {
        if( prefixed )
        {
            return currency.toString() + amount.toPlainString();
        }
        else
        {
            return amount.toPlainString() + currency.toString();
        }
    }
}
