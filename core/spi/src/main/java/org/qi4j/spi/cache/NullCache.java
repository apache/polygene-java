package org.qi4j.spi.cache;

/**
 * Cache null object.
 */
public final class NullCache<T>
    implements Cache<T>
{
    @Override
    public T get( String key )
    {
        return null;
    }

    @Override
    public T remove( String key )
    {
        return null;
    }

    @Override
    public void put( String key, T value )
    {
    }

    @Override
    public boolean exists( String key )
    {
        return false;
    }
}
