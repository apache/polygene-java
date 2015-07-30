/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.zest.dci.moneytransfer.domain.data;

import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.injection.scope.State;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.library.constraints.annotation.GreaterThan;

/**
 * Maintain a balance for an account, of any type. Methods are in past tense,
 * and are considered events. Only minimal sanity checks are performed.
 * The roles invoking these methods decide if it's ok to perform the change or not.
 */
@Mixins( BalanceData.Mixin.class )
public interface BalanceData
{
    void increasedBalance( @GreaterThan( 0 ) Integer amount );

    void decreasedBalance( @GreaterThan( 0 ) Integer amount );

    Integer getBalance();

    // Default implementation

    class Mixin
        implements BalanceData
    {
        @State
        @UseDefaults
        Property<Integer> balance;

        public void increasedBalance( Integer amount )
        {
            balance.set( balance.get() + amount );
        }

        public void decreasedBalance( Integer amount )
        {
            balance.set( balance.get() - amount );
        }

        public Integer getBalance()
        {
            return balance.get();
        }
    }
}
