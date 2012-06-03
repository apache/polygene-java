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

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.dci.moneytransfer.domain.data.BalanceData;
import org.qi4j.dci.moneytransfer.rolemap.CreditorRolemap;

/**
 * Context for paying bills from an account to a list of creditor accounts.
 * <p/>
 * Roles are defined within the context
 * A RoleMap lists what Roles an entity can play.
 */
public class PayBillsContext
{
    public SourceAccountRole sourceAccount;

    public PayBillsContext bind( BalanceData account )
    {
        sourceAccount = (SourceAccountRole) account;
        return this;
    }

    public void payBills()
        throws Exception
    {
        sourceAccount.payBills();
    }

    /**
     * The SourceAccountRole orchestrates the Pay Bills use case interactions.
     * <p/>
     * Code matches the use case text carefully (see references below).
     * <p/>
     * Pay Bills use case scenario:
     * <p/>
     * 1) Bank finds creditors (could be a use case scenario in itself)
     * 2) Bank calculates the amount owed to creditors
     * 3) Bank verifies sufficient funds
     * 4) Bank transfer money to each creditor
     * <p/>
     * Algorithm (steps to implement the scenario):
     * <p/>
     * 1a) Source Account finds list of creditors
     * <p/>
     * 2a) Source Account loops creditors to find the sum owed
     * <p/>
     * 3a) Source Account verifies that its current balance is greater than the sum owed, and throws an exception if not
     * <p/>
     * 4a) Source Account loops creditors
     * 4b) Make a MoneyTransfer of the amount owed to each creditor
     */

    @Mixins( SourceAccountRole.Mixin.class )
    public interface SourceAccountRole
    {
        void payBills()
            throws Exception;

        class Mixin
            implements SourceAccountRole
        {
            @Structure
            UnitOfWorkFactory uowf;

            @This
            BalanceData data;

            public void payBills()
                throws IllegalArgumentException
            {
                List<BalanceData> creditors = getCreditors();                                             // 1a

                Integer sumOwed = getSumOwedTo( creditors );                                              // 2a

                if( data.getBalance() - sumOwed < 0 )                                                      // 3a
                {
                    throw new IllegalArgumentException( "Insufficient funds to pay bills." );
                }

                final TransferMoneyContext transferMoney = new TransferMoneyContext();
                for( BalanceData creditor : creditors )                                                    // 4a
                {
                    if( creditor.getBalance() < 0 )
                    {
                        final Integer amountOwed = -creditor.getBalance();

                        // Bind nested context and execute enactment
                        transferMoney.bind( data, creditor ).transfer( amountOwed );                      // 4b
                    }
                }
            }

            // Internal helper methods to make the code above more readable / comparable to the algorithm text

            private List<BalanceData> getCreditors()
            {
                // Creditor retrieval could be a use case in itself...
                List<BalanceData> creditors = new ArrayList<BalanceData>();
                creditors.add( uowf.currentUnitOfWork().get( CreditorRolemap.class, "BakerAccount" ) );
                creditors.add( uowf.currentUnitOfWork().get( CreditorRolemap.class, "ButcherAccount" ) );
                return creditors;
            }

            private Integer getSumOwedTo( List<BalanceData> creditors )
            {
                Integer sumOwed = 0;
                for( BalanceData creditor : creditors )
                {
                    sumOwed += creditor.getBalance() < 0 ? -creditor.getBalance() : 0;
                }

                return sumOwed;
            }
        }
    }
}