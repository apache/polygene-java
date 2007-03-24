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
package org.qi4j.business.transaction.mixin;

import java.util.List;

public final class TransactionMixin implements Transaction
{
    private List<TransactionEntry> m_entries;
    private long m_time;

    public TransactionMixin()
    {
    }

    public List<TransactionEntry> getTransactionEntries()
    {
        return m_entries;
    }

    public long getTransactionTime()
    {
        return m_time;
    }

    public void setTransactionEntries( List<TransactionEntry> entries )
    {
        m_entries = entries;
    }

    public void setTransactionTime( long time )
    {
        m_time = time;
    }
}
