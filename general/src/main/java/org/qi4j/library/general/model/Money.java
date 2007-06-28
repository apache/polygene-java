package org.qi4j.library.general.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.io.Serializable;

/**
 * Generic interface for Money which stores an amount and currency.
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