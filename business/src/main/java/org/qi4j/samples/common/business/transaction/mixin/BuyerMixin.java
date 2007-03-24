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
package org.qi4j.samples.common.business.transaction.mixin;

import java.util.List;

public final class BuyerMixin implements Buyer
{
    private List<Transaction> m_transactions;

    public BuyerMixin()
    {
    }

    public final List<Transaction> getBuyerTransactions()
    {
        return m_transactions;
    }

    public final void setBuyerTransactions( List<Transaction> transactions )
    {
        m_transactions = transactions;
    }
}
