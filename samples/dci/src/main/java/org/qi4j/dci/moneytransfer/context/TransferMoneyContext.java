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

package org.qi4j.dci.moneytransfer.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.dci.moneytransfer.domain.data.BalanceData;

/**
 * Context for transfer of money between two accounts. Roles are defined within the context,
 * and only the role maps know about them outside of this context.
 */
public class TransferMoneyContext
{
    // Object->Role mappings
    public SourceAccountRole sourceAccount;
    public DestinationAccountRole destinationAccount;

    // Context setup
    // Objects are specified using their data interface, and then cast to the role interfaces
    public TransferMoneyContext bind( BalanceData source, BalanceData destination )
    {
        this.sourceAccount = (SourceAccountRole) source;
        this.destinationAccount = (DestinationAccountRole) destination;
        return this;
    }

    // Interactions
    public Integer availableFunds()
    {
        return sourceAccount.availableFunds();
    }

    public void transfer( int amount )
        throws IllegalArgumentException
    {
        sourceAccount.transfer( amount, destinationAccount );
    }

    // More interactions could go here...

    // Roles defined by this context, with default implementations
    @Mixins( SourceAccountRole.Mixin.class )
    public interface SourceAccountRole
    {
        // Role Methods
        public void transfer( int amount, DestinationAccountRole destinationAccount )
            throws IllegalArgumentException;

        Integer availableFunds();

        // Default implementation

        class Mixin
            implements SourceAccountRole
        {
            @This
            BalanceData data;

            public Integer availableFunds()
            {
                // Could be balance, or balance - non-confirmed transfers, or somesuch
                return data.getBalance();
            }

            public void transfer( int amount, DestinationAccountRole destinationAccount )
                throws IllegalArgumentException
            {
                // Validate command
                if( !( data.getBalance() >= amount ) )
                {
                    throw new IllegalArgumentException( "Not enough available funds" );
                }

                // Command is ok - create events in the data
                data.decreasedBalance( amount );

                // Look up the destination account from the current transfer context
                destinationAccount.deposit( amount );
            }
        }
    }

    @Mixins( DestinationAccountRole.Mixin.class )
    public interface DestinationAccountRole
    {
        public void deposit( int amount );

        class Mixin
            implements DestinationAccountRole
        {
            @This
            BalanceData data;

            public void deposit( int amount )
            {
                data.increasedBalance( amount );
            }
        }
    }
}
