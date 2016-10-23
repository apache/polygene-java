package org.apache.zest.api.identity;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StringIdentity
    implements Identity
{
    private final String value;

    public StringIdentity(String value)
    {
        Objects.requireNonNull( value, "Identity can not be null." );
        this.value = value;
    }

    public StringIdentity(byte[] bytes)
    {
        value = new String(bytes, StandardCharsets.UTF_8);
    }

    public String value()
    {
        return value;
    }

    @Override
    public byte[] toBytes()
    {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString()
    {
        return value;
    }

    public static Identity fromString(String serializedState)
    {
        return new StringIdentity( serializedState );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        StringIdentity that = (StringIdentity) o;

        return value.equals(that.value);

    }

    @Override
    public int hashCode()
    {
        return value.hashCode();
    }
}
