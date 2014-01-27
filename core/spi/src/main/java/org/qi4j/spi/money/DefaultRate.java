/*
 * Copyright 2014 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.qi4j.api.money.Rate;

/**
 * Default Rate implementation.
 */
public final class DefaultRate
    implements Rate
{

    private final CurrencyUnit from;
    private final CurrencyUnit to;
    private final DateTime when;
    private final BigDecimal rate;

    public DefaultRate( CurrencyUnit from, CurrencyUnit to, DateTime when, BigDecimal rate )
    {
        this.from = from;
        this.to = to;
        this.when = when;
        this.rate = rate;
    }

    @Override
    public CurrencyUnit from()
    {
        return from;
    }

    @Override
    public CurrencyUnit to()
    {
        return to;
    }

    @Override
    public DateTime when()
    {
        return when;
    }

    @Override
    public Money convert( Money money )
    {
        return convert( money, RoundingMode.HALF_UP );
    }

    @Override
    public Money convert( Money money, RoundingMode roundingMode )
    {
        if( !money.getCurrencyUnit().equals( from ) )
        {
            throw new IllegalArgumentException( "This Rate cannot convert from non " + from + " Money: " + money );
        }
        return money.convertedTo( to, rate, roundingMode );
    }

    @Override
    public BigMoney convert( BigMoney money )
    {
        if( !money.getCurrencyUnit().equals( from ) )
        {
            throw new IllegalArgumentException( "This Rate cannot convert from non " + from + " BigMoney: " + money );
        }
        return money.convertedTo( to, rate );
    }

    @Override
    public String toString()
    {
        return "Rate{"
               + "from=" + from + ", "
               + "to=" + to + ", "
               + "when=" + when + ", "
               + "rate=" + rate
               + '}';
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode( this.from );
        hash = 79 * hash + Objects.hashCode( this.to );
        hash = 79 * hash + Objects.hashCode( this.when );
        hash = 79 * hash + Objects.hashCode( this.rate );
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( obj == null )
        {
            return false;
        }
        if( getClass() != obj.getClass() )
        {
            return false;
        }
        final DefaultRate other = (DefaultRate) obj;
        if( !Objects.equals( this.from, other.from ) )
        {
            return false;
        }
        if( !Objects.equals( this.to, other.to ) )
        {
            return false;
        }
        if( !Objects.equals( this.when, other.when ) )
        {
            return false;
        }
        return Objects.equals( this.rate, other.rate );
    }

}
