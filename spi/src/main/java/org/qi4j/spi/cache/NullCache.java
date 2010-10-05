package org.qi4j.spi.cache;

public final class NullCache<T>
    implements Cache<T>
{
    public T get( String key )
    {
        return null;
    }

    public T remove( String key )
    {
        return null;
    }

    public void put( String key, T value )
    {
    }

    public boolean exists( String key )
    {
        return false;
    }
}
