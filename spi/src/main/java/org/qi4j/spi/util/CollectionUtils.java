package org.qi4j.spi.util;

import java.util.Iterator;

public final class CollectionUtils
{
    public static <K> Object firstElementOrNull( Iterable iterable )
    {
        Iterator iterator = iterable.iterator();
        if( iterator.hasNext() )
        {
            return iterator.next();
        }
        else
        {
            return null;
        }
    }
}
