package org.qi4j.runtime.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public interface Specification<T>
{
    boolean matches( T subject );

    public static final class CollectionFilter
    {
        public static <T> Collection<T> filterBy( Collection<T> input, Specification<T> specification )
        {
            if( input == null || input.isEmpty() )
            {
                return Collections.emptyList();
            }
            if( specification == null )
            {
                return Collections.unmodifiableCollection( input );
            }

            Collection<T> result = new ArrayList<T>( input.size() );
            for( T element : input )
            {
                if( specification.matches( element ) )
                {
                    result.add( element );
                }
            }

            return result;
        }
    }
}
