package org.qi4j.api.mixin;


// START SNIPPET: mixinType
public interface BankAccount
{
    Money checkBalance();
}
// END SNIPPET: mixinType

class Money {}

