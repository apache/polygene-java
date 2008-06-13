package org.qi4j.runtime.util;

import java.util.Iterator;

/**
 * @author mh14 @ jexp.de
 * @since 12.06.2008 22:13:02 (c) 2008 jexp.de
 */
public class CollectionUtils
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
