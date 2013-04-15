package org.qi4j.api.docsupport;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.ModuleAssembly;

public class CompositionDocs
{
// START SNIPPET: comp1
    @Mixins( { BalanceCheckMixin.class } )
    public interface BankAccount
    {
        Money checkBalance();
// END SNIPPET: comp1
// START SNIPPET: comp1
    }
// END SNIPPET: comp1

// START SNIPPET: comp2
    public void assemble( ModuleAssembly module )
    {
        module.entities( BankAccount.class );
    }
// END SNIPPET: comp2

    public static class BalanceCheckMixin
        implements BankAccount
    {
        @Override
        public Money checkBalance()
        {
            return null;
        }
    }

    public static class Money
    {
    }
}
