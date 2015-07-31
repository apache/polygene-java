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

package org.qi4j.dci.moneytransfer.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.dci.moneytransfer.context.PayBillsContext2;
import org.qi4j.dci.moneytransfer.context.TransferMoneyContext2;
import org.qi4j.dci.moneytransfer.domain.data.BalanceData;
import org.qi4j.dci.moneytransfer.domain.entity.CheckingAccountEntity;
import org.qi4j.dci.moneytransfer.domain.entity.CreditorEntity;
import org.qi4j.dci.moneytransfer.domain.entity.SavingsAccountEntity;
import org.qi4j.test.EntityTestAssembler;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Test of TransferMoneyContext
 */
public class TransferMoneyTest2
{
    private static Module module;
    public static final String SAVINGS_ACCOUNT_ID = "SavingsAccountId";
    public static final String CHECKING_ACCOUNT_ID = "CheckingAccountId";
    public static final String CREDITOR_ID1 = "BakerAccount";
    public static final String CREDITOR_ID2 = "ButcherAccount";

    @BeforeClass
    public static void setup()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.entities(
                    CheckingAccountEntity.class,
                    SavingsAccountEntity.class,
                    CreditorEntity.class );

                new EntityTestAssembler().assemble( module );

                module.transients( TransferMoneyContext2.class );
                module.objects( PayBillsContext2.class );
            }
        };

        module = assembler.module();

        bootstrapData();
    }

    @Before
    public void beforeBalances()
    {
        System.out.println( "Before enactment:" );
        printBalances();
        System.out.println( "" );
    }

    @After
    public void afterBalances()
    {
        System.out.println( "After enactment:" );
        printBalances();
        System.out.println( "-----------------" );
    }

    private void printBalances()
    {
        UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Print balances" ) );

        try
        {
            System.out
                .println( SAVINGS_ACCOUNT_ID + ":" + uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID ).getBalance() );
            System.out
                .println( CHECKING_ACCOUNT_ID + ":" + uow.get( BalanceData.class, CHECKING_ACCOUNT_ID ).getBalance() );
            System.out.println( CREDITOR_ID1 + ":" + uow.get( BalanceData.class, CREDITOR_ID1 ).getBalance() );
            System.out.println( CREDITOR_ID2 + ":" + uow.get( BalanceData.class, CREDITOR_ID2 ).getBalance() );
        }
        finally
        {
            uow.discard();
        }
    }

    private static void bootstrapData()
        throws Exception
    {
        UnitOfWork uow = module.newUnitOfWork( newUsecase( "Bootstrap data" ) );
        try
        {
            SavingsAccountEntity account = uow.newEntity( SavingsAccountEntity.class, SAVINGS_ACCOUNT_ID );
            account.increasedBalance( 1000 );

            CheckingAccountEntity checkingAccount = uow.newEntity(CheckingAccountEntity.class, CHECKING_ACCOUNT_ID);
            checkingAccount.increasedBalance(200);

            // Create some creditor debt
            BalanceData bakerAccount = uow.newEntity( CreditorEntity.class, CREDITOR_ID1 );
            bakerAccount.decreasedBalance( 50 );

            BalanceData butcherAccount = uow.newEntity( CreditorEntity.class, CREDITOR_ID2 );
            butcherAccount.decreasedBalance( 90 );

            // Save
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void transferHalfOfMoneyFromSavingsToChecking()
        throws Exception
    {
        UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Transfer from savings to checking" ) );

        try
        {
            // Select source and destination
            BalanceData source = uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID );
            BalanceData destination = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );

            // Instantiate context and execute enactments with that context
            TransferMoneyContext2 context = module.newTransient( TransferMoneyContext2.class )
                .bind( source, destination );

            // Query for half the balance
            final Integer amountToTransfer = context.availableFunds() / 2;

            // Transfer from savings to checking
            context.transfer( amountToTransfer );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test( expected = IllegalArgumentException.class )
    public void transferTwiceOfMoneyFromSavingsToChecking()
        throws Exception
    {
        UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Transfer from savings to checking" ) );

        try
        {
            // Select source and destination
            BalanceData source = uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID );
            BalanceData destination = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );

            // Instantiate context and execute enactments with that context
            TransferMoneyContext2 context = module.newTransient( TransferMoneyContext2.class )
                .bind( source, destination );

            // Query for double the balance
            final Integer amountToTransfer = context.availableFunds() * 2;

            // Transfer from savings to checking
            context.transfer( amountToTransfer );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void payAllBills()
        throws Exception
    {
        UnitOfWork uow = module.newUnitOfWork( newUsecase( "Pay all bills from checking to creditors" ) );
        try
        {
            BalanceData source = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );

            PayBillsContext2 context = module.newObject( PayBillsContext2.class );
            context.bind( source ).payBills();
        }
        finally
        {
            uow.discard();
        }
    }
}
