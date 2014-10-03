package org.qi4j.bootstrap;

import java.util.function.Predicate;
import org.qi4j.api.type.HasTypes;
import org.qi4j.functional.Specifications;

/**
 * Utility specifications for Assemblies.
 */
public class AssemblySpecifications
{
    public static Predicate<HasTypes> types( final Class... types )
    {
        return new Predicate<HasTypes>()
        {
            @Override
            public boolean test( HasTypes item )
            {

                for( Class<?> type : item.types() )
                {
                    if( Specifications.in( types ).test( type ) )
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
