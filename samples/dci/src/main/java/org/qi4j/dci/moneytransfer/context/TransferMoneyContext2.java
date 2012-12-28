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

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.dci.moneytransfer.domain.data.BalanceData;
import org.qi4j.library.constraints.annotation.GreaterThan;

/**
 * Context for transfer of money between two accounts. Roles are defined within the context,
 * and only the role maps know about them outside of this context.
 */
public class TransferMoneyContext2
    implements TransientComposite
{
    // Object->Role mappings
    private SourceAccountRole sourceAccount = new SourceAccountRole();
    private DestinationAccountRole destinationAccount = new DestinationAccountRole();

    public TransferMoneyContext2 bind( BalanceData source, BalanceData destination )
    {
        // Bind methodful roles to data instances
        sourceAccount.bind( source );
        destinationAccount.bind( destination );
        return this;
    }

    // Interactions
    public Integer availableFunds()
    {
        return sourceAccount.availableFunds();
    }

    public void transfer( @GreaterThan( 0 ) int amount )
        throws IllegalArgumentException
    {
        sourceAccount.transfer( amount );
    }

    // More interactions could go here...

    // Roles defined by this context, with default implementations
    class SourceAccountRole
        extends Role<BalanceData>
    {
        Integer availableFunds()
        {
            // Could be balance, or balance - non-confirmed transfers, or somesuch
            return self.getBalance();
        }

        void transfer( int amount )
            throws IllegalArgumentException
        {
            // Validate command
            if( !( self.getBalance() >= amount ) )
            {
                throw new IllegalArgumentException( "Not enough available funds" );
            }

            // Command is ok - create events in the data
            self.decreasedBalance( amount );

            // Look up the destination account from the current transfer context
            destinationAccount.deposit( amount );
        }
    }

    class DestinationAccountRole
        extends Role<BalanceData>
    {
        void deposit( int amount )
        {
            self.increasedBalance( amount );
        }
    }
}
