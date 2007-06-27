package org.qi4j.library.general.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.io.Serializable;

/**
 * This is a stateful mixin for Money.
 *
 * Money in this case is a value object which stores an amount and currency.
 * Both amount and currency must be immutable.
 */
public interface Money extends Serializable
{
    BigDecimal getAmount();

    // TODO: Amount should be immutable
    void setAmount( BigDecimal amount );

    // TODO: Currency should be immutable
    void setCurrency( Currency currency );

    Currency getCurrency();
}