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

public final class TransactionEntryMixin<E extends Number, F extends Item>
    implements TransactionEntry<E, F>
{
    private F m_item;
    private E m_quantity;

    public F getItem()
    {
        return m_item;
    }

    public void setItem( F item )
    {
        m_item = item;
    }

    public E getQuantity()
    {
        return m_quantity;
    }

    public void setQuantity( E quantity )
    {
        m_quantity = quantity;
    }
}
