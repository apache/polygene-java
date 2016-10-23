package org.apache.zest.dci.moneytransfer.test;

import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.identity.StringIdentity;

public interface AccountIds
{
    Identity SAVINGS_ACCOUNT_ID = new StringIdentity( "SavingsAccountId" );
    Identity CHECKING_ACCOUNT_ID = new StringIdentity( "CheckingAccountId" );
    Identity CREDITOR_ID1 = new StringIdentity( "BakerAccount" );
    Identity CREDITOR_ID2 = new StringIdentity( "ButcherAccount" );

}
