package org.qi4j.library.http;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Iterator;

public class Dispatchers
    implements Iterable<Dispatchers.Dispatcher>, Serializable
{
    public enum Dispatcher
    {
        FORWARD, REQUEST
    }

    private final EnumSet<Dispatcher> dispatchers;

    private Dispatchers( EnumSet<Dispatcher> dispatchers )
    {
        this.dispatchers = dispatchers;
    }

    public Iterator<Dispatcher> iterator()
    {
        return dispatchers.iterator();
    }

    public static Dispatchers dispatchers( Dispatcher first, Dispatcher... rest )
    {
        return new Dispatchers( EnumSet.of( first, rest ) );
    }
}
