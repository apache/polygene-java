/*
 * Copyright 2007 Edward Yakop.
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
package org.qi4j.business.operations;

import java.math.BigDecimal;
import java.util.Currency;
import org.ops4j.lang.NullArgumentException;

/**
 * TODO: add arithmetic computation on Money
 *
 * @since 1.0.0
 */
public class Money
{
    private final BigDecimal m_amount;
    private final Currency m_currency;

    public Money( Currency currency, BigDecimal amount )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( currency, "currency" );
        NullArgumentException.validateNotNull( amount, "amount" );

        m_currency = currency;
        m_amount = amount;
    }

    public final BigDecimal getAmount()
    {
        return m_amount;
    }

    public final Currency getCurrency()
    {
        return m_currency;
    }
}
